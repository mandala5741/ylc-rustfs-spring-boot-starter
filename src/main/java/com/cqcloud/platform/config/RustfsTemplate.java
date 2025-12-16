package com.cqcloud.platform.config;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * aws-s3 通用存储操作 支持所有兼容s3协议的云存储: {阿里云OSS，腾讯云COS，七牛云，京东云，minio,rustfs 等}
 *
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕2024年3月7日🐬🐇 💓💕
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RustfsProperties.class)
public class RustfsTemplate implements InitializingBean {

	private final RustfsProperties ossProperties;

	@Getter
	private S3Client s3Client;

	/**
	 * 创建bucket
	 * 
	 * @param bucketName bucket名称
	 */
	@SneakyThrows
	public void createBucket(String bucketName) {
		if (getAllBuckets().stream().noneMatch(b -> b.name().equals(bucketName))) {
			s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
		}
	}

	/**
	 * 获取全部bucket
	 */
	@SneakyThrows
	public List<Bucket> getAllBuckets() {
		return s3Client.listBuckets().buckets();
	}

	/**
	 * @param bucketName bucket名称
	 */
	@SneakyThrows
	public Optional<Bucket> getBucket(String bucketName) {
		return getAllBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
	}

	/**
	 * 删除bucket
	 * 
	 * @param bucketName bucket名称
	 */
	@SneakyThrows
	public void removeBucket(String bucketName) {
		s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
	}

	/**
	 * 根据文件前缀查询文件
	 * 
	 * @param bucketName bucket名称
	 * @param prefix     前缀
	 * @param recursive  是否递归查询
	 * @return S3ObjectSummary 列表
	 */
	public List<S3Object> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive) {
		// 构建查询请求
		ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build();
		// 获取查询结果
		ListObjectsV2Response response = s3Client.listObjectsV2(request);
		// 返回文件列表
		return new ArrayList<>(response.contents());
	}

	/**
	 * 获取文件
	 * 
	 * @param bucketName bucket名称
	 * @param objectName 文件名称
	 * @return 二进制流
	 */
	public ResponseInputStream<GetObjectResponse> getObject(String bucketName, String objectName) {
		return s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(objectName).build());
	}

	/**
	 * 上传文件
	 * 
	 * @param bucketName bucket名称
	 * @param objectName 文件名称
	 * @param stream     文件流
	 */
	public void putObject(String bucketName, String objectName, InputStream stream) throws Exception {
		putObject(bucketName, objectName, stream, stream.available(), MediaType.APPLICATION_OCTET_STREAM_VALUE);
	}

	/**
	 * 上传文件
	 * 
	 * @param bucketName  bucket名称
	 * @param objectName  文件名称
	 * @param stream      文件流
	 * @param size        大小
	 * @param contextType 类型
	 */
	public PutObjectResponse putObject(String bucketName, String objectName, InputStream stream, long size,
			String contextType) throws Exception {
		byte[] bytes = new byte[(int) size];
		stream.read(bytes);
		RequestBody requestBody = RequestBody.fromBytes(bytes);
		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(objectName).contentLength(size)
				.contentType(contextType).build();
		return s3Client.putObject(request, requestBody);
	}

	/**
	 * 删除文件
	 * 
	 * @param bucketName bucket名称
	 * @param objectName 文件名称
	 */
	public void removeObject(String bucketName, String objectName) {
		s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectName).build());
	}

	/**
	 * 初始化
	 */
	@Override
	public void afterPropertiesSet() {

		S3ClientBuilder s3ClientBuilder = S3Client.builder().endpointOverride(URI.create(ossProperties.getEndpoint()))
				.region(Region.of(ossProperties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider
						.create(AwsBasicCredentials.create(ossProperties.getAccessKey(), ossProperties.getSecretKey())))
				.serviceConfiguration(S3Configuration.builder().chunkedEncodingEnabled(false)
						.pathStyleAccessEnabled(ossProperties.getPathStyleAccess()).build());
		this.s3Client = s3ClientBuilder.build();
	}
}
