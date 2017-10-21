package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.service.IUserService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 控制层：用户操作
 */

@Controller
@RequestMapping(value = "/employee")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 人员操作命令生成器
     * @param action
     * @param userIdCollection
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/commandGenerate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void userCommandGenerate(@RequestBody String userInformation){

        System.out.println("[*] userInformation: " + userInformation);

        Map<String, Object> userInformationMap = (Map<String, Object>)JSONObject.fromObject(userInformation);
        String action = (String) userInformationMap.get("action");
        List<String> userIdCollection = (List<String>) userInformationMap.get("employeeIdCollection");

        iUserService.userCommandGenerate(action, userIdCollection);

    }

    /**
     * 测试接口
     */
    @ResponseBody
    @RequestMapping(value = "/abc", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void test(){

        System.out.println("[*] 测试接口");
    }

}
