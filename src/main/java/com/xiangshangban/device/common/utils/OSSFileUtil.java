package com.xiangshangban.device.common.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OSSFileUtil {
	private static final Log LOG = LogFactory.getLog(OSSFileUtil.class);
	private final static String OSS_ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
	private final static String OSS_ENDPOINT_PRE = "xiangshangban.com";
	private final static String OSS_BUCKET = "xiangshangban";
	private final static String OSS_BUCKET_PRE = "file";
	private final static String USER_FILE_LOCATION = "data";
	private final static String SYS_FILE_LOCATION = "sys";
	private  static OSSClient client;
	private  String accessId;
	private  String accessKey;
	
	public OSSFileUtil(String accessId, String accessKey){
		this.accessId = accessId;
		this.accessKey = accessKey;
	}
	
	/**
	 * 创建OSSClient对象
	 * @param endPoint
	 * @param accessId
	 * @param accessKey
	 * @return
	 */
	public void initialize(){
		if(null == client)
			client = new OSSClient(OSS_ENDPOINT, accessId, accessKey);
	}
	
    /**
     * 上传文件
     * @param customerId 公司编号。系统目录不需要指定该参数
     * @param key 文件key
     * @param directory 功能模块名
     * @param extension 扩展名
     * @param file 文件
     * @return
     * @throws OSSException
     * @throws ClientException
     * @throws FileNotFoundException
     */
	public String OSSPutObject(String customerId, String directory, String key, String extension,File file)
            throws OSSException, ClientException, FileNotFoundException {
    	//创建文件头对象
        ObjectMetadata objectMeta = new ObjectMetadata();
        //设置文件长度
        objectMeta.setContentLength(file.length());
        //设置文件类型
        objectMeta.setContentType(getFileType(extension));
        //创建文件流对象
        InputStream input = new FileInputStream(file);
        //文件路径(区分系统文件目录和用户文件目录)
        String filePath = StringUtils.isEmpty(customerId)?
        		SYS_FILE_LOCATION + "/"+directory+"/"+key+"."+extension
        		: USER_FILE_LOCATION+"/"+customerId + "/"+directory+ "/"+key+"."+extension;
        String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
            	filePath="test/"+filePath;
            }else{
            	filePath="prod/"+filePath;
            }
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
        //上传文件
        client.putObject(OSS_BUCKET, filePath, input, objectMeta);
        return key+"."+extension;
    }
	

	/**
	 * 设备文件上传专用
	 * @param String customerId 用户ID
	 * @param String SN 设备唯一号
	 * @param String edtion 版本号
	 * @param String type 1-设备端文件 2-云端文件
	 * @param byte[] content 内容
	 * @return String MD5摘要
	 * @throws OSSException
	 * @throws ClientException
	 */
	public String deviceFileUpload(String customerId,String directory,String SN,String edtion,String type,String md5,byte[] content )
            throws OSSException, ClientException {
    	//所有参数不得为空
        if( StringUtils.isNotEmpty(customerId) &&
        		StringUtils.isNotEmpty(SN) &&
        		StringUtils.isNotEmpty(edtion) &&
        		StringUtils.isNotEmpty(type)&&
        		content.length>0 ){
        	//创建文件头对象
            ObjectMetadata objectMeta = new ObjectMetadata();
            objectMeta.setContentMD5(md5);
            //文件名构建
        	String name = "device_" + SN + "_" + type + "_" + edtion;
        	//文件路径
            String filePath = USER_FILE_LOCATION+"/"+customerId + "/"+directory+"/"+name;
            String ossEnvironment="";
    		try {
    			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
    			if("test".equals(ossEnvironment)){
                	filePath="test/"+filePath;
                }else{
                	filePath="prod/"+filePath;
                }
    		} catch (IOException e) {
    			LOG.info("获取OSS环境属性错误");
    		}
            //创建文件流对象
            ByteArrayInputStream input = new ByteArrayInputStream(content);
            
            //上传文件
            PutObjectResult result = client.putObject(OSS_BUCKET, filePath, input, objectMeta);
            return result.getETag();
        }
        return null;
    }
	
	/**
	 * 自动上传用
	 * @param customerId
	 * @param type
	 * @param key
	 * @param input
	 * @return
	 */
	public String oSSPutStream(String customerId, String directory, String type,String key,InputStream input){
		String filePath = StringUtils.isEmpty(customerId)?SYS_FILE_LOCATION + "/"+directory+"/"
				: USER_FILE_LOCATION+"/"+customerId + "/"+directory+"/";
		//创建文件头对象
        ObjectMetadata objectMeta = new ObjectMetadata();
        //设置文件类型
        objectMeta.setContentType(getFileType(type));
      //请总是指定正确的content length。    修改：韦友弟    2017-01-16
        try {
			objectMeta.setContentLength(input.available());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
            	filePath="test/"+filePath;
            }else{
            	filePath="prod/"+filePath;
            }
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
		//上传文件
        client.putObject(OSS_BUCKET, filePath+key+"."+type, input, objectMeta);
        return key+"."+type;
	}

    /**
     * 设置上传文件头类型
     * @param prefix
     * @return
     */
	public String getFileType(String extension) {
    	  if(extension.equals("BMP")||extension.equals("bmp")){return "image/bmp";}  
          if(extension.equals("GIF")||extension.equals("gif")){return "image/gif";}  
          if(extension.equals("JPEG")||extension.equals("jpeg")||  
             extension.equals("JPG")||extension.equals("jpg")||     
             extension.equals("PNG")||extension.equals("png")){return "image/jpeg";}  
          if(extension.equals("HTML")||extension.equals("html")){return "text/html";}  
          if(extension.equals("TXT")||extension.equals("txt")){return "text/plain";}  
          if(extension.equals("VSD")||extension.equals("vsd")){return "application/vnd.visio";}  
          if(extension.equals("PPTX")||extension.equals("pptx")||  
              extension.equals("PPT")||extension.equals("ppt")){return "application/vnd.ms-powerpoint";}  
          if(extension.equals("DOCX")||extension.equals("docx")||  
              extension.equals("DOC")||extension.equals("doc")){return "application/msword";}  
          if(extension.equals("XML")||extension.equals("xml")){return "text/xml";}  
          if(extension.equals("MP3")||extension.equals("mp3")){return "audio/mp3";}
          if(extension.equals("AMR")||extension.equals("amr")){return "audio/amr";}
          return "text/html";
	}

	/**
	 * 删除文件
	 * @param bucketName
	 * @param key
	 * @throws OSSException
	 * @throws ClientException
	 */
	public void deleteFile(String key)
            throws OSSException, ClientException {
        client.deleteObject(OSS_BUCKET, key);
    }
	
	/**
     * 下载文件
     * @param client
     * @param bucketName
     * @param key
     * @param filename
     * @throws OSSException
     * @throws ClientException
     */
	public void downloadFile( String key, String filename)
            throws OSSException, ClientException {
        client.getObject(new GetObjectRequest(OSS_BUCKET, key),new File(filename));
    }
    
    /**
     * 获取文件路径
     * @param customerId
     * @param key
     * @return
     */
	public static String getFilePath(String customerId, String directory, String key){
		if(StringUtils.isNotEmpty(key)){
			String filePath = StringUtils.isEmpty(customerId)?SYS_FILE_LOCATION + "/"+directory+"/"
					: USER_FILE_LOCATION+"/"+customerId + "/"+directory+"/";
			String ossEnvironment="";
			try {
				ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
				if("test".equals(ossEnvironment)){
	            	filePath="test/"+filePath;
	            }else{
	            	filePath="prod/"+filePath;
	            }
			} catch (IOException e) {
				LOG.info("获取OSS环境属性错误");
			}
//	    	return "http://" +OSS_BUCKET_PRE +"." + OSS_ENDPOINT_PRE + "/"+filePath + "/" + key;
			return "http://" +OSS_BUCKET +"." + OSS_ENDPOINT + "/"+filePath + key;
		}	
		return "";	
    }
	
	/**
     * 获取文件路径
     * @param customerId
     * @param directory 
     * @return
     */
	public static List<String> getFilePathList(String customerId,String directory, List<String> keyList){
		List<String> result = new ArrayList<String>();
    	String filePath = StringUtils.isEmpty(customerId)?
    			SYS_FILE_LOCATION+"/"+directory
    			: USER_FILE_LOCATION+"/"+customerId+"/"+directory;
    	String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
            	filePath="test/"+filePath;
            }else{
            	filePath="prod/"+filePath;
            }
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
    	for(String key:keyList){
    		result.add("http://" +OSS_BUCKET_PRE +"." + OSS_ENDPOINT_PRE + "/"+filePath + "/" + key);
    	}
    	return result;
    }
	
	/**
	 * 上传
	 * @param customerId
	 * @param key
	 * @param multipartFile
	 * @return
	 * @throws OSSException
	 * @throws ClientException
	 * @throws FileNotFoundException
	 */
	 public String upload(String customerId,String directory, String key,MultipartFile multipartFile) throws OSSException, ClientException, FileNotFoundException {
		initialize();
		String extention = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1);
		CommonsMultipartFile commonsMultipartFile= (CommonsMultipartFile)multipartFile;
		DiskFileItem diskFileItem = (DiskFileItem)commonsMultipartFile.getFileItem(); 
		File file = diskFileItem.getStoreLocation();
		return OSSPutObject(customerId,directory,key, extention,file);
	 }
	 
	 /**
	  * 自动提交
	  * @param customerId
	  * @param type
	  * @param key
	  * @param input
	  * @return
	  */
	 public String autoUpload(String customerId, String type, String directory, String key,InputStream input){
		 initialize();
		 return oSSPutStream(customerId, directory, type, key, input);
	 }
	 
	/**
	 * 删除
	 * @param customerId
	 * @param key
	 */
	 public void delete(String customerId, String directory, String key) {	 	
		initialize();	   
		 //文件夹路径
	    String filePath = StringUtils.isEmpty(customerId)?
	    		SYS_FILE_LOCATION + "/"+directory+"/"
	    		: USER_FILE_LOCATION+"/"+customerId + "/"+directory+"/";
	    String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
            	filePath="test/"+filePath;
            }else{
            	filePath="prod/"+filePath;
            }
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
	    deleteFile(filePath+key);
	 }
	 	
	/**
	 * 下载
	 * @param customerId
	 * @param key
	 * @param filename
	 */
	 public void download(String customerId, String directory, String key,String filename) {	 	
		initialize();	   
		 //文件夹路径
	    String filePath = StringUtils.isEmpty(customerId)?
	    		SYS_FILE_LOCATION + "/"+directory+"/"
	    		: USER_FILE_LOCATION+"/"+customerId + "/"+directory+"/";
	    String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
            	filePath="test/"+filePath;
            }else{
            	filePath="prod/"+filePath;
            }
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
	    downloadFile(filePath+key,filename);
	 }

	public String autoUploadByLength(String customerId, String directory, String type, String key, int contentLength, InputStream input) {
		initialize();
		String filePath = StringUtils.isEmpty(customerId)?
	    		SYS_FILE_LOCATION + "/"+directory+"/"
	    		: USER_FILE_LOCATION+"/"+customerId + "/"+directory+"/";
		//创建文件头对象
        ObjectMetadata objectMeta = new ObjectMetadata();
        
        //请总是指定正确的content length。    修改：韦友弟    2017-01-16
        objectMeta.setContentLength(contentLength);
        //设置文件类型
        objectMeta.setContentType(getFileType(type));
        String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
            	filePath="test/"+filePath;
            }else{
            	filePath="prod/"+filePath;
            }
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
		//上传文件
        client.putObject(OSS_BUCKET, filePath+key+"."+type, input, objectMeta);
        return key+"."+type;
	}
}