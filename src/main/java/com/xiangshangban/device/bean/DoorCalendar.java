package com.xiangshangban.device.bean;

public class DoorCalendar {
    private String doorId;

    private String calendarDate;

    private String weatherOpenDoor;

    public String getDoorId() {
        return doorId;
    }

    public void setDoorId(String doorId) {
        this.doorId = doorId;
    }

    public String getCalendarDate() {
        return calendarDate;
    }

    public void setCalendarDate(String calendarDate) {
        this.calendarDate = calendarDate;
    }

    public String getWeatherOpenDoor() {
        return weatherOpenDoor;
    }

    public void setWeatherOpenDoor(String weatherOpenDoor) {
        this.weatherOpenDoor = weatherOpenDoor;
    }
}