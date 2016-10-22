package com.xinlan.filedownloadcontroller.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by py on 2016/10/21.
 */
public class DownloadManager {

    public interface DownloadListener{
        /**
         * 下载即将开始
         * @param url
         * @param totalSize
         */
        void onPreStart(final String url,final long totalSize);

        /**
         * 下载进度更新
         * @param url
         * @param currentProgress
         * @param total
         */
        void onUpdateProgress(final String url,final long currentProgress,final long total);

        /**
         * 下载完成
         * @param url
         * @param total
         */
        void onComplete(final String url,final long total);

        /**
         * 下载任务取消
         * @param url
         * @param currentProgress
         * @param total
         */
        void onCancel(final String url,final long currentProgress,final long total);

        /**
         * 下载发生错误
         * @param url
         * @param currentProgress
         * @param total
         */
        void onError(final String url,final long currentProgress,final long total,final Exception e);

        /**
         * 暂停下载任务
         * @param url
         * @param currentProgress
         * @param total
         */
        void onPause(final String url,final long currentProgress,final long total);

        /**
         * 继续下载任务
         * @param url
         * @param currentProgress
         * @param total
         */
        void onResume(final String url,final long currentProgress,final long total);
    }

    private static DownloadManager mInstance;

    private static Object lock = new Object();

    public static DownloadManager getInstance() {
        if (mInstance == null) {
            synchronized (lock) {
                mInstance = new DownloadManager();
            }
        }

        return mInstance;
    }

    private ExecutorService threadPool;//下载任务线程池

    private Handler mUIHandler;

    private boolean isInit = false;

    private Map<String,TaskBean> mTasks = new TreeMap<String,TaskBean>();

    private DownloadListener mDownloadListener;

    private DownloadManager() {
        mUIHandler = new Handler(Looper.getMainLooper());
    }

    public void init(Context context) {

        isInit = true;
    }


}//end class
