package com.xiangshangban.device.bean;

import java.io.Serializable;

public class OSSFile implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;//OSS上文件存储的key，如：123456.png或123456.zip等
	private String name;//文件名，如：123456.png或123456.zip等
	private String uploadTime;//上传时间，如：2017-01-01 10:00:00
	private String status;//有效状态，标记删除：0，有效，1：无效
	private String uploadUser;//操作人ID
	private String customerId;//公司ID
	private String path;//全路径，如：http://file.xiangshangban.com/test/data/customer001/touxiang/123456.png
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUploadUser() {
		return uploadUser;
	}
	public void setUploadUser(String uploadUser) {
		this.uploadUser = uploadUser;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	@Override
	public String toString() {
		return "OSSFile [key=" + key + ", name=" + name + ", uploadTime=" + uploadTime + ", status=" + status
				+ ", uploadUser=" + uploadUser + ", customerId=" + customerId + ", path=" + path + "]";
	}

}
