package com.xinlan.filedownloadcontroller.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by py on 2016/10/21.
 */
public class DownloadEngine {
    private static final int MAX_DOWNLOAD_TASK_NUM = 5;//支持最大同时下载数量
    private static final int BUFFER_SIZE = 1024;
    private static DownloadEngine mInstance;

    private static Object lock = new Object();

    public static DownloadEngine getInstance() {
        if (mInstance == null) {
            synchronized (lock) {
                mInstance = new DownloadEngine();
            }
        }

        return mInstance;
    }

    private ExecutorService threadPool;//下载任务线程池

    private Handler mUIHandler;

    private boolean isInit = false;

    protected Context context;
    protected String directory;

    private Map<String, TaskBean> mTasks = new ConcurrentHashMap<String, TaskBean>();

    private DownloadListener mDownloadListener;

    private DownloadEngine() {
        mUIHandler = new Handler(Looper.myLooper());
        checkAndEnsureThreadPool();
    }

    private void checkAndEnsureThreadPool() {
        if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()) {
            threadPool = Executors.newFixedThreadPool(MAX_DOWNLOAD_TASK_NUM);
        }
    }

    /**
     * @param context
     * @param saveDirectory
     */
    public void init(Context context, final String saveDirectory) {


        this.context = context;
        this.directory = saveDirectory;

        isInit = true;
    }

    /**
     * 获取任务
     * @param url
     * @return
     */
    public TaskBean getTaskBean(final String url){
        return mTasks.get(url);
    }

    /**
     * 添加下载任务
     *
     * @param fileUrl
     */
    public void startDownloadTask(final String fileUrl,String filename) {
        TaskBean taskBean = mTasks.get(fileUrl);

        if (taskBean == null) {//任务不存在
            taskBean = TaskBean.createTask(fileUrl,filename);
            mTasks.put(fileUrl, taskBean);
            startDownloadTask(taskBean);
        } else {

        }//end
    }

    protected boolean startDownloadTask(final TaskBean bean) {
        threadPool.submit(new DownloadTask(bean));
        return true;
    }



    public void setDownloadListener(DownloadListener mDownloadListener) {
        this.mDownloadListener = mDownloadListener;
    }

    public void destory() {
        if (threadPool != null && !threadPool.isShutdown()) {//关闭线程池中的任务
            threadPool.shutdownNow();
        }
    }

    //download Task
    private final class DownloadTask implements Runnable {
        private TaskBean taskBean;

        public DownloadTask(final TaskBean bean) {
            this.taskBean = bean;
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            RandomAccessFile downloadfile = null;

            try {
                //1.获取文件基本信息
                URL url = new URL(taskBean.getFileUrl());

                //创建下载目录
                File directoryFile = new File(directory);
                if (!directoryFile.exists()) {
                    directoryFile.mkdirs();
                }

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                setRequestParamsConstants(conn);

                if(TextUtils.isEmpty(taskBean.getPath())){
                    String downloadFilePath = directory + File.separatorChar + FileUtil.findFileName(taskBean.getFileUrl(),
                            taskBean.getFileName());
                    taskBean.setPath(downloadFilePath);
                }

                downloadfile = new RandomAccessFile(taskBean.getPath(), "rwd");

                long offset = taskBean.getCurrent();
                if (taskBean.getCurrent() != downloadfile.length()) {
                    taskBean.setCurrent(0);
                } else {
                    taskBean.setCurrent(offset);
                }

                conn.setRequestProperty("Range", "bytes=" + offset + "-" +
                        (taskBean.getTotal() > 0 ? taskBean.getTotal() + "" : ""));// 设置获取实体数据的范围

                conn.connect(); // 连接


                if (conn.getResponseCode() / 100 == 2) { // 响应成功
                    long fileSize = conn.getContentLength();
                    if (fileSize <= 0) {
                        setTaskErrorInfo(TaskBean.STATUS_ERROR, "file size error");
                        return;
                    }

                    taskBean.setCurrent(0);
                    taskBean.setTotal(fileSize);
                    taskBean.setStatus(TaskBean.STATUS_START);

                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mDownloadListener != null) {
                                mDownloadListener.onPreStart(taskBean.getFileUrl(), taskBean.getTotal());
                            }
                        }
                    });

                    InputStream inStream = conn.getInputStream();
                    byte[] buffer = new byte[BUFFER_SIZE];
                    downloadfile.seek(offset);

                    int readSize;

                    //下载文件
                    while (!taskBean.isPause.get()
                            && (readSize = inStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                        // 写入文件
                        downloadfile.write(buffer, 0, readSize);
                        offset += readSize; // 累加下载的大小
                        taskBean.setCurrent(offset);

                        //update callback
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mDownloadListener != null) {
                                    mDownloadListener.onUpdateProgress(taskBean.getFileUrl(),
                                            taskBean.getCurrent(),taskBean.getTotal());
                                }
                            }
                        });

                        //System.out.println("下载进度 : "+taskBean.getCurrent()+" / "+taskBean.getTotal());
                    }//end while

                    if(offset == taskBean.getTotal()){//下载成功
                        taskBean.setStatus(TaskBean.STATUS_COMPLETE);
                        mTasks.remove(taskBean.getFileUrl());

                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mDownloadListener != null) {
                                    mDownloadListener.onComplete(taskBean.getFileUrl(),taskBean.getTotal(),taskBean.getPath());
                                }
                            }
                        });
                    }
                } else {
                    setTaskErrorInfo(TaskBean.STATUS_ERROR, "server no response");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                setTaskErrorInfo(TaskBean.STATUS_ERROR, e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                setTaskErrorInfo(TaskBean.STATUS_ERROR, e.toString());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (downloadfile != null) {
                    try {
                        downloadfile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void setRequestParamsConstants(HttpURLConnection conn) throws ProtocolException {
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
//            conn.setRequestProperty(
//                    "Accept",
//                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg, " +
//                            "application/x-shockwave-flash, application/xaml+xml, " +
//                            "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
//                            "application/x-ms-application, application/vnd.ms-excel, " +
//                            "application/vnd.ms-powerpoint, application/msword, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Referer", taskBean.getFileUrl());
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2)");
            conn.setRequestProperty("Connection", "Keep-Alive");
        }

        private void setTaskErrorInfo(final int status, final String msg) {
            taskBean.setStatus(status);
            taskBean.setExtra(msg);
            //mTasks.remove(taskBean.getFileUrl());

            System.out.println("error ="+msg);

            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDownloadListener != null) {
                        mDownloadListener.onError(taskBean.getFileUrl(), taskBean.getCurrent(),
                                taskBean.getTotal(), taskBean.getExtra());
                    }
                }
            });
        }
    }//end inner class
}//end class
