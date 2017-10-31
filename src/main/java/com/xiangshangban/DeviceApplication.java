package com.xiangshangban;

import com.github.pagehelper.PageHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

/**
 * 应用程序入口
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.xiangshangban.device.dao")
public class DeviceApplication {

	public static void main(String[] args) {

		SpringApplication.run(DeviceApplication.class, args);
}

    @Bean
	public PageHelper pageHelper(){
		PageHelper pageHelper = new PageHelper();
		Properties properties = new Properties();
		//设置属性
		properties.setProperty("offsetAsPageNum","true");
		properties.setProperty("rowBoundsWithCount","true");
		properties.setProperty("reasonable","true");
		properties.setProperty("dialect","postgresql");

 		pageHelper.setProperties(properties);

		return pageHelper;
	}
}
