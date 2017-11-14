package com.xiangshangban.device.service.impl;

import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.dao.OSSFileMapper;
import com.xiangshangban.device.service.OSSFileService;
import com.xiangshangban.device.bean.OSSFile;
import com.xiangshangban.device.common.utils.OSSFileUtil;
import com.xiangshangban.device.common.utils.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class OSSFileServiceImpl implements OSSFileService {

	@Autowired
	OSSFileMapper oSSFileMapper;
	
	@Override
	public OSSFile autoAddOSSFile(String customerId, String directory, String type,InputStream input){
		try {
			OSSFile oSSFile = new OSSFile();
			String accessId = PropertiesUtils.ossProperty("accessKey");
			String accessKey = PropertiesUtils.ossProperty("securityKey");
			OSSFileUtil client  = new OSSFileUtil(accessId,accessKey);
			String key = client.autoUpload(customerId,type, directory, getKey(), input);
			oSSFile.setKey(key);
			oSSFile.setName(key);
			oSSFile.setCustomerId(customerId);
			oSSFile.setStatus("0");
			oSSFile.setPath(OSSFileUtil.getFilePath(customerId, directory, key));
			oSSFileMapper.addOSSFile(oSSFile);
			return oSSFile;
			
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	@Override
	public String getPathByKey(String customerId, String directory, String key) {
		return OSSFileUtil.getFilePath(customerId, directory, key);
	}
	/**
	 * 产生文件KEY
	 * @return
	 */
	private String getKey() {	
		return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
	}

	@Override
	public OSSFile addOSSFile(String customerId, String directory, MultipartFile file) {
		try {
			OSSFile oSSFile = new OSSFile();
			//从配置文件中获取登录OSS的凭证
			String accessId = PropertiesUtils.ossProperty("accessKey");
			String accessKey = PropertiesUtils.ossProperty("securityKey");
			OSSFileUtil client  = new OSSFileUtil(accessId,accessKey );
			//String customerId = "C001";//公司编号
			String userId = "u001";//用户编号ID
			String key = client.upload(customerId, directory, getKey(), file);//上传到OSS
			//设置文件相关信息
			oSSFile.setKey(key);
			oSSFile.setName(file.getOriginalFilename());
			oSSFile.setUploadTime(DateUtils.getDateTime());
			oSSFile.setCustomerId(customerId);
			oSSFile.setUploadUser(userId);
			oSSFile.setStatus("0");
			oSSFile.setPath(OSSFileUtil.getFilePath(customerId, directory, key));
			oSSFileMapper.addOSSFile(oSSFile);//数据库中存储关联关系
			return oSSFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}

	@Override
	public String deviceFileUpload(String customerId, String directory, String SN, String edtion, String type,String fileMd5, byte[] content) {
		try {
			String accessId = PropertiesUtils.ossProperty("accessKey");
			String accessKey = PropertiesUtils.ossProperty("securityKey");
			OSSFileUtil client  = new OSSFileUtil(accessId,accessKey);
			String md5 = client.deviceFileUpload(customerId, directory, SN, edtion, type,fileMd5, content);
			return md5;
		}catch(Exception e){
			return null;
		}
	}
	@Override
	public OSSFile autoAddOSSFileByLength(String customerId,  String directory,String type, int contentLength,
			InputStream input) {
		try {
			OSSFile oSSFile = new OSSFile();
			String accessId = PropertiesUtils.ossProperty("accessKey");
			String accessKey = PropertiesUtils.ossProperty("securityKey");
			OSSFileUtil client  = new OSSFileUtil(accessId,accessKey);
			String key = client.autoUploadByLength(customerId, directory,type, getKey(), contentLength, input);
			oSSFile.setKey(key);
			oSSFile.setName(key);
			oSSFile.setCustomerId(customerId);
			oSSFile.setStatus("0");
			oSSFile.setPath(OSSFileUtil.getFilePath(customerId, directory, key));
			//oSSFileMapper.addOSSFile(oSSFile);
			return oSSFile;
			
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
}
