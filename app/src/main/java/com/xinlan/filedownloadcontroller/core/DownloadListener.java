package com.xinlan.filedownloadcontroller.core;

/**
 * Created by panyi on 16/10/22.
 */
public interface DownloadListener {
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
    void onComplete(final String url,final long total,final String path);

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
    void onError(final String url,final long currentProgress,final long total,final String e);

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
}//end class
