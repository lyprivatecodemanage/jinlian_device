package com.xiangshangban.device.common.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {
	
	private static Resource resource;
	private static Properties props; 
	
	public static void setPropertyName( String propertyFileName ) throws IOException{
		resource = new ClassPathResource( propertyFileName );
		props = PropertiesLoaderUtils.loadProperties( resource );
	}

//	public static String  rmqProperty( String property ) throws IOException{
//		setPropertyName( "/properties/rmq.properties" );
//		return props.getProperty( property );
//	}

	public static String  ossProperty( String property ) throws IOException{
		setPropertyName( "/properties/oss.properties" );
		return props.getProperty( property );
	}
}
