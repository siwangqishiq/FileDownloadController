package com.xinlan.filedownloadcontroller;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.xinlan.filedownloadcontroller.core.DownloadEngine;
import com.xinlan.filedownloadcontroller.core.DownloadListener;
import com.xinlan.filedownloadcontroller.core.FileUtil;
import com.xinlan.filedownloadcontroller.core.TaskBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mBtn;
    private ProgressBar mProgressBar;

    private ActionDownload mActionDownload = new ActionDownload();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        DownloadEngine.getInstance().init(this,sdPath+"/panyi_download");

        DownloadEngine.getInstance().setDownloadListener(mActionDownload);

        mBtn = (Button)findViewById(R.id.btn);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);

        mBtn.setOnClickListener(this);
    }

    private void startDownload(){
        final String url = Constants.urls[1];
        int status = DownloadEngine.getInstance().queryTaskStatus(url);
        if(status == TaskBean.STATUS_PAUSE || status == TaskBean.STATUS_READY){//继续
            DownloadEngine.getInstance().startDownloadTask(url,"4.mp4");
        }else{//暂停任务
            DownloadEngine.getInstance().pauseTask(url);
        }

    }

    @Override
    public void onClick(View view) {
        startDownload();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadEngine.getInstance().setDownloadListener(null);
    }

    /**
     * 文件下载监听
     */
    private final class ActionDownload implements DownloadListener{

        @Override
        public void onPreStart(String url, long totalSize) {
            //mProgressBar.setMax((int)totalSize);
        }

        @Override
        public void onUpdateProgress(final String url,final long currentProgress,final long totalSize) {
            int progress =  (int)((currentProgress / (double)totalSize)*100);

            //System.out.println(currentProgress+"   "+totalSize +"   "+progress);
            mBtn.setText(progress+"%");
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onComplete(String url, long total, String path) {
            mBtn.setText("下载完成");
        }

        @Override
        public void onCancel(String url, long currentProgress, long total) {

        }

        @Override
        public void onError(String url, long currentProgress, long total, String e) {

        }

        @Override
        public void onPause(String url, long currentProgress, long total) {
            mBtn.setText("继续");
        }

        @Override
        public void onResume(String url, long currentProgress, long total) {

        }
    }//end inner class

}//end class
