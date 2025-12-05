package com.cqcloud.platform.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqcloud.platform.exception.BizException;

import com.cqcloud.platform.config.RustfsProperties;
import com.cqcloud.platform.config.RustfsTemplate;
import com.cqcloud.platform.dto.SysFileSelDto;
import com.cqcloud.platform.entity.SysFile;
import com.cqcloud.platform.mapper.SysFileMapper;
import com.cqcloud.platform.service.SysFileService;
import com.cqcloud.platform.vo.SysFileVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 系统基础信息--文件管理服务实现类
 *
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2023年5月20日 🐬🐇 💓💕
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

	private final RustfsTemplate rustfsTemplate;

	private final RustfsProperties rustfsProperties;

	@Override
	public Map<String, String> uploadFile(MultipartFile file, String groupId, Integer sort) {
		String fileId = IdUtil.simpleUUID();
		// 如果 groupId 为空，使用默认值 "defaultGroupId"
		groupId = StringUtils.isBlank(groupId) ? "defaultGroupId" : groupId;
		// 如果 sort 为空，使用默认值 0
		sort = Objects.isNull(sort) ? 0 : sort;
		String originalFilename = new String(
				Objects.requireNonNull(file.getOriginalFilename()).getBytes(StandardCharsets.ISO_8859_1),
				StandardCharsets.UTF_8);
		String suffix = FileUtil.extName(originalFilename);
		// 生成文件名
		String fileName = IdUtil.simpleUUID() + StrUtil.DOT + suffix;
		// 生成文件目录，格式为 yyyy/MM/dd/
		String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
		// 拼接完整路径,存储在 rustfs 中的完整路径
		String fullFilePath = dir + fileName;

		// 准备返回结果
		Map<String, String> resultMap = new LinkedHashMap<>();
		resultMap.put("bucketName", rustfsProperties.getBucketName());
		resultMap.put("fileId", fileId);
		resultMap.put("fileName", fileName);
		resultMap.put("originalFilename", originalFilename);
		resultMap.put("fullFilePath", fullFilePath);
		resultMap.put("previewById", String.format("/api/sysfile/preview/%s", fileId));
		resultMap.put("url", String.format("/api/sysfile/previewByFileName/%s", fileName));
		// resultMap.put("protocolUrl", String.format("%s/%s/%s",rustfsProperties.getPreviewDomain(), rustfsProperties.getBucketName(),fullFilePath));
		try (InputStream inputStream = file.getInputStream()) {
			// 上传文件到 rustfs
            rustfsTemplate.putObject(rustfsProperties.getBucketName(), fullFilePath, inputStream, file.getSize(),
					file.getContentType());
			// 文件管理数据记录
            rustfsInsertToDb(file, fileId, fileName, originalFilename, suffix, groupId, fullFilePath, sort);
		}
		catch (BizException e) {
			throw e;
		}
		catch (Exception e) {
			log.error("上传失败", e);
			throw new BizException(e.getLocalizedMessage());
		}
		return resultMap;
	}

	private void rustfsInsertToDb(MultipartFile file, String fileId, String fileName, String originalFilename,
			String suffix, String groupId, String fullFilePath, Integer sort) {
		SysFile sysFile = new SysFile();
		sysFile.setId(fileId);
		sysFile.setName(fileName);
		sysFile.setOriginal(originalFilename);
		sysFile.setGroupId(groupId);
		sysFile.setFileType(suffix);
		sysFile.setSuffix(suffix);
		sysFile.setSize((int) file.getSize());
		sysFile.setPreviewUrl(fullFilePath);
		sysFile.setStorageType("rustfs");
		sysFile.setBucketName(rustfsProperties.getBucketName());
		sysFile.setObjectName(fileName);
		sysFile.setVisitCount(0);
		sysFile.setSort(sort);
		sysFile.setTenantId(1L);
		if (!this.save(sysFile)) {
			throw new BizException("上传失败");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteFile(String id) {
		SysFile file = this.getById(id);
        rustfsTemplate.removeObject(rustfsProperties.getBucketName(), file.getName());
		return this.removeById(file);
	}

	@Override
	public IPage<SysFileVo> getSysFileVoPage(Page<?> page, SysFileSelDto dto) {
		return baseMapper.getSysFileVoPage(page, dto);
	}

	@Override
	public void getFile(String fileId, HttpServletResponse response) {
		// 1. 根据文件ID查找文件的元数据
		SysFile sf = this.getById(fileId);
		if (Objects.isNull(sf)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// 获取文件所在的桶（Bucket）和文件路径
		String bucketName = sf.getBucketName();
		String fullFilePath = sf.getPreviewUrl();

		try {
			// 2. 从rustfs获取文件输入流
			InputStream fileInputStream = rustfsTemplate.getObject(bucketName, fullFilePath);

			// 获取文件的Content-Type
			String contentType = URLConnection.guessContentTypeFromName(fileId);
			if (Objects.isNull(contentType)) {
				contentType = "application/octet-stream";
			}
			// 设置响应头（可选，根据需要设置）
			response.setContentType(contentType);
			String encodedFileName = URLEncoder.encode(sf.getName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");
			response.setContentLengthLong(sf.getSize());

			// 3. 写文件内容到响应输出流
			try (OutputStream outputStream = response.getOutputStream()) {
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = fileInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();
			}
		}
		catch (Exception e) {
			log.error("读取文件失败，文件名: {}, 错误信息: {}", fileId, e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("读取文件失败，请稍后重试！");
			}
			catch (IOException ioException) {
				log.error("响应错误消息失败", ioException);
			}
		}
	}

	@Override
	public void previewFile(String fileId, HttpServletResponse response) {
		// 1. 根据文件ID查找文件的元数据
		SysFile sf = this.getById(fileId);
		if (Objects.isNull(sf)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// 获取文件所在的桶（Bucket）和文件路径
		String bucketName = sf.getBucketName();
		String fullFilePath = sf.getPreviewUrl();

		try {
			// 2. 从rustfs获取文件输入流
			InputStream fileInputStream = rustfsTemplate.getObject(bucketName, fullFilePath);

			// 获取文件的Content-Type
			String contentType = URLConnection.guessContentTypeFromName(fullFilePath);
			if (Objects.isNull(contentType)) {
				contentType = "application/octet-stream";
			}
			// 设置响应头
			response.setContentType(contentType);
			String encodedFileName = URLEncoder.encode(sf.getName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
			// 设置 Content-Disposition 为 inline，这样浏览器会尝试预览文件
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFileName + "\"");
			response.setContentLengthLong(sf.getSize());

			// 3. 写文件内容到响应输出流
			try (OutputStream outputStream = response.getOutputStream()) {
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = fileInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();
			}
		}
		catch (Exception e) {
			log.error("读取文件失败，文件名: {}, 错误信息: {}", fileId, e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("读取文件失败，请稍后重试！");
			}
			catch (IOException ioException) {
				log.error("响应错误消息失败", ioException);
			}
		}
	}

	@Override
	public void previewByFileName(String fileName, HttpServletResponse response) {
		// 1. 根据文件ID查找文件的元数据
		SysFile sf = this.getOne(Wrappers.<SysFile>lambdaQuery().eq(SysFile::getObjectName, fileName));
		if (Objects.isNull(sf)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// 获取文件所在的桶（Bucket）和文件路径
		String bucketName = sf.getBucketName();
		String fullFilePath = sf.getPreviewUrl();

		try {
			// 2. 从rustfs获取文件输入流
			InputStream fileInputStream = rustfsTemplate.getObject(bucketName, fullFilePath);

			// 获取文件的Content-Type
			String contentType = URLConnection.guessContentTypeFromName(fullFilePath);
			if (Objects.isNull(contentType)) {
				contentType = "application/octet-stream";
			}
			// 设置响应头
			response.setContentType(contentType);
			String encodedFileName = URLEncoder.encode(sf.getName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
			// 设置 Content-Disposition 为 inline，这样浏览器会尝试预览文件
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFileName + "\"");
			response.setContentLengthLong(sf.getSize());

			// 3. 写文件内容到响应输出流
			try (OutputStream outputStream = response.getOutputStream()) {
				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = fileInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();
			}
		}
		catch (Exception e) {
			log.error("读取文件失败，文件名: {}, 错误信息: {}", fileName, e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("读取文件失败，请稍后重试！");
			}
			catch (IOException ioException) {
				log.error("响应错误消息失败", ioException);
			}
		}
	}

	@Override
	public Map<String, String> uploadBase64File(String base64Data, String groupId, Integer sort) {
		try {
			// 将Base64字符串转换为MultipartFile
			MultipartFile multipartFile = base64ToMultipartFile(base64Data);

			// 调用现有的上传方法
			return uploadFile(multipartFile, groupId, sort);
		}
		catch (IOException e) {
			log.error("Base64文件上传失败", e);
			throw new BizException("Base64文件上传失败: " + e.getMessage());
		}
	}

	/**
	 * 将Base64字符串转换为MultipartFile 支持JSON数据和PDF文件
	 */
	private MultipartFile base64ToMultipartFile(String base64Data) throws IOException {
		if (StringUtils.isBlank(base64Data)) {
			throw new BizException("Base64数据不能为空");
		}

		String dataPart;
		String mimeType;
		String fileExtension;

		// 解析Base64字符串
		String[] parts = base64Data.split(",");
		if (parts.length == 2) {
			// 带data URL前缀的格式：data:application/json;base64,xxxx
			dataPart = parts[1];
			String headerPart = parts[0];

			// 从header中提取MIME类型
			mimeType = headerPart.split(":")[1].split(";")[0];
			fileExtension = getFileExtensionFromMimeType(mimeType);
		}
		else if (parts.length == 1) {
			// 纯Base64数据，没有data URL前缀 - 自动检测类型
			dataPart = base64Data;
			byte[] decodedBytes = Base64.getDecoder().decode(dataPart);

			// 检测文件类型
			if (isPdfFile(decodedBytes)) {
				mimeType = "application/pdf";
				fileExtension = "pdf";
			}
			else if (isJsonData(decodedBytes)) {
				mimeType = "application/json";
				fileExtension = "json";
			}
			else {
				// 默认按文本处理（可能是纯JSON字符串）
				mimeType = "application/json";
				fileExtension = "json";
			}
		}
		else {
			throw new BizException("Base64数据格式不正确");
		}

		// 解码Base64数据
		byte[] fileBytes = Base64.getDecoder().decode(dataPart);

		// 生成文件名
		String originalFileName = IdUtil.simpleUUID() + "." + fileExtension;

		// 创建MultipartFile对象
		return new MultipartFile() {
			@Override
			public String getName() {
				return "file";
			}

			@Override
			public String getOriginalFilename() {
				return originalFileName;
			}

			@Override
			public String getContentType() {
				return mimeType;
			}

			@Override
			public boolean isEmpty() {
				return fileBytes.length == 0;
			}

			@Override
			public long getSize() {
				return fileBytes.length;
			}

			@Override
			public byte[] getBytes() throws IOException {
				return fileBytes;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(fileBytes);
			}

			@Override
			public void transferTo(File dest) throws IOException, IllegalStateException {
				FileUtil.writeBytes(fileBytes, dest);
			}
		};
	}

	/**
	 * 根据MIME类型获取文件扩展名
	 */
	private String getFileExtensionFromMimeType(String mimeType) {
		switch (mimeType.toLowerCase()) {
			case "application/json":
				return "json";
			case "application/pdf":
				return "pdf";
			case "image/jpeg":
			case "image/jpg":
				return "jpg";
			case "image/png":
				return "png";
			case "image/gif":
				return "gif";
			case "text/plain":
				return "txt";
			default:
				return "dat";
		}
	}

	/**
	 * 检测是否为PDF文件
	 */
	private boolean isPdfFile(byte[] data) {
		// PDF文件以 "%PDF" 开头
		if (data.length >= 4) {
			return data[0] == 0x25 && // %
					data[1] == 0x50 && // P
					data[2] == 0x44 && // D
					data[3] == 0x46; // F
		}
		return false;
	}

	/**
	 * 检测是否为JSON数据
	 */
	private boolean isJsonData(byte[] data) {
		if (data.length == 0) {
			return false;
		}

		try {
			// 尝试解析为JSON
			String content = new String(data, StandardCharsets.UTF_8).trim();

			// JSON通常以 { 或 [ 开头
			if (content.startsWith("{") || content.startsWith("[")) {
				// 尝试用JSON库解析验证
				try {
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.readTree(content);
					return true;
				}
				catch (Exception e) {
					// 如果JSON解析失败，但格式看起来像JSON，仍然认为是JSON
					return content.startsWith("{") && content.endsWith("}")
							|| content.startsWith("[") && content.endsWith("]");
				}
			}
		}
		catch (Exception e) {
			// 编码异常，不是文本数据
			return false;
		}

		return false;
	}

}
