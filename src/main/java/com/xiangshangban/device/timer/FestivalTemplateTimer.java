package com.xiangshangban.device.timer;

import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.dao.TemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * author : Administrator
 * date: 2017/11/23 11:06
 * describe: TODO 定时下发节日节气模板
 */
@Component
public class FestivalTemplateTimer {

    @Autowired
    private TemplateMapper templateMapper;

    //定义定时器（每天早上3点触发）
    private static final String FESTIVAL_CHECK_TIME = "0 0 3 * * ?";

    @Scheduled(cron = FESTIVAL_CHECK_TIME)
    public void comfirmFestival(){
        //判断今天是否是节日
        System.out.println(DateUtils.getDate());
    }
}
