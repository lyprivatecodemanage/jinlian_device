package com.xiangshangban.device.controller;

import com.xiangshangban.device.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 控制层：用户操作
 */

@Controller
@RequestMapping(value = "/test")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @ResponseBody
    @RequestMapping(value = "/abc", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void test(String action, List<String> userIdCollection){

        iUserService.userCommandGenerate(action, userIdCollection);
    }

    /**
     * 人员操作命令生成器
     * @param action
     * @param userIdCollection
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/commandGenerate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void userCommandGenerate(String action, List<String> userIdCollection){

        iUserService.userCommandGenerate(action, userIdCollection);
    }

}
