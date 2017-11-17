/**
 * Copyright (C), 2015-2017, 上海金念有限公司
 * FileName: ServletFilter
 * Author:   liuguanglong
 * Date:     2017/11/14 17:35
 * Description: 跨域过滤器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.xiangshangban.device.common.filter;

/**
 * 〈一句话功能简述〉<br> 
 * 〈跨域过滤器〉
 *
 * @author liuguanglong
 * @create 2017/11/14
 * @since 1.0.0
 */

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@WebFilter(filterName="ServletFilter",urlPatterns="/*")
public class ServletFilter implements Filter{

    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath+fileName);
        out.write(file);
        out.flush();
        out.close();
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("进来了");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req=(HttpServletRequest) request;
        HttpServletResponse res=(HttpServletResponse) response;
        String uri = req.getRequestURI();
        System.out.println(uri);
        //这里填写你允许进行跨域的主机ip
        res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        //res.setHeader("Access-Control-Allow-Origin", "http://192.168.0.141:80");
        res.setHeader("Access-Control-Allow-Credentials","true");
        //允许的访问方法
        //res.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
        //Access-Control-Max-Age 用于 CORS 相关配置的缓存
        res.setHeader("Access-Control-Max-Age", "3600");
        //res.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, type");
        res.setHeader("Access-Control-Allow-Methods", req.getMethod());
        res.setHeader("Access-Control-Allow-Headers", req.getHeader("Access-Control-Request-Headers"));

        chain.doFilter(req, res);
        System.out.println("嘻嘻嘻");
    }

    @Override
    public void destroy() {
    }

}

