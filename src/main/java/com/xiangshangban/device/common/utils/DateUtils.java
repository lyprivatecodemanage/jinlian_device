package com.xiangshangban.device.common.utils;

/**
 * Created by liuguanglong on 2017/10/20.
 */

import com.xiangshangban.device.common.utils.DateUtilsDependency.StringUtils;
import com.xiangshangban.device.common.utils.DateUtilsDependency.TimeConstant;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;

import java.text.*;
import java.util.*;

/**
 * 日期工具类
 *
 *
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    private static String[] parsePatterns = { "yyyy", "MM", "dd", "E", "HH:mm:ss", "yyyy.MM", "yyyy/MM", "yyyy-MM", "yyyy.MM.dd", "yyyy/MM/dd", "yyyy/M/d", "yyyy-MM-dd", "yyyy.MM.dd HH:mm",
            "yyyy/MM/dd HH:mm", "yyyy-MM-dd HH:mm", "yyyy.MM.dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSS Z", "HH:mm" };

    /**
     * 得到当前年份字符串 格式（yyyy）
     */
    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    /**
     * 判断时间1是否小于时间2
     *
     * @return true:小于，false：大于
     * @param time1
     * @param time2
     */
    public static boolean isTime1LtTime2(String time1, String time2) {
        return parseDate(time1).getTime() < parseDate(time2).getTime();
    }

    // public static void main(String[] args) throws ParseException {
    // // 获取开始和结束相差天数
    // int betdays = daysBetween("2016-10-12", "2016-10-19") + 1;
    // // 生成开始到结束之间的所有日期(yyyy-MM-dd)
    // List<String> dates =
    // getDateListFromStart(DateUtils.stringToDate("2016-10-12", "yyyy-MM-dd"),
    // betdays);
    // for (String string : dates) {
    // Date d = stringToDate(string, "yyyy-MM-dd");
    // boolean flag = d.before(new Date());
    // if (flag) {
    // System.out.println(string);
    // }
    // }
    // }

    /**
     * 得到当前月份字符串 格式（MM）
     */
    public static String getMonth() {
        return formatDate(new Date(), "MM");
    }

    /**
     * 获取指定日期的月份
     */
    public static String getDay(Date date) {
        return formatDate(date, "dd");
    }

    /**
     * 得到当天字符串 格式（dd）
     */
    public static String getDay() {
        return formatDate(new Date(), "dd");
    }

    /**
     * 得到当前星期字符串 格式（E）星期几
     */
    public static String getWeek() {
        return formatDate(new Date(), "E");
    }

    /**
     * 获取指定日期是星期几
     *
     * @param date
     * @return
     */
    public static String getWeek(Date date) {
        return formatDate(date, "E");
    }

    /**
     * 得到当前时间字符串 格式HH:mm:ss
     */
    public static String getTime() {
        return formatDate(new Date(), "HH:mm:ss");
    }

    /**
     * 获取今天任意时间段的时间
     *
     * @param startTime
     *
     * @return
     */

    public static Date getnewDateForTime(Date startTime, int Time) {
        Date date = startTime;// 取时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, Time);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 得到当前日期字符串 格式yyyy-MM-dd
     */
    public static String getDate() {
        return getDate("yyyy-MM-dd");
    }

    /**
     * 得到当前日期和时间字符串格式yy-MM-dd HH:mm
     */
    public static String getDateminutes() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm");
    }

    /**
     * 得到当前日期和时间字符串格式yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前日期字符串格式pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"等
     */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    /**
     * 得到日期时间字符串，转换格式yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到日期时间字符串，转换格式yyyy-MM-dd
     */
    public static String formatDateTime2(Date date) {
        return formatDate(date, "yyyy-MM-dd");
    }

    /**
     * 得到当前日期字符串格式pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"等pattern为空为 "yyyy-MM-dd"
     */
    public static String formatDate(Date date, Object... pattern) {
        String formatDate;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    public static String getDate(Date date) {
        return formatDate(date, "yyyy-MM-dd");
    }

    /**
     * 日期型字符串转化为日期格式 "yyyy-MM-dd" "yyyy-MM-dd HH:mm:ss" "yyyy-MM-dd HH:mm"等
     */
    public static Date parseDate(Object str) {
        if (str == null || StringUtils.isBlank(str.toString())) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     *
     * @return
     * @param start
     * @param end
     */
    public static long getMiniteBetweenTwoDate(Date start, Date end) {
        return Math.abs(end.getTime() - start.getTime()) / TimeConstant.ONE_MINITU_MILL;
    }

    /**
     * yyyy-MM-dd HH:mm:ss to date
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date stringToDateWithoutSeconds(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(date);
    }

    public static Date stringToDateWithYMD(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(date);
    }

    public static String getDateString(Date date) {
        return formatDate(date, "yyyy-MM-dd");
    }

    public static String getDateStringToYYYYMM(Date date) {
        return formatDate(date, "yyyy-MM");
    }

    public static String getTimeString(Date date) {
        return formatDate(date, "HH:mm:ss");
    }

    public static String getString(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * date To string
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String dateToString(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * String类型date to pattern(yyyy-MM-dd HH:mm /HH:mm:ss )Date
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date stringToDate(String date, String pattern) throws ParseException {
        return new SimpleDateFormat(pattern).parse(date);
    }

    /**
     * 转换为时间（天,时:分:秒.毫秒）
     *
     * @param timeMillis
     * @return
     */
    public static String formatDateTime(long timeMillis) {
        long day = timeMillis / (24 * 60 * 60 * 1000);
        long hour = timeMillis / (60 * 60 * 1000) - day * 24;
        long min = (timeMillis / (60 * 1000)) - day * 24 * 60 - hour * 60;
        long s = timeMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60;
        long sss = timeMillis - day * 24 * 60 * 60 * 1000 - hour * 60 * 60 * 1000 - min * 60 * 1000 - s * 1000;
        return (day > 0 ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
    }

    /**
     * 获取过去的分钟
     *
     * @param date
     * @return
     */
    public static long pastMinutes(Date date) {
        long t = new Date().getTime() - date.getTime();
        return t / (60 * 1000);
    }

    /**
     * 获取过去的小时
     *
     * @param date
     * @return
     */
    public static long pastHour(Date date) {
        long t = new Date().getTime() - date.getTime();
        return t / (60 * 60 * 1000);
    }

    /**
     * 获取过去的天数
     *
     * @param date
     * @return
     */
    public static long pastDays(Date date) {
        long t = new Date().getTime() - date.getTime();
        return t / (24 * 60 * 60 * 1000);
    }

    /**
     * 获取两个日期之间的天数
     *
     * @param before
     * @param after
     * @return
     */
    public static double getDistanceOfTwoDate(Date before, Date after) {
        long beforeTime = before.getTime();
        long afterTime = after.getTime();
        return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
    }

    /**
     * double和 date的转换
     *
     */
    public static Date doubleToDate(Double time) {
        double a = time;
        int x = (int) a;
        DecimalFormat dcmFmt = new DecimalFormat("0.000000000000");
        double y = Double.parseDouble(dcmFmt.format(a - x) + "0000");
        long itemLong = (long) x * (1000 * 60 * 60 * 24) - 8 * 3600 * 1000;// date从1970年8点开始，所以要减去8小时的毫秒数
        long itemday = (long) (y * (24.0 * 60 * 60 * 1000));
        return new Date(itemLong + itemday);
    }

    /**
     * UTF格式转为Date
     *
     * @param utfTime
     *            yyyy-MM-dd'T'HH:mm:ss.SSS Z
     * @return
     */
    public static Date UTFtoDate(String utfTime) {
        utfTime = utfTime.replace("Z", " UTC");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        Date dt = null;
        try {
            dt = sdf.parse(utfTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;

    }

    /**
     * UTF格式转为Date格式
     *
     * @param utfTime
     *            yyyyMMdd'T'HHmmss Z
     * @return
     */
    public static Date UTFtoDate2(String utfTime) {
        utfTime = utfTime.replace("Z", " UTC");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss Z");
        Date dt = null;
        try {
            dt = sdf.parse(utfTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    /**
     * 判断2个时间的差值(end - start)
     *
     * @return
     */
    public static long compareDate(Date startDate, Date endDate) {
        return endDate.getTime() - startDate.getTime();
    }

    /**
     * 判断2个时间的差值(end - start)
     *
     * @return
     */
    public static long compareDate(String startDate, String endDate) {
        return parseDate(endDate).getTime() - parseDate(startDate).getTime();
    }

    /**
     * 当前日期加上天数
     *
     * @param day
     * @return
     * @throws ParseException
     */
    public static Date plusDay(int day) throws ParseException {
        return new DateTime(stringToDateWithoutSeconds(getDateTime())).plusDays(day).toDate();
    }

    /**
     * 当前日期减去天数
     *
     * @param day
     * @return
     * @throws ParseException
     */
    public static String minusDay(int day) throws ParseException {
        return new DateTime(stringToDateWithoutSeconds(getDateTime())).minusDays(day).toString("yyyy-MM-dd");
    }

    /**
     * 计算两个日期之间相差的分钟数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int MinutesBetween(Date date1, Date date2) {
        long time1 = date1.getTime();
        long time2 = date2.getTime();
        long between_days = Math.abs((time1 - time2) / TimeConstant.ONE_MINITU_MILL);
        return Integer.parseInt(String.valueOf(between_days));
    }

    public static long getDistanceTimes(Date one, Date two) {
        long day = 0;
        long hour = 0;
        long min1 = 0;
        long min = 0;
        long time1 = one.getTime();
        long time2 = two.getTime();
        long diff;
        if (time1 < time2) {// 当前时间小于规定时间
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);// tian
        hour = diff / (60 * 60 * 1000) - day * 24;// shi
        min1 = (diff / (60 * 1000)) - day * 24 * 60 - hour * 60;// fen
        min = day * 24 * 60 + hour * 60 + min1;//
        return min;
    }

    /**
     * z
     *
     * @param str1
     *            第一段时间
     * @param str2
     *            第二段时间
     * @return Long 两个时间段的差值（分钟）
     */
    public static long getDistanceTimes(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long min = 0;
        try {
            Date one = df.parse(str1);// 当前时间
            Date two = df.parse(str2);// 规定时间
            return getDistanceTimes(one, two);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return min;
    }

    public static long getDistanceTimesByHHAndmm(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        long min = 0;
        try {
            Date one = df.parse(str1);// 当前时间
            Date two = df.parse(str2);// 规定时间
            return getDistanceTimes(one, two);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return min;
    }

    public static boolean whetherIsEarlierThanToday(Date date) {
        Date d1 = date;
        Date d2 = DateUtils.parseDate(DateUtils.getDate());
        if (d1.getTime() <= d2.getTime()) {
            return true;
        }
        return false;
    }

    /**
     * 规定多少分钟后
     */
    public static String forwordDate(Date date, Integer minute) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long c = date.getTime() + Long.valueOf(String.valueOf(minute)) * 1000 * 60;// 1000*60*1//是1分钟
        Date forworddate = new Date(c);
        return sdf.format(forworddate);
    }

    public static String forwordDate2(Date date, Integer minute) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long c = date.getTime() - Long.valueOf(String.valueOf(minute)) * 1000 * 60;// 1000*60*1//是1分钟
        Date forworddate = new Date(c);
        return sdf.format(forworddate);
    }

    /**
     * 福利日得到最小单位为0.5小时之后的时间
     * @param date
     * @param minute
     * @return
     */
    public static Date forwordDateWelfare(Date date, Integer minute) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long c = date.getTime() + Long.valueOf(String.valueOf(minute)) * 1000 * 60;// 1000*60*1//是1分钟
        Date forworddate = new Date(c);
        return parseDate(sdf.format(forworddate));
    }

    /**
     * 加减天数后返回string 日期
     *
     * @param dateTime
     *            yyyy-MM-dd HH:mm
     * @param days
     * @return yyyy-MM-dd HH:mm
     */
    public static String addDaysOfDateFormatterString(Date dateTime, int days) {
        if (dateTime == null) {
            return null;
        }
        Date date = DateUtils.addDays(dateTime, days);
        return DateUtils.formatDate(date, "yyyy-MM-dd HH:mm");
    }

    /**
     * 加减天数后返回string 日期
     *
     * @param dateTime
     *
     * @param days
     * @return yyyy-MM-dd
     */
    public static String addDaysOfDateFormatterStringNotHours(String dateTime, int days) {
        if (StringUtils.isBlank(dateTime)) {
            return null;
        }
        Date date = DateUtils.addDays(DateUtils.parseDate(dateTime), days);
        return DateUtils.formatDate(date, "yyyy-MM-dd");
    }

    /**
     * 将星期几转换为对应的数字
     */
    public static int weekToNum(String week) {
        int num = 0;
        if (week.equals("星期一")) {
            num = 1;
        } else if (week.equals("星期二")) {
            num = 2;
        } else if (week.equals("星期三")) {
            num = 3;
        } else if (week.equals("星期四")) {
            num = 4;
        } else if (week.equals("星期五")) {
            num = 5;
        } else if (week.equals("星期六")) {
            num = 6;
        } else if (week.equals("星期日")) {
            num = 7;
        }
        return num;
    }

    /**************************** Calendar *****************************/

    /**
     * date转换为Calendar
     *
     * @param date
     */
    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * date to yyyy-MM-dd HH:mm(Calendar)
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static Calendar stringToCalendar(String date) throws ParseException {
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");//
        // 小写的mm表示的是分钟
        return dateToCalendar(DateUtils.parseDate(date));
    }

    /**
     * date to pattern(yyyy-MM-dd HH:mm /pattern ((Calendar))
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static Calendar stringToCalendar(String date, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);// 小写的mm表示的是分钟
        return dateToCalendar(sdf.parse(date));
    }

    /**
     * UTF格式转为Calendar
     *
     * @param utfTime
     *            yyyy-MM-dd'T'HH:mm:ss.SSS Z
     * @return
     */
    public static Calendar UTFtoCalendar(String utfTime) {
        return dateToCalendar(UTFtoDate(utfTime));
    }

    /**
     * UTF格式转为Calendar格式
     *
     * @param utfTime
     *            yyyyMMdd'T'HHmmss Z
     * @return
     */
    public static Calendar UTFtoCalendar2(String utfTime) {
        return dateToCalendar(UTFtoDate2(utfTime));
    }

    /**
     * 字符串的日期格式的计算
     *
     * @param smdate
     *            开始日期
     * @param bdate
     *            结束日期
     */
    public static int daysBetween(String smdate, String bdate) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtils.parseDate(smdate));
        long time1 = cal.getTimeInMillis();
        cal.setTime(DateUtils.parseDate(bdate));
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 计算两个日期之间相差的分钟数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int MinutesBetween(Calendar date1, Calendar date2) {
        long time1 = date1.getTimeInMillis();
        long time2 = date2.getTimeInMillis();
        long between_days = Math.abs((time1 - time2) / (1000 * 60));
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate
     *            较小的时间
     * @param bdate
     *            较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 判断2个时间相差的天(同一年)
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int daysOfTwo(Date startDate, Date endDate) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(startDate);
        int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
        aCalendar.setTime(endDate);
        int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);

        return day2 - day1;
    }

    /**
     * 将日历对象增减分钟后返回一个新的日历对象作为结果
     *
     */
    public static Calendar calendarAddMin(Calendar calendar, int minute) {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(calendar.getTimeInMillis());
        result.add(Calendar.MINUTE, minute);
        return result;
    }

    /**
     * date和double 的转换
     *
     * @param date
     */
    public static Double dateToDouble(Date date) {
        Calendar calendar = dateToCalendar(date);
        Double etDay = 0.0;
        Double etTime = 0.0;
        try {
            etDay = Double.parseDouble(Long.toString(calendar.getTimeInMillis() / (1000 * 60 * 60 * 24)));
            etTime = calendar.get(Calendar.HOUR_OF_DAY) / 24.0 + calendar.get(Calendar.MINUTE) / (24.0 * 60) + calendar.get(Calendar.SECOND) / (24.0 * 60 * 60);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return etDay + etTime;
    }

    /**
     * calendar转string
     *
     * @param calendar
     * @return
     */
    public static String stringCalendar(Calendar calendar) {
        return DateUtils.formatDate(calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * yyyy-MM-dd calendar 转 string
     *
     * @param calendar
     * @return
     */
    public static String stringCalendar_Days(Calendar calendar) {
        return DateUtils.formatDate(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * c1>=c2返回true
     *
     * @param c1
     * @param c2
     * @return
     */
    public static boolean clendarCompareEq(Calendar c1, Calendar c2) {
        if (c1.getTimeInMillis() >= c2.getTimeInMillis()) {
            return true;
        }
        return false;
    }

    /**
     * c1>c2 , return true
     *
     * @param c1
     * @param c2
     * @return
     */
    public static boolean clendarCompare(Calendar c1, Calendar c2) {
        if (c1.getTimeInMillis() > c2.getTimeInMillis()) {
            return true;
        }
        return false;
    }

    /**
     * 获取开始日期后的天数的所有yyyy-MM-dd格式的String list
     *
     * @param date
     * @param days
     * @return
     */
    public static List<String> getDateListFromStart(Date date, int days) {
        Calendar calendar = DateUtils.dateToCalendar(date);
        List<String> list = new ArrayList<String>();
        list.add(DateUtils.stringCalendar_Days(calendar));

        for (int i = 1; i < days; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            list.add(DateUtils.stringCalendar_Days(calendar));
        }
        return list;
    }

    /**
     * 判断一个时间是否在两段时间之间
     *
     * @param start
     * @param end
     * @param calendar
     * @return
     */
    public static boolean isBetweenTwoTime(Calendar start, Calendar end, Calendar calendar) {
        return calendar.before(end) && calendar.after(start) ? true : false;
    }

    public static boolean isBetweenTwoTime(Date startDate, Date endDate, Date date) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();

        start.setTime(startDate);
        end.setTime(endDate);
        calendar.setTime(date);
        return isBetweenTwoTime(start, end, calendar);
    }

    public static boolean isBetweenTwoTime(String startTime, String endTime, String dateStr) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();

        Date startDate = DateUtils.parseDate(startTime);
        Date endDate = DateUtils.parseDate(endTime);
        Date date = DateUtils.parseDate(dateStr);

        start.setTime(startDate);
        end.setTime(endDate);
        calendar.setTime(date);

        return isBetweenTwoTime(start, end, calendar);
    }

    /**
     * 获取开始日期前的天数的所有yyyy-MM-dd格式的String list
     *
     * @param date
     * @param days
     * @return
     */
    public static List<String> getbeforeDateListFromStart(Date date, int days) {
        Calendar calendar = DateUtils.dateToCalendar(date);
        List<String> list = new ArrayList<String>();
        list.add(DateUtils.stringCalendar_Days(calendar));

        for (int i = 1; i < days; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            list.add(DateUtils.stringCalendar_Days(calendar));
        }
        return list;
    }

    /**
     * 获取开始日期前后的天数的yyyy-MM-dd格式的String
     *
     * @param date
     * @param days
     * @return
     * @throws ParseException
     */
    public static String getDateFromStart(String date, int days) throws ParseException {
        Calendar rightNow = DateUtils.dateToCalendar(DateUtils.stringToDate(date, "yyyy-MM-dd"));
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
        // 如果是几天，+1天
        // 如果是后退几天，就写 -天数 例如：
        rightNow.add(java.util.Calendar.DAY_OF_MONTH, days);
        // 进行时间转换
        return sim.format(rightNow.getTime());
    }

    public static Date getDateFromStartDate(String date, int days) throws ParseException {
        Calendar rightNow = DateUtils.dateToCalendar(DateUtils.stringToDate(date, "yyyy-MM-dd"));
        // 如果是几天，+1天
        // 如果是后退几天，就写 -天数 例如：
        rightNow.add(java.util.Calendar.DAY_OF_MONTH, days);
        // 进行时间转换
        return rightNow.getTime();
    }

    public static Date getDateFromStart(Date date, int days) throws ParseException {
        Calendar rightNow = DateUtils.dateToCalendar(date);
        // 如果是几天，+1天
        // 如果是后退几天，就写 -天数 例如：
        rightNow.add(java.util.Calendar.DAY_OF_MONTH, days);
        // 进行时间转换
        return rightNow.getTime();
    }

    /**
     * 根据开始时间计算日期
     *
     * @param startTime
     *            开始时间
     * @return
     * @throws ParseException
     */
    public static Date generateSumTime(String startTime) throws ParseException {
        SimpleDateFormat formt = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(formt.parse(startTime));
        c.add(Calendar.DATE, 1); // 日期分钟加1,Calendar.DATE(天),Calendar.HOUR(小时)
        return c.getTime();
    }

    public static Date generateSumTime(Date startTime) throws ParseException {
        Calendar c = dateToCalendar(startTime);
        c.add(Calendar.DATE, 1); // 日期分钟加1,Calendar.DATE(天),Calendar.HOUR(小时)
        return c.getTime();
    }

    /**
     * 时间比较 ，仅 HH:mm 部分
     *
     * @formatter:off start > end return -1 start = end return 0 else return 1
     * @formatter:on
     * @param start
     * @param end
     * @return
     */
    public static int calendarCompare(String start, String end) {
        SimpleDateFormat dateformat1 = new SimpleDateFormat("HH:mm");
        String pa;
        String pas;
        Date date = parseDate(start);
        Date ends = parseDate(end);
        pa = dateformat1.format(date);
        pas = dateformat1.format(ends);

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(dateformat1.parse(pa));
            c2.setTime(dateformat1.parse(pas));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return c2.compareTo(c1);
    }

    public static int calendarCompare(Date start, Date end) {
        SimpleDateFormat dateformat1 = new SimpleDateFormat("HH:mm");
        String pa;
        String pas;
        Date date = start;
        Date ends = end;
        pa = dateformat1.format(date);
        pas = dateformat1.format(ends);

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try {
            c1.setTime(dateformat1.parse(pa));
            c2.setTime(dateformat1.parse(pas));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return c2.compareTo(c1);
    }

    /**
     * 生成两个日期之间的时间
     *
     * @param dBegin
     * @param dEnd
     * @return
     */
    public static List<Date> findDates(Date dBegin, Date dEnd) {
        List<Date> lDate = new ArrayList<Date>();
        lDate.add(dBegin);
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(dEnd);
        // 测试此日期是否在指定日期之后
        while (dEnd.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(calBegin.getTime());
        }
        return lDate;
    }

    /**
     * 获取当前月的第一天date yyyy-MM-dd
     */
    public static String monthfirstday() {
        Date date = new Date();
        Calendar c = DateUtils.dateToCalendar(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return DateUtils.stringCalendar_Days(c);

    }

    /**
     * 获取当前月的最后一天date yyyy-MM-dd
     */
    public static String monthendday() {
        Date date = new Date();
        Calendar c = DateUtils.dateToCalendar(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return DateUtils.stringCalendar_Days(c);

    }

    public static int getDaysByYearAndMonth(String year, String month) {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, Integer.parseInt(year));
        a.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取上个月的最后一天,上个月的天数
     *
     * @return
     */
    public static int agoMonthendday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 当前日期指定月后的日期
     *
     * @param months
     * @return
     */
    public static String getRegulaDate(int months) {
        Format f = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, months);
        return f.format(c.getTime());
    }

    /**
     * 根据日期 找到对应日期的 星期
     */
    public static String getDayOfWeekByDate(String date) {
        String dayOfweek = "-1";
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate;
        try {
            myDate = myFormatter.parse(date);
            SimpleDateFormat formatter = new SimpleDateFormat("E");
            String str = formatter.format(myDate);
            dayOfweek = str;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dayOfweek;
    }

    /**
     * 指定月份内的倒数第几个工作日
     *
     * @param date
     * @param num
     *            1.最后一个，2.倒数第二个
     * @return
     */
    public static String getDayOfWeekByDate(Date date, int num) {
        List<Date> dates = new ArrayList<Date>();
        Calendar cal = Calendar.getInstance();
        Integer year = Integer.valueOf(formatDate(date, "yyyy")); //传入的日期的年份 2017
        Integer month = Integer.valueOf(formatDate(date, "MM")); //传入的日期的月份 12

        cal.set(Calendar.YEAR, year); //2017
        cal.set(Calendar.MONTH, month - 1); //11
        cal.set(Calendar.DATE, 1); //一个月中的第一天 1

        while (cal.get(Calendar.MONTH) < month) { // 11<12

            int day = cal.get(Calendar.DAY_OF_WEEK); //6

            if (!(day == Calendar.SUNDAY || day == Calendar.SATURDAY)) {
                //该日是工作日（记录）
                dates.add((Date) cal.getTime().clone());
            }
            cal.add(Calendar.DATE, +1);
        }
        return new java.sql.Date(dates.get(dates.size() - num).getTime()).toString();
    }

    /**
     * 获取指定月内的星期几的数量
     *
     * @param dayofweek
     *            星期一，星期二，星期三，星期四，星期五，星期六，星期日
     * @return
     */
    public static List<String> getDayOfWeekListByDateRange(Date date, String dayofweek) {
        System.out.println("date"+date);
        Calendar c_begin = new GregorianCalendar();
        Calendar c_end = new GregorianCalendar();
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] weeks = dfs.getWeekdays();
        Integer year = Integer.valueOf(formatDate(date, "yyyy"));
        Integer month = Integer.valueOf(formatDate(date, "MM")) - 1;
        Calendar c = DateUtils.dateToCalendar(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        Integer endDay = Integer.valueOf(DateUtils.stringCalendar_Days(c).substring(8, 10));
        c_begin.set(year, month, 1); // Calendar的月从0-11，所以2月是1
        c_end.set(year, month, endDay);
        c_end.add(Calendar.DAY_OF_YEAR, 1); // 结束日期下滚一天是为了包含最后一天
        List<String> dates = new ArrayList<String>();

        while (c_begin.before(c_end)) {
            if (weeks[c_begin.get(Calendar.DAY_OF_WEEK)].equals(dayofweek)) {
                dates.add(new java.sql.Date(c_begin.getTime().getTime()).toString());
            }
            // System.out.println("第" + count + "周 日期：" + new
            // java.sql.Date(c_begin.getTime().getTime()) + "," +
            // weeks[c_begin.get(Calendar.DAY_OF_WEEK)]);
            // if (c_begin.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            // count++;
            // }
            c_begin.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dates;
    }

    public static List<String> queryWeekOfRangeMonth(String date,String week){
        List<String> finalDate = new ArrayList<String>();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        sdfDate.setLenient(false);
        SimpleDateFormat sdfWeek = new SimpleDateFormat("EEE");
        for(int i=1;i<32;i++){
            try {
                Date newdate = sdfDate.parse(date.substring(0,7) + "-" + i);
                String dateWeek = sdfWeek.format(newdate).toString();
                if (dateWeek.equals(week)){
                    finalDate.add(formatDateTime2(newdate));
                }
            }catch (ParseException e){
                System.out.println("DateUtils.queryWeekOfRangeMonth转换日期格式出错！");
                e.printStackTrace();
            }
        }
        return finalDate;
    }

    /**
     * 传入指定的日期，加上秒数转换成年月日时分的字符串格式
     * @param date
     * @param seconds
     * @return
     */
    public static String addSecondsConvertToYMDHM(Date date, String seconds){
//        System.out.println("转换前："+DateUtils.formatDate(date, "yyyy-MM-dd HH:mm:ss"));
        int secondsNumber = Integer.valueOf(seconds);
        Date dateConvert= DateUtils.addSeconds(date, secondsNumber);
        String dateConvertString = DateUtils.formatDate(dateConvert, "yyyy-MM-dd HH:mm");
//        System.out.println("转换后："+DateUtils.formatDate(dateTrans, "yyyy-MM-dd HH:mm"));

        return dateConvertString;
    }

    /**
     * 获取昨天的日期
     */
    public static String getYesterdayDate(){
        return new SimpleDateFormat("yyyy-MM-dd").format((new Date().getTime()-24*3600*1000L));
    }
}
