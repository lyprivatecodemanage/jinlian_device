package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.OSSFileUtil;
import com.xiangshangban.device.common.utils.ReturnCodeUtil;
import com.xiangshangban.device.dao.DeviceMapper;
import com.xiangshangban.device.dao.DeviceUpdatePackAppMapper;
import com.xiangshangban.device.dao.DeviceUpdatePackSysMapper;
import com.xiangshangban.device.dao.EmployeeMapper;
import com.xiangshangban.device.service.OSSFileService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@RestController
@RequestMapping(value = "/oss")
public class OSSController {

	@Autowired
	OSSFileService oSSFileService;

	@Autowired
	DeviceUpdatePackSysMapper deviceUpdatePackSysMapper;

	@Autowired
	DeviceUpdatePackAppMapper deviceUpdatePackAppMapper;

	@Autowired
	DeviceMapper deviceMapper;

	@Autowired
	EmployeeMapper employeeMapper;

	/**
	 * 上传文件到OSS
	 * @param file
	 * @param companyId
	 * @param funcDirectory 存储模块名称
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/upload",method= RequestMethod.POST)
	public String appUpload(@RequestParam(value="file") MultipartFile file,
							@RequestParam(value = "companyId") String companyId,
                            @RequestParam(value="funcDirectory") String funcDirectory){
		String token = "未使用";
//		String customerId = "C001";//公司编号，此编号实际应用时，应根据token去查询
		String customerId = companyId;
//		funcDirectory = "portrait";//portrait目录存储员工头像
		if(StringUtils.isNotEmpty(token)){
	        if(!file.isEmpty()){
	        	try {
					OSSFile ossFile = oSSFileService.addOSSFile(customerId, funcDirectory, file);
					//System.out.println(JSON.toJSONString(ossFile));
					System.out.println("*********上传文件成功**********");
					return JSON.toJSONString(ossFile);
				}catch (Exception e){
	        		e.printStackTrace();
					System.out.println("*********上传文件失败**********");
				}
	        }
		}
        return null;
    }

	/**
	 * 上传文件到OSS
	 * @param file
	 * @param companyId
	 * @param funcDirectory 存储模块名称
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/uploadSysApp",method= RequestMethod.POST)
	public String uploadSysApp(@RequestParam(value="file") MultipartFile file,
							@RequestParam(value = "companyId") String companyId,
							@RequestParam(value="funcDirectory") String funcDirectory){
		String token = "未使用";
//		String customerId = "C001";//公司编号，此编号实际应用时，应根据token去查询
		String customerId = companyId;
//		funcDirectory = "portrait";//portrait目录存储员工头像
		if(StringUtils.isNotEmpty(token)){
			if(!file.isEmpty()){
				try {
					OSSFile ossFile = oSSFileService.addOSSFileSysApp(customerId, funcDirectory, file);
					//System.out.println(JSON.toJSONString(ossFile));
					System.out.println("*********上传文件成功**********");
					return JSON.toJSONString(ossFile);
				}catch (Exception e){
					e.printStackTrace();
					System.out.println("*********上传文件失败**********");
				}
			}
		}
		return null;
	}

	/**
	 * 根据文件名获取全路径
	 * @param key
	 * @param ACCESS_TOKEN
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/path.shtml",produces = "application/json;charset=UTF-8",method= RequestMethod.GET)
	public String appGetPath(String key, @RequestHeader String ACCESS_TOKEN, @RequestParam String funcDirectory){
		String token = ACCESS_TOKEN;
		String customerId = "C001";//公司编号，此编号实际应用时，应根据token去查询
		return OSSFileUtil.getFilePath(customerId, funcDirectory, key);
	}

	/**
	 * 设备上传应用升级包、警报图片及其它未知文件专用接口
	 * @param file
	 * @param fileType
	 * @param appVersion
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/deviceOssUpdate",method= RequestMethod.POST)
	public String deviceOssUpdate(@RequestParam(value="file") MultipartFile file,
								  @RequestParam(value = "fileType") String fileType,
								  @RequestParam(value = "appVersion") String appVersion,
								  @RequestParam(value = "deviceId") String deviceId){

		String funcDirectory = "";
		String ossFile = "";

		//判断上传的文件应该放在哪个文件夹
		if (fileType.equals("system")){
			//系统升级的文件放在这个文件夹下
			funcDirectory = "device/update/system/" + CalendarUtil.getCurrentTime();

			try {
				//上传文件到oss
				ossFile = appUpload(file, "", funcDirectory);
				//提取文件上传返回的数据
				Map<String, String> ossFileResultMap = net.sf.json.JSONObject.fromObject(ossFile);
				String key = ossFileResultMap.get("key");
				String name = ossFileResultMap.get("name");
				String path = ossFileResultMap.get("path");
				String createTime = DateUtils.getDateTime();

				DeviceUpdatePackSys deviceUpdatePackSys = new DeviceUpdatePackSys();
				deviceUpdatePackSys.setNewSysVerion(name);
				deviceUpdatePackSys.setPath(path);
				deviceUpdatePackSys.setCreateTime(createTime);
				deviceUpdatePackSys.setFileKey(key);

				//保存系统文件的信息到系统升级包信息表里
				DeviceUpdatePackSys deviceUpdatePackSysExist = deviceUpdatePackSysMapper.selectByPrimaryKey(name);
				if (deviceUpdatePackSysExist == null){
					deviceUpdatePackSysMapper.insertSelective(deviceUpdatePackSys);
				}else {
					deviceUpdatePackSysMapper.updateByPrimaryKeySelective(deviceUpdatePackSys);
				}

			}catch (Exception e){
				System.out.println("上传系统升级包文件异常");
			}

		}else if (fileType.equals("application")){
			//应用升级的文件放在这个文件夹下
			funcDirectory = "device/update/application/"+appVersion;

			try {
				//上传文件到oss
				ossFile = appUpload(file, "", funcDirectory);

				//提取文件上传返回的数据
				Map<String, String> ossFileResultMap = net.sf.json.JSONObject.fromObject(ossFile);
				String key = ossFileResultMap.get("key");
				String name = ossFileResultMap.get("name");
				String path = ossFileResultMap.get("path");
				String createTime = DateUtils.getDateTime();

				DeviceUpdatePackApp deviceUpdatePackApp = new DeviceUpdatePackApp();
				deviceUpdatePackApp.setAppName(name);
				deviceUpdatePackApp.setVersion(appVersion);
				deviceUpdatePackApp.setPath(path);
				deviceUpdatePackApp.setCreateTime(createTime);
				deviceUpdatePackApp.setFileKey(key);

				//保存应用文件的信息到应用升级包信息表里
				DeviceUpdatePackApp deviceUpdatePackAppExist = deviceUpdatePackAppMapper.selectByPrimaryKey(name);
				if (deviceUpdatePackAppExist == null){
					deviceUpdatePackAppMapper.insertSelective(deviceUpdatePackApp);
				}else {
					deviceUpdatePackAppMapper.updateByPrimaryKeySelective(deviceUpdatePackApp);
				}

			}catch (Exception e){
				System.out.println("上传应用升级包文件异常");
			}

		}else if (fileType.equals("deviceRecordImg")){

			if (org.apache.commons.lang3.StringUtils.isNotEmpty(deviceId)){
				//获取设备所属的公司id
				String companyId = deviceMapper.selectByPrimaryKey(deviceId).getCompanyId();

				//获取公司编号

				List<String> employeeList = employeeMapper.selectCompanyNoByCompanyId(companyId);
				String companyNo = "unknowCompanyNo";

				if (employeeList.size() > 0){
					if (org.apache.commons.lang3.StringUtils.isNotEmpty(employeeList.get(0))){
						companyNo = employeeList.get(0);
					}
				}

				//上传的设备记录图片的文件放在这个文件夹下
				funcDirectory = "device/"+deviceId;
				//上传文件到oss
				ossFile = appUpload(file, companyNo, funcDirectory);
			}else {
				System.out.println("上传文件时所传设备id为空");
			}

		}else if (fileType.equals("other")){

			//其它的的文件放在这个文件夹下
			funcDirectory = "device/update/other";
			//上传文件到oss
			ossFile = appUpload(file, "", funcDirectory);

		}else {

			//提示文件类型输入错误
			ossFile = "文件类型输入错误，目前系统升级包文件第二个参数需要输入system," +
					"应用升级包文件第二个参数需要输入application，" +
					"设备上传的记录照片第二个参数需要输入deviceRecordImg，" +
					"其它文件第二个参数需要输入other，请重新输入";

		}
		return ossFile;
	}

	/**
	 * 设备上传系统升级包文件专用接口
	 * @param file1
	 * @param file2
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/deviceOssUpdateSys",method= RequestMethod.POST)
	public String deviceOssUpdateSys(@RequestParam(value="file1") MultipartFile file1,
									 @RequestParam(value="file2") MultipartFile file2){

		String funcDirectory = "";
		String ossFile1 = "";
		String ossFile2 = "";
		String time = CalendarUtil.getCurrentTime();

		//系统升级的文件放在这个文件夹下
		funcDirectory = "device/update/system/" + time;

		if (null != file1 && null != file2){
			try {
				//上传系统文件1
				ossFile1 = appUpload(file1, "", funcDirectory);
				//提取文件上传返回的数据
				Map<String, String> ossFileResultMap1 = net.sf.json.JSONObject.fromObject(ossFile1);
				String key1 = ossFileResultMap1.get("key");
				String name1 = ossFileResultMap1.get("name");
				String path1 = ossFileResultMap1.get("path");

				DeviceUpdatePackSys deviceUpdatePackSys1 = new DeviceUpdatePackSys();
				deviceUpdatePackSys1.setNewSysVerion(name1);
				deviceUpdatePackSys1.setPath(path1);
				deviceUpdatePackSys1.setCreateTime(time);
				deviceUpdatePackSys1.setFileKey(key1);

				//保存系统文件的信息到系统升级包信息表里
				deviceUpdatePackSysMapper.insertSelective(deviceUpdatePackSys1);

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////

				//上传系统文件2
				ossFile2 = appUpload(file2, "", funcDirectory);
				//提取文件上传返回的数据
				Map<String, String> ossFileResultMap2 = net.sf.json.JSONObject.fromObject(ossFile2);
				String key2 = ossFileResultMap2.get("key");
				String name2 = ossFileResultMap2.get("name");
				String path2 = ossFileResultMap2.get("path");

				DeviceUpdatePackSys deviceUpdatePackSys2 = new DeviceUpdatePackSys();
				deviceUpdatePackSys2.setNewSysVerion(name2);
				deviceUpdatePackSys2.setPath(path2);
				deviceUpdatePackSys2.setCreateTime(time);
				deviceUpdatePackSys2.setFileKey(key2);

				//保存系统文件的信息到系统升级包信息表里
				deviceUpdatePackSysMapper.insertSelective(deviceUpdatePackSys2);

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("上传系统升级包文件异常");
			}
		}else {
			ossFile1 = "文件不能为空";
		}

		return ossFile1+"\n"+ossFile2;
	}

	// List里面的多个对象根据某个字段的值进行比较排序
	public static void main(String[] args) {
		List<Device> deviceList = new ArrayList<Device>();
		Device device1 = new Device();
		Device device2 = new Device();
		Device device3 = new Device();
		device1.setDeviceId("2");
		device1.setDeviceName("好");
		device3.setDeviceId("3");
		device3.setDeviceName("啊");
		device2.setDeviceId("1");
		device2.setDeviceName("你");
		deviceList.add(device1);
		deviceList.add(device3);
		deviceList.add(device2);

		System.out.println(JSON.toJSONString(deviceList));
		Collections.sort(deviceList, new DeviceIdComparator());
		System.out.println(JSON.toJSONString(deviceList));
	}

	static class DeviceIdComparator implements Comparator{
		public int compare(Object object1, Object object2){
			Device p1 = (Device) object1; // 强制转换
			Device p2 = (Device) object2;
			return new Double(p1.getDeviceId()).compareTo(new Double(p2.getDeviceId()));
		}
	}


	/**
	 * 测试下方templateOSSUpload方法的入口
	 * @return
	 */
	/*@GetMapping("/uploadTest")
	public String uploadTest(){
		//创建二维码对象
		TwoDimensionCode twoDimensionCode = new TwoDimensionCode();
		//根据设备信息生成二维码，写入指定的输出流中
		BufferedImage qrCodeBufferImage = twoDimensionCode.getQRCodeBufferImage("上传二维码图片到OSS测试");
		//创建字节数组输出流
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//返回码
		String backCode = "";
		try {
			ImageIO.write(qrCodeBufferImage, "png", os);
			InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
			backCode = templateOSSUpload(null, "1", inputStream, "2");
		}catch(Exception e){
			e.printStackTrace();
		}
		return backCode;
	}*/

	/**
	 * TODO 模板主题部分：
	 * 上传Logo和二维码
	 * @param file
	 * @param templateId
	 * @return 添加logo和二维码后，生成的主键的ID
	 */
	@PostMapping("/templateOSSUpload")
	public String templateOSSUpload(@RequestParam(name = "file",required = false) MultipartFile file,
									@RequestParam(name = "templateId") String templateId,
									@RequestParam(name = "inputStream",required = false)InputStream inputStream,
									@RequestParam(name = "flag") String flag) throws IOException {
		String funcDirectory = "";
		//根据使用的标准模板的id，设定保存的文件夹
		funcDirectory = "device/template/template"+templateId;
		String returnInfo = "";
		int logoQrCodeId = 0;
		try {
			if(file!=null){
				//使用MultipartFile
				logoQrCodeId = oSSFileService.templateFileUpload(funcDirectory,file,null,flag);
			}
			if(inputStream!=null){
				//以输入流的形式上传文件
				logoQrCodeId = oSSFileService.templateFileUpload(funcDirectory,null,inputStream,flag);
			}
			if(logoQrCodeId>0){
				System.out.println("*********上传文件成功,并保存到本地**********");
				returnInfo = String.valueOf(logoQrCodeId);
			}
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("*********上传文件失败**********");
			returnInfo = String.valueOf(logoQrCodeId);
		}
		return returnInfo;
	}

	/*******************************************************************************
	 * 					@TODO 设备端：上传应用包和升级包
	 *******************************************************************************/
	@PostMapping("/devicePackageOSSUpload")
	public String deviceUploadPackage(@RequestParam("versionCode") String versionCode,
									  @RequestParam("uploadResource")MultipartFile uploadResource,
									  @RequestParam("fileType") String fileType,
									  @RequestParam("employeeId") String employeeId) throws IOException {
		/**
		 * fileType：facePhoto时为人脸图片上传，为任意其它字符串时为系统升级包上传
		 */
		//验证参数的完整性
		Map result = new HashMap();
		if((versionCode==null || versionCode.isEmpty()) ||(uploadResource==null || uploadResource.isEmpty())){
			//参数异常
			result = ReturnCodeUtil.addReturnCode(1);
		}else{

			String funcDirectory = "";
			//判断文件类型
			if ("facePhoto".equals(fileType)){
				funcDirectory = "FacePhotoLibrary/"+employeeId;
			}else {
				//设置上传文件保存的路径
				funcDirectory = "device/update/system/"+versionCode;
			}

			//上传
			String filePath = oSSFileService.devicePackageUpload(funcDirectory,uploadResource,fileType);
			if(filePath.trim().equals("false")){
				//上传失败
				result = ReturnCodeUtil.addReturnCode(Boolean.valueOf(filePath.trim()),"上传升级包（应用包）失败");
			}else{
				//上传成功，保存信息到本地
				DeviceUpdatePackSys deviceUpdatePackSys = new DeviceUpdatePackSys();
				deviceUpdatePackSys.setNewSysVerion(versionCode);
				deviceUpdatePackSys.setPath(filePath.trim());
				deviceUpdatePackSys.setCreateTime(DateUtils.getDateTime());

				//根据路径查询当前本地数据库中是否已经存在该资源
				String status = deviceUpdatePackSysMapper.verifyWhetherExistsResource(filePath.trim());
				if(status==null || "".equals(status)){
					//添加新的数据
					int insertResult = deviceUpdatePackSysMapper.insert(deviceUpdatePackSys);

					if(insertResult>0){
						result = ReturnCodeUtil.addReturnCode(true,"上传文件成功，并保存信息至本地数据库");
					}else{
						result = ReturnCodeUtil.addReturnCode(false,"上传文件成功，保存至本地数据库失败");
					}
				}else{
					//根据路径更新操作时间
					Map map = new HashMap();
					map.put("createTime",DateUtils.getDateTime());
					map.put("path",filePath.trim());
					deviceUpdatePackSysMapper.updateOperateTime(map);
					result = ReturnCodeUtil.addReturnCode(true,"上传文件成功，并保存信息至本地数据库");
				}
			}
		}
		return JSONObject.toJSONString(result);
	}
}

