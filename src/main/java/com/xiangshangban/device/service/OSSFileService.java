package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.OSSFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface OSSFileService {
	/**
	 * 上传文件
	 * @param token
	 * @param file
	 * @return
	 */
	public OSSFile addOSSFile(String customerId, String directory, MultipartFile file);
	
	/**
	 * 自动上传
	 * @param customerId
	 * @param type
	 * @param input
	 * @return
	 */
	public OSSFile autoAddOSSFile(String customerId, String directory, String type, InputStream input);
	
	/**
	 * 设备上传专用
	 * @param customerId
	 * @param SN
	 * @param edtion
	 * @param type
	 * @param content
	 * @return
	 */
	public String deviceFileUpload(String customerId, String directory, String SN, String edtion, String type, String fileMd5, byte[] content);
	
	/**
	 * 根据KEY获取文件路径
	 * @param customerId
	 * @param key
	 * @return
	 */
	public String getPathByKey(String customerId, String directory, String key);

	public OSSFile autoAddOSSFileByLength(String customerId, String directory, String string, int contentLength, InputStream inputStream);
}
