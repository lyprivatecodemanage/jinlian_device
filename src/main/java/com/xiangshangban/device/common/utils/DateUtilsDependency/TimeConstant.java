package com.xiangshangban.device.common.utils.DateUtilsDependency;

/**
 * Created by liuguanglong on 2017/10/20.
 */

public class TimeConstant {

    public static long ONE_MINITU_MILL = 1 * 60 * 1000;

    public static long ONE_DAY_MILL = 1 * 24 * 60 * ONE_MINITU_MILL;

    public static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String DATE_FORMAT = "yyy-MM-dd";

    public static String TIME_FORMAT = "HH:mm:ss";

    public static String[] parsePatterns = { "yyyy", "MM", "dd", "E", "HH:mm:ss", "yyyy.MM", "yyyy/MM", "yyyy-MM", "yyyy.MM.dd", "yyyy/MM/dd", "yyyy-MM-dd", "yyyy.MM.dd HH:mm", "yyyy/MM/dd HH:mm",
            "yyyy-MM-dd HH:mm", "yyyy.MM.dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSS Z" };
}
