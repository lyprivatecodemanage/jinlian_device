package com.xiangshangban.device.bean;

/**
 * @Author Wangyonghui
 * @Date 2017/12/26 ~ 19:32
 * TODO 分页信息实体类（代替PageHelper进行多数据分页操作）
 */
public class PageInfo {
    //当前页码
    private String pageIndex;
    //每一页显示的行数
    private String pageSize;
    //数据总行数
    private String totalCount;
    //要分的总页数
    private String totalPage;

    public String getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(String pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(String totalPage) {
        this.totalPage = totalPage;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
                "pageIndex='" + pageIndex + '\'' +
                ", pageSize='" + pageSize + '\'' +
                ", totalCount='" + totalCount + '\'' +
                ", totalPage='" + totalPage + '\'' +
                '}';
    }
}
