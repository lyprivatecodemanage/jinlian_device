package com.xiangshangban.controller;

import com.xiangshangban.service.IUserService;
import com.xiangshangban.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @ResponseBody
    @RequestMapping(value = "/abc", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void test(){

        String message = UserServiceImpl.sendRequet("http://192.168.0.108:8072/EmployeeController/findByAllEmployee", "");
        System.out.println(message);

        System.out.println("[*] send: 已发出请求");
    }

}
