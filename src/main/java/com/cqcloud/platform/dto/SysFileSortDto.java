package com.cqcloud.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件排序
 *
 * @author weimeilayer@gmail.com
 * @date 💓💕2023年5月22日🐬🐇💓💕
 */
@Data
@Schema(description = "上传文件排序信息")
public class SysFileSortDto implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "主键")
	private String id;

	@Schema(description = "排序")
	private Integer sort;

}
