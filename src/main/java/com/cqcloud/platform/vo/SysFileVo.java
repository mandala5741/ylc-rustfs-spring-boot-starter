package com.cqcloud.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统基础信息--文件管理表
 *
 * @author weimeilayer@gmail.com
 * @date 2021-12-13 16:28:32
 */
@Data
public class SysFileVo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 */
	@Schema(description = "主键ID")
	private String id;

	/**
	 * 原文件名
	 */
	@Schema(description = "原文件名")
	private String name;

	/**
	 * 分组编号，用于对应多文件
	 */
	@Schema(description = "分组编号，用于对应多文件")
	private String groupId;

	/**
	 * 文件类型
	 */
	@Schema(description = "文件类型")
	private String fileType;

	/**
	 * 文件后缀
	 */
	@Schema(description = "文件后缀")
	private String suffix;

	/**
	 * 文件大小，单位字节
	 */
	@Schema(description = "文件大小，单位字节")
	private Integer size;

	/**
	 * 预览地址
	 */
	@Schema(description = "预览地址")
	private String previewUrl;

	/**
	 * 存储类型
	 */
	@Schema(description = "存储类型")
	private String storageType;

	/**
	 * 存储地址
	 */
	@Schema(description = "存储地址")
	private String storageUrl;

	/**
	 * 桶名
	 */
	@Schema(description = "桶名")
	private String bucketName;

	/**
	 * 桶内文件名
	 */
	@Schema(description = "桶内文件名")
	private String objectName;

	/**
	 * 访问次数
	 */
	@Schema(description = "访问次数")
	private Integer visitCount;

	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sort;

	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String remarks;

	/**
	 * 逻辑删除（0：未删除；null：已删除）
	 */
	@Schema(description = "逻辑删除（0：未删除；null：已删除）")
	private String delFlag;

	/**
	 * 创建人
	 */
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 编辑人
	 */
	@Schema(description = "编辑人")
	private String updateBy;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private LocalDateTime gmtCreate;

	/**
	 * 编辑时间
	 */
	@Schema(description = "编辑时间")
	private LocalDateTime gmtModified;

	/**
	 * 所属租户
	 */
	@Schema(description = "所属租户")
	private Long tenantId;

}
