package com.xiangshangban.device.bean;

public class DoorTimingKeepOpen {
    private String doorId;

    private String dayOfWeek;

    private String timingOpenStartTime;

    private String timingOpenEndTime;

    public String getDoorId() {
        return doorId;
    }

    public void setDoorId(String doorId) {
        this.doorId = doorId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimingOpenStartTime() {
        return timingOpenStartTime;
    }

    public void setTimingOpenStartTime(String timingOpenStartTime) {
        this.timingOpenStartTime = timingOpenStartTime;
    }

    public String getTimingOpenEndTime() {
        return timingOpenEndTime;
    }

    public void setTimingOpenEndTime(String timingOpenEndTime) {
        this.timingOpenEndTime = timingOpenEndTime;
    }
}