package com.xiangshangban.device.common.utils;

/**
 * Created by liuguanglong on 2017/10/20.
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class CalendarUtil {
    /**
     * 返回星期几
     * @param dayOfWeek
     * @return
     */
    public static String getWeek(int dayOfWeek){
        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        return weekDays[ dayOfWeek - 1 ];
    }

    /**
     * 根据两个时间返回显示格式
     * @param begin
     * @param end
     * @return
     */
    public static String getFormatTime(String begin,String end){
        if( StringUtils.isEmpty(end)){
            return begin.substring(0, 16);
        }else{
            if(begin.equals(end)){
                return begin.substring(0, 16);
            }else if(begin.substring(0, 10).equals(end.substring(0, 10))){
                return begin.substring(0, 10)+ " " +begin.substring(11, 16)+"~"+end.substring(11, 16);
            }else{
                return begin.substring(0, 16)+"~"+end.substring(0, 16);
            }
        }

    }

    /**
     * 或取上个月第一天日期yyyy-MM-dd
     * @return
     */
    public static String getLastMonthFirstDate(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //取得上一个月时间
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-1);
        String lastMonth= sdf.format(calendar.getTime());
        return lastMonth+"-01";
    }
    /**
     * 获取本月1日日期
     * @return
     */
    public static String getCurrentMonthFirstDate() {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return sdf.format(calendar.getTime());
    }
    /**
     * 获取下月1日日期
     * @return
     */
    public static String getNextMonthFirstDate() {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        return sdf.format(calendar.getTime());
    }
    /**
     * 或取上个月最后一天日期yyyy-MM-dd
     * @return
     */
    public static String getLastMonthEndDate(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //取得上一个月时间
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-1);
        String lastMonth= sdf.format(calendar.getTime());
        int num = calendar.getActualMaximum(Calendar.DATE);
        return lastMonth+"-"+num;
    }
    /**
     * 或取上个月月份
     * @return
     */
    public static String getLastMonth(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //取得上一个月时间
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-1);
        String lastMonth= sdf.format(calendar.getTime());
        return lastMonth;
    }
    /**
     * 或取当前日期字符串
     * @return
     */
    public static String getCurrentDate(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
    /**
     * 或取当前时间字符串
     * @return
     */
    public static String getCurrentTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
    /**
     * 或取本月最后一天日期yyyy-MM-dd
     * @return
     */
    public static int getCurrentMaxDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH));
        int num = calendar.getActualMaximum(Calendar.DATE);
        return num;
    }
    /**
     * 或取某一年的某月最后一天日期yyyy-MM-dd
     * @param month 月份  格式：yyyy-MM
     * @return
     */
    public static int getCurrentMaxDate(String month){
        SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(fomat.parse(month+"-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH));
        int num = calendar.getActualMaximum(Calendar.DATE);
        return num;
    }
    /**
     * 或取某一年的某月的前一个月yyyy-MM
     * @param month 月份  格式：yyyy-MM
     * @return
     */
    public static String getLastMonth(String month){
        SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(fomat.parse(month+"-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-1);
        return fomat.format(calendar.getTime());
    }
    /**
     * 或取某个日期的前一天日期(默认返回当前日期前一天)
     * @param date 设定日期字符串
     * @return
     */
    public static String getLastDayDate(String date){
        SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(fomat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)-1);
        return fomat.format(calendar.getTime());
    }
    /**
     * 两个日期相差的月份
     * @param date1 较大日期
     * @param date2 较小日期
     * @return
     */
    public static int monthOfDate(String date1, String date2){//date1>=date2
        int month=0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        try {
            calendar1.setTime(df.parse(date1));
            calendar2.setTime(df.parse(date2));
            int dateY1=calendar1.get(Calendar.YEAR);
            int dateM1=calendar1.get(Calendar.MONTH);
            int dateDay1=calendar1.get(Calendar.DATE);
            int dateY2=calendar2.get(Calendar.YEAR);
            int dateM2=calendar2.get(Calendar.MONTH);
            int dateDay2=calendar2.get(Calendar.DATE);
            if(dateY1-dateY2>1){
                if(dateM1-dateM2>=0){
                    if((dateM1-dateM2==0) && (dateDay1-dateDay2<0)){
                        month=(dateY1-dateY2-1)*12+dateM1+(11-dateM2);
                    }else{
                        month=(dateY1-dateY2)*12+dateM1-dateM2;
                    }
                }else{
                    month=(dateY1-dateY2-1)*12+dateM1+(11-dateM2);
                }
            }else{
                if(dateM1-dateM2>=0){
                    if(dateM1-dateM2==0){
                        month=0;
                    }else{
                        month=dateM1-dateM2;
                    }
                }else{
                    month=dateM1-dateM2;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return month;
    }
    /**
     * 两个日期相差的星期数
     * @param date1 较大日期
     * @param date2 较小日期
     * @return
     */
    public static int weekOfDate(String date1, String date2){//date1>=date2
        int week = dayOfDate(date1, date2)/7;
        return week;
    }
    /**
     * 两个日期相差的天数
     * @param date1 较大日期
     * @param date2 较小日期
     * @return
     */
    public static int dayOfDate(String date1, String date2){//date1>=date2
        long day=0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            day=(df.parse(date1).getTime()-df.parse(date2).getTime())/1000/60/60/24;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (int) day;
    }
    /**
     * 两个日期相差的小时数
     * @param date1 较大日期
     * @param date2 较小日期
     * @return
     */
    public static int hourOfTime(String date1, String date2){//date1>=date2
        long hour=0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            hour=(df.parse(date1).getTime()-df.parse(date2).getTime())/1000/60/60;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (int) hour;
    }
    /**
     * 两个日期相差的小时数
     * @param date1 较大日期
     * @param date2 较小日期
     * @return
     */
    public static int minuteOfTime(String date1, String date2){//date1>=date2
        long minute=0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            minute=(df.parse(date1).getTime()-df.parse(date2).getTime())/1000/60;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (int) minute;
    }
    /**
     * 查看time2是否在time1后的24小时内
     * @param time1
     * @param time2
     * @return boolean
     */
    public static boolean isIn24Hour(String time1, String time2){
        long hour=24*1000*60*60+1;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            hour=(df.parse(time2).getTime()-df.parse(time1).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(hour<=24*1000*60*60){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 获取一定长度时间之后的时间
     * @param distance
     * @param type Calendar.MONTH Calendar.YEAR Calendar.DATE Calendar.MINUTE Calendar.SECOND
     * @return
     */
    public static String getTimeAfterNow(int distance,int type) {
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar now = Calendar.getInstance();
        now.set(type, now.get(type) + distance);
        return formatter.format(now.getTime());
    }

    /**
     * 获取一定长度时间之后的时间
     * @param distance
     * @param type Calendar.MONTH Calendar.YEAR Calendar.DATE Calendar.MINUTE Calendar.SECOND
     * @return
     */
    public static long getLongAfterNow(int distance,int type) {
        Calendar now = Calendar.getInstance();
        now.set(type, now.get(type) + distance);
        return now.getTimeInMillis();
    }
    /**
     * 获得当前年份
     * @return
     */
    public static String getCurentYear(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy");
        return sdf.format(new Date());
    }
    /**
     * 获得当前年月，格式yyyy-MM
     * @return
     */
    public static String getCurrentMonth() {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        return sdf.format(new Date());
    }

    /**
     * 日期转换：将形如“2016-01-01”转成“2016年1月1日”
     * @param sourceDate 原始日期串
     * @param split 分隔符
     * @return
     */
    public static String getDateString(String sourceDate, String split){
        String [] dateArr = sourceDate.split(split);
        return dateArr[0]+"年"+Integer.parseInt(dateArr[1])+"月"+Integer.parseInt(dateArr[2])+"日";
    }
    /**
     * 获取指定日期在当前日期之前的时间差，如：刚刚，n分钟前，n天前，n月前，n年前
     * @param sourceTime 原始时间串
     * @param format 字符串格式  如：sourceDate="2016-05-26 00:00:00"时format="yyyy-MM-dd HH:mm:ss"
     * @return 小于当前时间：返回  刚刚/n分钟前/n天前/n月前/n年前
     */
    public static String getTimeBefore(String sourceTime, String format){
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String curRentTime = formatter.format(System.currentTimeMillis());
        int minute = minuteOfTime(curRentTime,sourceTime);
        if(minute==0){
            return "刚刚";
        }
        int hour = hourOfTime(curRentTime,sourceTime);
        if(hour==0){
            return minute+"分钟前";
        }
        int chaDay = dayOfDate(curRentTime,sourceTime);
        if(chaDay==0){
            return hour+"小时前";
        }

        int chaMon = monthOfDate(curRentTime,sourceTime);
        if(chaMon==0){
            return chaDay+"天前";
        }
        if(chaMon>=12){
            return chaMon/12+"年前";
        }else{
            return chaMon+"月前";
        }

    }
    /**
     * 比较某个时间点之后的一段时间的时间点是否大于当前时间点
     * @param time 某个时间点
     * @param type 距离单位Calendar.MONTH Calendar.YEAR Calendar.DATE Calendar.MINUTE Calendar.SECOND
     * @param distance 距离
     * @return true:小于，
     */
    public static boolean compareTimeWithNow(String time, int type, int distance){
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter1=new SimpleDateFormat("yyyy-MM-dd");
        Calendar now = Calendar.getInstance();
        String currentDate = formatter1.format(now.getTime());
        String compareTime = currentDate+" "+time;
        try {
            Date date = formatter.parse(compareTime);
            Calendar compare = Calendar.getInstance();
            compare.setTime(date);
            compare.set(type, compare.get(type) + distance);
            return now.getTimeInMillis()<compare.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 判断某个日期是否等于当前日期
     * @param dateS
     * @return true:是/false：否/error：转换出错
     */
    public static String isToday(String dateS){
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
        try {
            dateS = formatter.format(formatter.parse(dateS));
        } catch (ParseException e) {
            return "error";
        }
        String today = formatter.format(new Date());
        return today.equals(dateS)?"true":"false";
    }
    /**
     * 两个时间long值比较大小
     * @param newTime 新的时间，格式：yyyy-MM-dd HH:mm:ss
     * @param oldTime 老的时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 若newTime大于oldTime则true，否则false
     */
    public static boolean compareTime(String newTime, String oldTime){
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(newTime).getTime()>formatter.parse(oldTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 测试方法
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(CalendarUtil.getTimeBefore("2017-04-10 11:25:23", "yyyy-MM-dd HH:mm:ss"));
        //System.out.println("zhang、".substring(0, "zhang、".length()-1));
    }
}
