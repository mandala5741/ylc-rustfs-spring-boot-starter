package com.cqcloud.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqcloud.platform.utils.Result;
import com.cqcloud.platform.dto.SysFileSelDto;
import com.cqcloud.platform.service.SysFileService;
import com.cqcloud.platform.vo.SysFileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 系统基础信息--文件管理模块
 *
 * @author weimeilayer@gmail.com
 * @date 2021-12-13 16:28:32
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysfile")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Tag(description = "系统基础信息--文件管理模块操作接口", name = "系统基础信息--文件管理模块操作接口")
public class SysFileController {

	private final SysFileService sysFileService;

	/**
	 * 分页查询文件信息列表
	 * @param page 分页参数
	 * @param dto 查询参数
	 * @return 分页列表
	 */
	@GetMapping("/pagelist")
	@Operation(summary = "分页查询文件信息", description = "分页查询文件信息")
	public Result<IPage<SysFileVo>> getSysFileVoPage(@ParameterObject Page<?> page,
			@ParameterObject SysFileSelDto dto) {
		return Result.ok(sysFileService.getSysFileVoPage(page, dto));
	}

	/**
	 * 上传文件 文件名采用uuid
	 * @param file 文件
	 * @return 文件信息
	 */
	@PostMapping("upload")
	@Parameters({ @Parameter(name = "groupId", description = "分组编号，用于对应多文件", example = "1"),
			@Parameter(name = "sort", description = "排序", required = true, example = "1") })
	@Operation(summary = "上传文件", description = "上传文件")
	public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file, String groupId, Integer sort) {
		return Result.ok(sysFileService.uploadFile(file, groupId, sort));
	}

	/**
	 * 获取文件
	 * @param fileId 文件ID
	 */
	@GetMapping("/{fileId}")
	@Operation(summary = "获取文件", description = "获取文件")
	public void file(@PathVariable String fileId, HttpServletResponse response) {
		sysFileService.getFile(fileId, response);
	}

	/**
	 * 根据id预览文件
	 * @param fileId 文件ID
	 */
	@GetMapping("/preview/{fileId}")
	@Operation(summary = "根据id预览文件", description = "根据文件ID预览文件")
	public void previewFile(@PathVariable String fileId, HttpServletResponse response) {
		sysFileService.previewFile(fileId, response);
	}

	/**
	 * 根据文件名字预览文件
	 * @param fileName 文件名称
	 */
	@GetMapping("/previewByFileName/{fileName}")
	@Operation(summary = "根据文件名字预览文件", description = "根据文件名字预览文件")
	public void previewByFileName(@PathVariable("fileName") String fileName, HttpServletResponse response) {
		sysFileService.previewByFileName(fileName, response);
	}

}
