package com.cqcloud.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author weimeilayer@gmail.com
 * @date 💓💕2023年5月22日🐬🐇💓💕
 */
@Data
@Schema(description = "上传文件信息")
public class SysFileSelDto implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "主键")
	private String id;

	@Schema(description = "文件名")
	private String name;

	@Schema(description = "文件后缀")
	private String suffix;

	@Schema(description = "文件大小")
	private Integer size;

	@Schema(description = "预览地址")
	private String url;

	@Schema(description = "排序")
	private Integer sort;

}
