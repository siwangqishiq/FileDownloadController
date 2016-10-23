package com.xinlan.filedownloadcontroller.core;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by panyi on 2016/10/20.
 */
public class TaskBean {
    public static int STATUS_READY = 0;
    public static int STATUS_START = 1;
    public static int STATUS_DOWNING = 2;
    public static int STATUS_PAUSE = 3;
    public static int STATUS_COMPLETE = 4;
    public static int STATUS_ERROR = 5;

    private String fileUrl;
    private long total;
    private long current;
    private int status;
    protected AtomicBoolean isPause = new AtomicBoolean(false);

    private String extra;

    private String path;//下载路径

    private String fileName;

    public static TaskBean createTask(final String url,final String downloadFileName){
        TaskBean bean = new TaskBean();
        bean.setFileUrl(url);
        bean.setFileName(downloadFileName);
        bean.setTotal(-1);
        bean.setCurrent(0);
        bean.setStatus(STATUS_READY);
        return bean;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}//end class
