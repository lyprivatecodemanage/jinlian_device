package com.xiangshangban;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用程序入口
 */
@SpringBootApplication
@MapperScan("com.xiangshangban.device.mapper.*.xml")
public class DeviceApplication {

	public static void main(String[] args) {

		SpringApplication.run(DeviceApplication.class, args);
	}
}
