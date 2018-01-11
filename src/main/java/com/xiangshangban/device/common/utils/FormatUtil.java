package com.xiangshangban.device.common.utils;

import com.alibaba.fastjson.JSON;

import java.util.*;

import net.sf.json.JSONObject;

/**
 * Created by liuguanglong on 2017/10/18.
 */
public class FormatUtil {

    /**
     * 返回json字符串
     * @param name
     * @param value
     * @return
     */
    public static String returnJSONResult(String name,String value){
        Map<String, String> map = new HashMap<String, String>();
        map.put(name,value);
        return JSON.toJSONString(map, true);
    }

    /**
     * 生成UUID
     * @return
     */
    public static String createUuid(){
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * 转换字符集
     * @param in
     * @return
     */
    public static String formatToUTF8(String in){
        try{
            return new String(in.getBytes("ISO-8859-1"),"UTF-8");
        }catch(Exception e){
            return null;
        }
    }
    /**
     * 将json格式的字符串解析成Map对象 <li>
     * json格式：{"name":"admin","retries":"3fff","testname"
     * :"ddd","testretries":"fffffffff"}
     */
    public static HashMap<String, Object> toHashMap(Object object)
    {
        HashMap<String, Object> data = new HashMap<String, Object>();
        // 将json字符串转换成jsonObject
        JSONObject jsonObject = JSONObject.fromObject(object);
        Iterator it = jsonObject.keys();
        // 遍历jsonObject数据，添加到Map对象
        while (it.hasNext())
        {
            String key = String.valueOf(it.next());
            Object ob = jsonObject.get(key);
            data.put(key, ob);
        }
        return data;
    }
    /**
     * 随机指定范围内N个不重复的数
     * 利用HashSet的特征，只能存放不同的值
     * @param min 指定范围最小值
     * @param max 指定范围最大值
     * @param n 随机数个数
     * @param set<Integer> set 随机数结果集
     */
    public static void randomSet(int min, int max, int n, HashSet<Integer> set) {
        if (n > (max - min + 1) || max < min) {
            return;
        }
        for (int i = 0; i < n; i++) {
            // 调用Math.random()方法
            int num = (int) (Math.random() * (max - min)) + min;
            set.add(num);// 将不同的数存入HashSet中
        }
        int setSize = set.size();
        // 如果存入的数小于指定生成的个数，则调用递归再生成剩余个数的随机数，如此循环，直到达到指定大小
        if (setSize < n) {
            randomSet(min, max, n - setSize, set);// 递归
        }
    }
    public static void main(String[] args) {
        HashSet<Integer> set =new HashSet<Integer>();
        randomSet(0,65536,5,set);
        Iterator<Integer> iter = set.iterator();
		   /*while(iter.hasNext()){
			   System.out.println(Integer.toHexString(iter.next()));
		   }*/
        System.out.println(createUuid());
    }

}
