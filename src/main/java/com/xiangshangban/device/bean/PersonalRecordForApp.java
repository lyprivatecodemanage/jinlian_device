package com.xiangshangban.device.bean;

/**
 * author :Yonghui Wang
 * date: 2017/12/6 20:10
 * describe: TODO 个人打卡记录实体类（APP使用）
 *
 * {
         "message":"数据请求成功",
         "returnCode":"3000",
         "data":[
             {
             "door_name":"博瑞3号门",
             "record_date":"2017-12-06 18:00:00",
             "door_id":"18",
             "openType":"指纹"
             }
         ]
    }
 */
public class PersonalRecordForApp {
    private String doorName;
    private String recordDate;
    private String doorId;
    private String openType;

    public String getDoorName() {
        return doorName;
    }

    public void setDoorName(String doorName) {
        this.doorName = doorName;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public String getDoorId() {
        return doorId;
    }

    public void setDoorId(String doorId) {
        this.doorId = doorId;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    @Override
    public String toString() {
        return "PersonalRecordForApp{" +
                "doorName='" + doorName + '\'' +
                ", recordDate='" + recordDate + '\'' +
                ", doorId='" + doorId + '\'' +
                ", openType='" + openType + '\'' +
                '}';
    }
}
