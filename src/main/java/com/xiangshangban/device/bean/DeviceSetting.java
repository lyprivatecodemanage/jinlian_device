package com.xiangshangban.device.bean;

public class DeviceSetting {
    private String deviceId;

    private String heartbeatPeriod;

    private String faceThreshold;

    private String fingerThreshold;

    private String faceDetecTime;

    private String fanOnTemp;

    private String fanOffTemp;

    private String enableSelfHelpBioRegister;

    private String enableSelfHelpCardRegister;

    private String enableTimedReboot;

    private String timedRebootPeriod;

    private String timedRebootTime;
    
  /*  private String symmetricKey;
    
    private String privateKey;
    
    private String publicKey;*/

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    public void setHeartbeatPeriod(String heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }

    public String getFaceThreshold() {
        return faceThreshold;
    }

    public void setFaceThreshold(String faceThreshold) {
        this.faceThreshold = faceThreshold;
    }

    public String getFingerThreshold() {
        return fingerThreshold;
    }

    public void setFingerThreshold(String fingerThreshold) {
        this.fingerThreshold = fingerThreshold;
    }

    public String getFaceDetecTime() {
        return faceDetecTime;
    }

    public void setFaceDetecTime(String faceDetecTime) {
        this.faceDetecTime = faceDetecTime;
    }

    public String getFanOnTemp() {
        return fanOnTemp;
    }

    public void setFanOnTemp(String fanOnTemp) {
        this.fanOnTemp = fanOnTemp;
    }

    public String getFanOffTemp() {
        return fanOffTemp;
    }

    public void setFanOffTemp(String fanOffTemp) {
        this.fanOffTemp = fanOffTemp;
    }

    public String getEnableSelfHelpBioRegister() {
        return enableSelfHelpBioRegister;
    }

    public void setEnableSelfHelpBioRegister(String enableSelfHelpBioRegister) {
        this.enableSelfHelpBioRegister = enableSelfHelpBioRegister;
    }

    public String getEnableSelfHelpCardRegister() {
        return enableSelfHelpCardRegister;
    }

    public void setEnableSelfHelpCardRegister(String enableSelfHelpCardRegister) {
        this.enableSelfHelpCardRegister = enableSelfHelpCardRegister;
    }

    public String getEnableTimedReboot() {
        return enableTimedReboot;
    }

    public void setEnableTimedReboot(String enableTimedReboot) {
        this.enableTimedReboot = enableTimedReboot;
    }

    public String getTimedRebootPeriod() {
        return timedRebootPeriod;
    }

    public void setTimedRebootPeriod(String timedRebootPeriod) {
        this.timedRebootPeriod = timedRebootPeriod;
    }

    public String getTimedRebootTime() {
        return timedRebootTime;
    }

    public void setTimedRebootTime(String timedRebootTime) {
        this.timedRebootTime = timedRebootTime;
    }

	/*public String getSymmetricKey() {
		return symmetricKey;
	}

	public void setSymmetricKey(String symmetricKey) {
		this.symmetricKey = symmetricKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
    */
}