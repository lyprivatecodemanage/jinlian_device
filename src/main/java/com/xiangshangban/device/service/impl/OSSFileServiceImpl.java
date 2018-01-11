package com.xiangshangban.device.service.impl;

import com.xiangshangban.device.bean.OSSFile;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.OSSFileUtil;
import com.xiangshangban.device.common.utils.PropertiesUtils;
import com.xiangshangban.device.dao.ImagesMapper;
import com.xiangshangban.device.dao.OSSFileMapper;
import com.xiangshangban.device.service.OSSFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OSSFileServiceImpl implements OSSFileService {

	@Autowired
	OSSFileMapper oSSFileMapper;

	@Autowired
	ImagesMapper imagesMapper;

	
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

	/**
	 *
	 * @param customerId   公司ID
	 * @param directory 存储模块名称
	 * @param file
	 * @return
	 */
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
			//获取上传文件名称
			oSSFile.setName(file.getOriginalFilename());
			//上传时间
			oSSFile.setUploadTime(DateUtils.getDateTime());
			//公司ID
			oSSFile.setCustomerId(customerId);
			//上传用户
			oSSFile.setUploadUser(userId);
			//上传状态
			oSSFile.setStatus("0");
			oSSFile.setPath(OSSFileUtil.getFilePath(customerId, directory, key));

			oSSFileMapper.addOSSFile(oSSFile);//数据库中存储关联关系
			return oSSFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}

	/**
	 *
	 * @param customerId   公司ID
	 * @param directory 存储模块名称
	 * @param file
	 * @return
	 */
	@Override
	public OSSFile addOSSFileSysApp(String customerId, String directory, MultipartFile file) {
		try {
			OSSFile oSSFile = new OSSFile();
			//从配置文件中获取登录OSS的凭证
			String accessId = PropertiesUtils.ossProperty("accessKey");
			String accessKey = PropertiesUtils.ossProperty("securityKey");
			OSSFileUtil client  = new OSSFileUtil(accessId,accessKey );
			//String customerId = "C001";//公司编号
			String userId = "u001";//用户编号ID
			String key = client.uploadSysApp(customerId, directory, getKey(), file);//上传到OSS
			//设置文件相关信息
			oSSFile.setKey(key);
			//获取上传文件名称
			oSSFile.setName(file.getOriginalFilename());
			//上传时间
			oSSFile.setUploadTime(DateUtils.getDateTime());
			//公司ID
			oSSFile.setCustomerId(customerId);
			//上传用户
			oSSFile.setUploadUser(userId);
			//上传状态
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

	/**
	 * 模板部分上传公司Logo和由设备信息生成的二维码图片
	 * @param directory
	 * @param file
	 * @return 保存到本地的logo图片的ID
	 */
	@Override
	public String templateFileUpload(String directory, MultipartFile file,InputStream inputStream,String flag) throws IOException {
		//从配置文件中获取登录OSS的凭证
		String accessId = PropertiesUtils.ossProperty("accessKey");
		String accessKey = PropertiesUtils.ossProperty("securityKey");
		OSSFileUtil client  = new OSSFileUtil(accessId,accessKey );
		String insertResult = "";

		//以流的形式传递的文件名称
		String key = getKey();
		//上传文件的路径
		String filePath = "";
		if(file!=null){
			//以MultipartFile的类型上传文件到OSS
			filePath = client.templateUploadTransfer(directory,file,null,null);
		}
		if(inputStream!=null){
			//以流的形式上传文件到OSS
			filePath = client.templateUploadTransfer(directory,null,key,inputStream);
		}
		//保存上传的文件信息到本地仓库
		Map infoMap = new HashMap<>();
		infoMap.put("id",(imagesMapper.selectImagePrimaryKey()==null && imagesMapper.selectImagePrimaryKey().isEmpty())?"1":Integer.parseInt(imagesMapper.selectImagePrimaryKey())+1);
		/**上传的是logo的时候，使用的文件名称是用户上传的文件的真实名称（格式）、
		上传的如果是二维码的时候，采用的是流的形式，所以要生成不同的文件名称（格式），所以采用UUID*/

		infoMap.put("img_name",flag.equals("1")?file.getOriginalFilename():key+".png");
		infoMap.put("img_url","http://xiangshangban.oss-cn-hangzhou.aliyuncs.com"+"/"+filePath+"/");
		infoMap.put("img_type",flag.equals("1")?"logo":"qr_code");
		infoMap.put("relate","");
		infoMap.put("ripple_color","");

		int insert = imagesMapper.insertIntoImageInfo(infoMap);
		if(insert>0){
			//获取保存的logo图片的ID
			insertResult = imagesMapper.selectImagePrimaryKey();
		}
		return insertResult;
	}


	/*****************************************************************************
	 *@TODO 设备部分上传升级包和应用包
	 * @param directory 保存资源的路径
	 * @param file 要上传的资源
	 * @return 上传文件后的全路径
	 */
	@Override
	public String devicePackageUpload(String directory, MultipartFile file, String fileType) throws IOException {
		//从配置文件中获取登录OSS的凭证
		String accessId = PropertiesUtils.ossProperty("accessKey");
		String accessKey = PropertiesUtils.ossProperty("securityKey");
		OSSFileUtil client  = new OSSFileUtil(accessId,accessKey );
		//定义上传后的完整路径（保存本地数据库的时候使用）
		String wholePath = client.devicePackageUploadTransfer(directory,file,fileType);
		return wholePath;
	}
}
