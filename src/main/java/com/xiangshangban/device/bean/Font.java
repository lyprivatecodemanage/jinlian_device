package com.xiangshangban.device.bean;

/**
 * @Author 王勇辉
 * @Date 2017/11/12  10:10
 */
public class Font {
    private String content;
    private String coordinateX;
    private String coordinateY;
    private String fontSize;
    private String fontBold;
    private String fontColor;
    private String fontOrient;

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontBold() {
        return fontBold;
    }

    public void setFontBold(String fontBold) {
        this.fontBold = fontBold;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontOrient() {
        return fontOrient;
    }

    public void setFontOrient(String fontOrient) {
        this.fontOrient = fontOrient;
    }

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
