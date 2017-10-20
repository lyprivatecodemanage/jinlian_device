package com.xiangshangban.bean;

/**
 * Created by liuguanglong on 2017/10/19.
 */

public class Command {

    private String serverId;
    private String deviceId;
    private String fileEdition;
    private String commandMode;
    private String commandType;
    private String commandTotal;
    private String commandIndex;
    private String sendTime;
    private String outOfTime;
    private String md5Check;
    private String superCmdId;
    private String subCmdId;
    private String action;
    private String actionCode;
    private String resultCode;
    private String resultMessage;
    private String data;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

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

    public String getCommandMode() {
        return commandMode;
    }

    public void setCommandMode(String commandMode) {
        this.commandMode = commandMode;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandTotal() {
        return commandTotal;
    }

    public void setCommandTotal(String commandTotal) {
        this.commandTotal = commandTotal;
    }

    public String getCommandIndex() {
        return commandIndex;
    }

    public void setCommandIndex(String commandIndex) {
        this.commandIndex = commandIndex;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getOutOfTime() {
        return outOfTime;
    }

    public void setOutOfTime(String outOfTime) {
        this.outOfTime = outOfTime;
    }

    public String getMd5Check() {
        return md5Check;
    }

    public void setMd5Check(String md5Check) {
        this.md5Check = md5Check;
    }

    public String getSuperCmdId() {
        return superCmdId;
    }

    public void setSuperCmdId(String superCmdId) {
        this.superCmdId = superCmdId;
    }

    public String getSubCmdId() {
        return subCmdId;
    }

    public void setSubCmdId(String subCmdId) {
        this.subCmdId = subCmdId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Command{" +
                "serverId='" + serverId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", fileEdition='" + fileEdition + '\'' +
                ", commandMode='" + commandMode + '\'' +
                ", commandType='" + commandType + '\'' +
                ", commandTotal='" + commandTotal + '\'' +
                ", commandIndex='" + commandIndex + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", outOfTime='" + outOfTime + '\'' +
                ", md5Check='" + md5Check + '\'' +
                ", superCmdId='" + superCmdId + '\'' +
                ", subCmdId='" + subCmdId + '\'' +
                ", action='" + action + '\'' +
                ", actionCode='" + actionCode + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", resultMessage='" + resultMessage + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
