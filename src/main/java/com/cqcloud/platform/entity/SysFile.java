package com.cqcloud.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统基础信息--文件管理表
 *
 * @author weimeilayer@gmail.com
 * @date 2021-12-13 16:28:32
 */
@Data
@TableName("public.sys_file")
@EqualsAndHashCode(callSuper = false)
@Schema(description = "系统基础信息--文件管理表")
public class SysFile extends Model<SysFile> {

	@Serial
	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private String id;

	@Schema(description = "文件名")
	private String name;

	@Schema(description = "原始文件名")
	private String original;

	@Schema(description = "分组编号，用于对应多文件")
	private String groupId;

	@Schema(description = "文件类型")
	private String fileType;

	@Schema(description = "文件后缀")
	private String suffix;

	@Schema(description = "文件大小，单位字节")
	private Integer size;

	@Schema(description = "预览地址")
	private String previewUrl;

	@Schema(description = "存储类型")
	private String storageType;

	@Schema(description = "存储地址")
	private String storageUrl;

	@Schema(description = "桶名")
	private String bucketName;

	@Schema(description = "桶内文件名")
	private String objectName;

	@Schema(description = "访问次数")
	private Integer visitCount;

	@Schema(description = "排序")
	private Integer sort;

	@Schema(description = "备注")
	private String remarks;

	@Schema(description = "所属租户")
	private Long tenantId;

	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,1:已删除,0:正常")
	private String delFlag;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "编辑人")
	private String updateBy;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime gmtCreate;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "编辑时间")
	private LocalDateTime gmtModified;

}
