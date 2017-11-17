package com.xiangshangban.device.bean;

/**
 * @Author 王勇辉
 * @Date 2017/11/12  10:10
 */
public class Font {
    private String content;
    private String coordinateX;
    private String coordinateY;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(String coordinateX) {
        this.coordinateX = coordinateX;
    }

    public String getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(String coordinateY) {
        this.coordinateY = coordinateY;
    }

    @Override
    public String toString() {
        return "Font{" +
                "content='" + content + '\'' +
                ", coordinateX='" + coordinateX + '\'' +
                ", coordinateY='" + coordinateY + '\'' +
                '}';
    }
}
