package com.xinlan.filedownloadcontroller.core;

import java.util.List;

/**
 * Created by panyi on 2016/10/20.
 */
public interface DownloadDataRecord {
    void saveDownloadTask(final String fileUrl,final long total,final long progress);
    List<TaskBean> readDownloadTaskList();
}
