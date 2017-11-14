package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.OSSFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OSSFileMapper {

	public void addOSSFile(OSSFile oSSFile);
}
