package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.OSSFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OSSFileMapper {

	public void addOSSFile(OSSFile oSSFile);

	//根据文件名查找
	OSSFile selectByFileName(@Param("fileName") String fileName);
}
