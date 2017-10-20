package com.xiangshangban.device.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 数据访问层：用户管理
 */

@Mapper
public interface IUserDao {

    String getDeviceIdByEmployeeId();

}
