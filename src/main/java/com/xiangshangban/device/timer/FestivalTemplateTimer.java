package com.xiangshangban.device.timer;

import com.xiangshangban.device.bean.Festival;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.service.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * author : Administrator
 * date: 2017/11/23 11:06
 * describe: TODO 定时下发节日节气模板
 */
@Component
public class FestivalTemplateTimer {

    @Autowired
    private ITemplateService iTemplateService;

    //定义定时器（每天早上3点触发）
   private static final String FESTIVAL_CHECK_TIME = "0 0 3 * * ?";

//    @Scheduled(cron = FESTIVAL_CHECK_TIME)
    public void confirmFestival(){
        //判断今天是否是节日
        Festival festival = iTemplateService.verifyCurrentDate(DateUtils.getDate());
        if(festival!=null){
            //触发下发节假日的接口
            boolean result = iTemplateService.addFestivalTemplate(festival.getFestivalName());
            if(result){
                System.out.println("********************=========《下发节假日模板成功》===========***********************");
            }else{
                System.out.println("********************=========《下发节假日模板失败》===========***********************");
            }
        }else{
            System.out.println("========非节日节气无模板下发==========");
        }
    }
}
