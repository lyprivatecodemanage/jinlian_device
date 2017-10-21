package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.bean.Employee;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.HttpRequestFactory;
import com.xiangshangban.device.dao.DoorCmdMapper;
import com.xiangshangban.device.dao.DoorEmployeeMapper;
import com.xiangshangban.device.dao.DoorMapper;
import com.xiangshangban.device.service.IUserService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**d
 * date: 2017/10/19 10:38
 * describe: TODO 用户管理实现类
 */

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    //人员模块命令生成器
    @Override
    public void userCommandGenerate(String action, List<String> employeeIdCollection) {

        if (action.equals("UPDATE_USER_INFO")){

            for (String employeeId : employeeIdCollection) {

                //根据人员id请求单个人员信息
                String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", employeeId);
                System.out.println("[*] send: 已发出请求");
                System.out.println("[*] employeeInfo: " + employeeInfo);

                //取出需要的人员信息
                Employee employee = new Employee();
                Map<String, String> employeeInfoMap = (Map<String, String>)JSONObject.fromObject(employeeInfo);
                employee.setEmployeeId(employeeInfoMap.get("employeeId"));
                employee.setEmployeeNumber(employeeInfoMap.get("employeeNo"));
                employee.setEmployeeName(employeeInfoMap.get("employeeName"));
                employee.setEmployeeDepartmentId(employeeInfoMap.get("departmentId"));
                employee.setEmployeeDepartmentName(employeeInfoMap.get("departmentName"));
                employee.setEmployeeBirthday("");
                employee.setEmployeeEntryTime(employeeInfoMap.get("entryTime"));
                employee.setEmployeeProbationaryExpired(employeeInfoMap.get("probationaryExpired"));
                employee.setEmployeeContractExpired("");
                employee.setAdminFlag("");
                employee.setEmployeePhone(employeeInfoMap.get("employeePhone"));

                //DATA JSON字符串
                String dataJsonString = JSON.toJSONString(employee);

                //获取人员和设备关联的信息
                String doorId = doorEmployeeMapper.selectByPrimaryKey(employeeId).getDoorId();
                String deviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();

                //生成一条人员修改命令
                DoorCmd doorCmd = new DoorCmd();
                //协议格式
                doorCmd.setServerId("");
                doorCmd.setDeviceId(deviceId);
                doorCmd.setFileEdition("v1.3");
                doorCmd.setCommandMode("C");
                doorCmd.setCommandType("single");
                doorCmd.setCommandTotal("1");
                doorCmd.setCommandIndex("1");
                doorCmd.setSendTime(CalendarUtil.getCurrentTime());
                doorCmd.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
                doorCmd.setSuperCmdId("");
                doorCmd.setSubCmdId("");
                doorCmd.setAction(action);
                doorCmd.setActionCode("2001");
                doorCmd.setData(dataJsonString);
                doorCmd.setStatus("0");

                System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));

                //储存人员修改命令到命令表里
                doorCmdMapper.insert(doorCmd);

            }

        }else if (action.equals("DELETE_USER_INFO")){

            //DATA JSON字符串
            String dataJsonString = JSON.toJSONString(employeeIdCollection);

            //获取人员和设备关联的信息
            String doorId = doorEmployeeMapper.selectByPrimaryKey(employeeIdCollection.get(0)).getDoorId();
            String deviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();

            //生成一条人员删除命令
            DoorCmd doorCmd = new DoorCmd();
            //协议格式
            doorCmd.setServerId("");
            doorCmd.setDeviceId(deviceId);
            doorCmd.setFileEdition("v1.3");
            doorCmd.setCommandMode("C");
            doorCmd.setCommandType("single");
            doorCmd.setCommandTotal("1");
            doorCmd.setCommandIndex("1");
            doorCmd.setSendTime(CalendarUtil.getCurrentTime());
            doorCmd.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(), 3));
            doorCmd.setSuperCmdId("");
            doorCmd.setSubCmdId("");
            doorCmd.setAction(action);
            doorCmd.setActionCode("2002");
            doorCmd.setData(dataJsonString);
            doorCmd.setStatus("0");

            System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));

            //储存人员修改命令到命令表里
            doorCmdMapper.insert(doorCmd);

        }
    }

    public static void main(String[] args) {
        String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", "13DFF865799A42C785F33AAFDC2FDD2D");
        System.out.println("[*] send: 已发出请求");
        System.out.println(employeeInfo);
    }
}
