package com.xiangshangban.device.common.utils;

/**
 * Created by liuguanglong on 2017/10/27.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public class ImageUtil {

//    private static final Log LOG = LogFactory.getLog(ImageUtil.class);
    /**
     * 两张图片合并
     * @param backgroundImg
     * @param type
     * @param toMergeImg
     * @param x
     * @param y
     * @return
     * @throws IOException
     */
    public static InputStream mergeTwoImg(InputStream backgroundImg,String type,InputStream toMergeImg,int x,int y) throws IOException{
        if(null != backgroundImg && null != toMergeImg){
            //处理被合并图片
            BufferedImage toMergeImgBuffer = ImageIO.read(toMergeImg);
            BufferedImage backgroundImageBuffer =  ImageIO.read(backgroundImg);
            if((backgroundImageBuffer.getWidth()>=toMergeImgBuffer.getWidth()+x)
                    && (backgroundImageBuffer.getHeight()>=toMergeImgBuffer.getHeight()+y)){
                int width = toMergeImgBuffer.getWidth();
                int height = toMergeImgBuffer.getHeight();
                int[] toMergeImgArray = new int[width*height];
                toMergeImgArray = toMergeImgBuffer.getRGB(0,0,width,height,toMergeImgArray,0,width);
                backgroundImageBuffer.setRGB(x,y,width,height,toMergeImgArray,0,width);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(backgroundImageBuffer, type, os);
                return new ByteArrayInputStream(os.toByteArray());
            }
        }
        return null;
    }
    /**
     * 确定图片是否压缩
     * @param confirmImg 要判断的图片
     * @param w 要比较的目标宽度
     * @param h 要比较的目标高度
     * @return boolean
     * @throws IOException
     */
    public static boolean confirmRatio(InputStream confirmImg,int w,int h) throws IOException{
        if(null != confirmImg){
            BufferedImage confirmImgBuffer =  ImageIO.read(confirmImg);
            if(confirmImgBuffer.getWidth()>w || confirmImgBuffer.getWidth()>h){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
    /**
     * 图片上写字符串
     * @param backgroundImg
     * @param type
     * @param str
     * @param x
     * @param y
     * @param font
     * @param color
     * @return
     * @throws IOException
     */
    public static InputStream stringMergeToImg(InputStream backgroundImg,String type,String str,int x,int y,Font font,Color color) throws IOException{

        if(null != backgroundImg ){
            BufferedImage backgroundImageBuffer =  ImageIO.read(backgroundImg);
            if (backgroundImageBuffer == null){
                System.out.println("空");
            }
            Graphics g=backgroundImageBuffer.createGraphics();
            g.setColor(color);
            g.setFont(font);
            g.drawString(str, x, y);
            g.dispose();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(backgroundImageBuffer, type, os);
            return new ByteArrayInputStream(os.toByteArray());
        }
        return null;
    }
    /**
     * 裁剪图片
     * @param toCutImg
     * @param type
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     * @throws IOException
     */
    public static InputStream cutImg(InputStream toCutImg,String type,int x,int y,int width,int height) throws IOException{

        if(null != toCutImg){
            BufferedImage toCutImageBuffer =  ImageIO.read(toCutImg);
            if((toCutImageBuffer.getWidth()>=width) && (toCutImageBuffer.getHeight()>=height)){
                int[] toMergeImgArray = new int[width*height];
                toMergeImgArray = toCutImageBuffer.getRGB(x,y,width,height,toMergeImgArray,0,width);
                BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                resultImage.setRGB(0,0,width,height,toMergeImgArray,0,width);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(resultImage, type, os);
                return new ByteArrayInputStream(os.toByteArray());
            }
        }
        return null;
    }
    /**
     * 缩放图片
     * @param toCutImg
     * @param type
     * @param width
     * @param height
     * @return
     * @throws IOException
     */
    public static InputStream ratioImg(InputStream toCutImg,String type,int width,int height) throws IOException{

        if(null != toCutImg ){
            BufferedImage toRatioImageBuffer =  ImageIO.read(toCutImg);
            BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
            Graphics2D gp2d=(Graphics2D) resultImage.getGraphics();
            gp2d.drawImage(toRatioImageBuffer, 0, 0,width,height,null); //画图
            gp2d.dispose();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resultImage, type, os);
            return new ByteArrayInputStream(os.toByteArray());
        }
        return null;
    }
    
    /**
     * 网络文件转换为流
     * @param urlpath
     * @return
     * @throws IOException
     */
    public static InputStream getInputStreamByUrl(String urlpath) throws IOException {
        URL url = new URL(urlpath);
        URLConnection conn = url.openConnection();
        conn.connect();
        return conn.getInputStream();
    }
    /*	//测试
        public static void main(String[] args) throws IOException {
            File a = new File("d://a.png");
            File b = new File("d://b.png");
            InputStream imgStream = mergeTwoImg(a,b,100,100);

            File f=new File("d://c.png");
            inputstreamToFile(imgStream,f);
            InputStream imgStream2 = stringMergeToImg(f,"上海酬勤信息科技有限公司",700,700,new Font("微软雅黑",30, 30),new Color(177,222,200));
            inputstreamToFile(imgStream2,f);
        }
        //测试
        public static void inputstreamToFile(InputStream ins,File file) throws IOException{
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
            }*/
    //测试图片合成
    //测试图片合成
//    public static void main(String[] args) {
//        String customer="1";
//        String key="abc";
//        String fileName="335E9FFF068C4AFBA6AD1A27EA334ADC.png";
//        String fileName2="DB64EA3F909848099266D8FF3714C1DB.jpg";


//        OSSFileUtil util=new OSSFileUtil("VCaiaSOYIX54e9Ft", "hBHjH0uxBxvfly5FSZpKhB7jbZgjQC");
//        util.initialize();
//        String background=OSSFileUtil.getFilePath(null, fileName);
//        String toMerge=OSSFileUtil.getFilePath(customer, fileName2);
//        try {
//            System.out.println(util.oSSPutStream(
//                    customer, "png", key,
//                    mergeTwoImg(getInputStreamByUrl(background), "png", getInputStreamByUrl(toMerge),101, 120)));
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
    //测试裁剪图片/缩放
	/*public static void main(String[] args) {
		String customer="1";
		 String key="abc1";
		 String fileName="D676B5D444F243318BF0D3DD78206C97.png";
		 OSSFileUtil util=new OSSFileUtil("VCaiaSOYIX54e9Ft", "hBHjH0uxBxvfly5FSZpKhB7jbZgjQC");
		 util.initialize();
		 String imgPath =OSSFileUtil.getFilePath(customer, fileName);
		 try {
			 //System.out.println(util.oSSPutStream(customer, "png", key, cutImg(getInputStreamByUrl(imgPath), "jpg", 100, 19, 200, 200)));
			 System.out.println(util.oSSPutStream(customer, "png", "333", ratioImg(getInputStreamByUrl(imgPath), "png", 45, 45)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	/*public static void main(String[] args) {
		String path="99999.png";
		System.out.println(path.substring(path.lastIndexOf(".")+1));
	}*/

    public static void main(String[] args) throws IOException {

        File file1 = new File("C:\\Users\\loogooloo\\Desktop\\1.png");
        File file2 = new File("C:\\Users\\loogooloo\\Desktop\\2.png");

        if (file1 == null){
            System.out.println("文件Null");
        }else {
            System.out.println("文件非Null");
        }

        InputStream inputStream = new FileInputStream(file1);
        OutputStream outputStream = new FileOutputStream(file2);

        if (inputStream == null){
            System.out.println("空空");
        }else {
            System.out.println("非空");
        }

        InputStream inputStreamResult = stringMergeToImg(inputStream, "png", "你好!", 275, 245, new Font("微软雅黑", Font.BOLD, 20), new Color(0,0,0));

        byte[] read = new byte[1024];
        int len = 0;
        while ((len=inputStreamResult.read(read)) != -1){
            outputStream.write(read, 0, len);
        }

        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }
}
