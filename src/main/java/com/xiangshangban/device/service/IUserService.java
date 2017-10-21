package com.xiangshangban.device.service;

import java.util.List;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 业务层：用户管理
 */

public interface IUserService {

    //人员模块命令生成器
    public void userCommandGenerate(String action, List<String> userIdCollection);

}
