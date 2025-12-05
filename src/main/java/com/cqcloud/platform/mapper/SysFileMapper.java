package com.cqcloud.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqcloud.platform.dto.SysFileSelDto;
import com.cqcloud.platform.entity.SysFile;
import com.cqcloud.platform.vo.SysFileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统基础信息--文件管理信息 Mapper 接口
 *
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2023年5月20日 🐬🐇 💓💕
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {

	/**
	 * 分页查询SysFile
	 * @param dto 查询参数
	 * @return 分页列表
	 */
	IPage<SysFileVo> getSysFileVoPage(@Param("page") Page<?> page, @Param("query") SysFileSelDto dto);

}
