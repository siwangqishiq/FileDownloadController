package com.xinlan.filedownloadcontroller.core;

import android.text.TextUtils;

import java.util.UUID;

/**
 * Created by panyi on 16/10/22.
 */
public class FileUtil {
    public static String findFileName(final String fileUrl,String setFileName) {
        if(!TextUtils.isEmpty(setFileName)){
            return setFileName;
        }

        String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
            filename = UUID.randomUUID() + ".tmp";// 默认取一个文件名
        }
        return filename;
    }
}//end class
