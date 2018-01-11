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
import java.util.Date;
import java.util.List;

public class OSSFileUtil {
	private static final Log LOG = LogFactory.getLog(OSSFileUtil.class);

	private final static String OSS_ENDPOINT_PRE = "xiangshangban.com";
//***************************************************************************************
	private final static String OSS_ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
	private final static String OSS_BUCKET = "xiangshangban";
//	private final static String OSS_ENDPOINT = "oss-cn-shanghai.aliyuncs.com";
//	private final static String OSS_BUCKET = "shoumy";
//***************************************************************************************
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
	public String OSSPutObjectSysApp(String customerId, String directory, String key, String extension,File file,String fileName)
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
				SYS_FILE_LOCATION + "/"+directory+"/"+fileName+"."+extension
				: USER_FILE_LOCATION+"/"+customerId + "/"+directory+ "/"+fileName+"."+extension;
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
		return fileName+"."+extension;
	}
	

	/**
	 * 设备文件上传专用
	 * @param  customerId 用户ID
	 * @param  SN 设备唯一号
	 * @param  edtion 版本号
	 * @param type 1-设备端文件 2-云端文件
	 * @param content 内容
	 * @return  MD5摘要
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
     * @param extension
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
		//截取文件后缀
		String extention = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1);
		//将multipartFile转换成file
		CommonsMultipartFile commonsMultipartFile= (CommonsMultipartFile)multipartFile;
		DiskFileItem diskFileItem = (DiskFileItem)commonsMultipartFile.getFileItem();
		File file = diskFileItem.getStoreLocation();
		return OSSPutObject(customerId,directory,key, extention,file);
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
	public String uploadSysApp(String customerId,String directory, String key,MultipartFile multipartFile) throws OSSException, ClientException, FileNotFoundException {
		initialize();
		//截取文件后缀
		String extention = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1);
		//将multipartFile转换成file
		CommonsMultipartFile commonsMultipartFile= (CommonsMultipartFile)multipartFile;
		DiskFileItem diskFileItem = (DiskFileItem)commonsMultipartFile.getFileItem();
		File file = diskFileItem.getStoreLocation();
		String fileName = multipartFile.getOriginalFilename().substring(0, multipartFile.getOriginalFilename().indexOf("."));
		return OSSPutObjectSysApp(customerId,directory,key, extention,file,fileName);
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

	/*********************************************************************
	 * TODO 活动管理部分文件上传
	 **********************************************************************/

	/**
	 *活动管理部分上传文件中转站
	 * @param directory
	 * @param multipartFile
	 */
	public String templateUploadTransfer(String directory,MultipartFile multipartFile,String key,InputStream inputStream) throws FileNotFoundException {
		initialize();
		String filePath = "";
		if(multipartFile!=null){
			//截取文件后缀
			String extention = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1);
			//获取上传文件名
			String filename = multipartFile.getOriginalFilename().substring(0,multipartFile.getOriginalFilename().lastIndexOf("."));
			//将multipartFile转换成file
			CommonsMultipartFile commonsMultipartFile= (CommonsMultipartFile)multipartFile;
			DiskFileItem diskFileItem = (DiskFileItem)commonsMultipartFile.getFileItem();
			File file = diskFileItem.getStoreLocation();
			//上传文件
			filePath = doTemplateInfoUpload(directory,filename,extention,file);
		}

		if(inputStream!=null){
			filePath = doTemplateInfoUpload(directory,"png",key,inputStream);
		}
		return filePath;
	}

	/**
	 * 上传模板相关的Logo和设备信息二维码
	 * @param directory 上传文件保存的路径
	 * @param extension 文件的后缀
	 * @param file 要上传的文件
	 */
	public String doTemplateInfoUpload(String directory,String fileName,String extension,File file) throws FileNotFoundException {
		//创建文件头对象
		ObjectMetadata objectMeta = new ObjectMetadata();
		//设置文件长度
		objectMeta.setContentLength(file.length());
		//设置文件类型
		objectMeta.setContentType(getFileType(extension));
		//创建文件流对象
		InputStream input = new FileInputStream(file);
		//文件路径(区分系统文件目录和用户文件目录)
		String filePath = SYS_FILE_LOCATION + "/"+directory+"/"+fileName+"."+extension;
		String returnFilePath = "";
		String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
				filePath="test/"+filePath;
				returnFilePath = "test/"+SYS_FILE_LOCATION + "/"+directory;
			}else{
				filePath="prod/"+filePath;
				returnFilePath = "prod/"+SYS_FILE_LOCATION + "/"+directory;
			}
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
		//上传文件
		client.putObject(OSS_BUCKET, filePath, input, objectMeta);
		//返回文件保存路径
		return returnFilePath;
	}

	/**
	 * 上传模板相关的Logo和设备信息二维码
	 * @param directory 上传文件保存的路径
	 * @param key 使用UUID生成文件名称
	 * @param input 要上传的文件流
	 */
	public String doTemplateInfoUpload(String directory, String type,String key,InputStream input) throws FileNotFoundException {
		//创建文件头对象
		ObjectMetadata objectMeta = new ObjectMetadata();
		//设置文件类型
		objectMeta.setContentType(type);
		try {
			//设置文件长度
			objectMeta.setContentLength(input.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//文件路径(区分系统文件目录和用户文件目录)
		String filePath = SYS_FILE_LOCATION + "/"+directory+"/"+key+"."+type;
		String returnFilePath = "";
		String ossEnvironment="";
		try {
			ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			if("test".equals(ossEnvironment)){
				filePath="test/"+filePath;
				returnFilePath = "test/"+SYS_FILE_LOCATION + "/"+directory;
			}else{
				filePath="prod/"+filePath;
				returnFilePath = "prod/"+SYS_FILE_LOCATION + "/"+directory;
			}
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
		//上传文件
		client.putObject(OSS_BUCKET, filePath, input, objectMeta);
		//返回文件保存路径
		return returnFilePath;
	}

	/*****************************************************************
     *						@TODO 设备部分上传应用包和升级包
	 *****************************************************************/

	/**
	 *
	 * @param directory
	 * @param multipartFile
	 * @return 上传资源的完整路径
	 * @throws FileNotFoundException
	 */
	public String devicePackageUploadTransfer(String directory,MultipartFile multipartFile,String fileType) throws FileNotFoundException {
		//初始化OssClient
		initialize();
		String filePath = "";
		if(multipartFile!=null){
			//截取文件后缀
			String extention = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1);

			String fileName = "";
			//判断文件类型
			if ("facePhoto".equals(fileType)){
				fileName = DateUtils.formatDate(new Date(), "yyyy-MM-dd-HH-mm-ss");
			}else {
				//获取上传文件名
				fileName = multipartFile.getOriginalFilename().substring(0,multipartFile.getOriginalFilename().lastIndexOf("."));
			}

			//将multipartFile转换成file
			CommonsMultipartFile commonsMultipartFile= (CommonsMultipartFile)multipartFile;
			DiskFileItem diskFileItem = (DiskFileItem)commonsMultipartFile.getFileItem();
			File file = diskFileItem.getStoreLocation();
			//上传文件(返回上传的真实路径)
			filePath = doDevicePackageUpload(directory,fileName,extention,file,fileType);
		}
		return filePath;
	}

	/**
	 * 上传设备的升级包和应用包
	 * @param directory
	 * @param fileName
	 * @param extension
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public String doDevicePackageUpload(String directory,String fileName,String extension,File file,String fileType) throws FileNotFoundException {
		//创建文件头对象
		ObjectMetadata objectMeta = new ObjectMetadata();
		//设置文件长度
		objectMeta.setContentLength(file.length());
		//设置文件类型
		objectMeta.setContentType(getFileType(extension));
		//创建文件流对象
		InputStream input = new FileInputStream(file);

		String filePath = "";
		//判读文件上传类型
		if ("facePhoto".equals(fileType)){
			filePath = USER_FILE_LOCATION + "/"+directory+"/"+fileName+"."+extension;
		}else {
			//文件路径(区分系统文件目录和用户文件目录)
			filePath = SYS_FILE_LOCATION + "/"+directory+"/"+fileName+"."+extension;
		}

		try {
			String ossEnvironment = PropertiesUtils.ossProperty("ossEnvironment");
			filePath = ("test".equals(ossEnvironment)?"test/"+filePath:"prod/"+filePath);
		} catch (IOException e) {
			LOG.info("获取OSS环境属性错误");
		}
		//上传文件
		PutObjectResult putObjectResult = client.putObject(OSS_BUCKET, filePath, input, objectMeta);
		if(putObjectResult.getETag() != null && !"".equals(putObjectResult.getETag())){
			//返回文件保存路径
			return "http://"+OSS_BUCKET+"."+OSS_ENDPOINT+"/"+filePath;
		}else{
			//上传失败
			return "false";
		}
	}
}