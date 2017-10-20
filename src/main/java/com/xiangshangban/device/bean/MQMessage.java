package com.xiangshangban.device.bean;

import java.io.Serializable;

/**
 * Created by liuguanglong on 2017/10/18.
 */
public class MQMessage implements Serializable {

    private static final long serialVersionUID = 78765587219821L;
    private String deviceId;
    private String fileEdition;
    private String fileMd5;
    private String fileContent;
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getFileEdition() {
        return fileEdition;
    }
    public void setFileEdition(String fileEdition) {
        this.fileEdition = fileEdition;
    }
    public String getFileMd5() {
        return fileMd5;
    }
    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
    public String getFileContent() {
        return fileContent;
    }
    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
	/*@Override
	public String toString() {
		return "RabbitMQMessage [deviceId=" + deviceId + ", fileEdition=" + fileEdition + ", fileMd5=" + fileMd5
				+ "]";
	}*/

}
