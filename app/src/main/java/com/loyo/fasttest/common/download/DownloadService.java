package com.loyo.fasttest.common.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.loyo.fasttest.R;
import com.loyo.fasttest.common.BaseApplication;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 已经使用downmanager替换本类，但是作为部分手机的下载管理比较过分，所以不考虑删除，后面可能用得到
 */
public class DownloadService extends Service {
    private String TAG = "UpdateService";
    /***************** 下面的一些，可以更改 ***********************/
    private Boolean autoInstall = true;// 下载完了以后，是否自动安装

    /***************** 下面的设置，不建议更改 ***********************/
    // 下载相关的东西
    private static final int down_step_custom = 1; // 更新进度条的步长
    private static final int ReadTIMEOUT = 10 * 1000;// 获取内容超时时长
    private static final int ConnectTIMEOUT = 3 * 1000;// 连接主机超时时长
    private static final int DOWN_PROGRESS = 2;//下载中
    private static final int DOWN_OK = 1;//下载完成
    private static final int DOWN_ERROR = 0;//下载出错
    private static final int NOTIFICATIONID = 100;//通知的id

    /***************** 下面的是变量，不需要管 ***********************/
    // 通知栏相关
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private PendingIntent pendingIntent;

    private String saveFileName;//保存文件名字
    private String down_url;// 下载的url
    private String title;//通知栏标题
    private String content;//下载中的内容
    private String endContent;//结束的时候的内容
    private String startTicker;//开始下载的时候的提示
    private String endTicker;//下载结束的提示
    private String errorTicker;//出错的提示


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        saveFileName = intent.getStringExtra("saveFileName");
        down_url = intent.getStringExtra("down_url");
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        endContent = intent.getStringExtra("endContent");
        startTicker = intent.getStringExtra("startTicker");
        endTicker = intent.getStringExtra("endTicker");
        errorTicker = intent.getStringExtra("errorTicker");

        if (FileUtil.createFile(saveFileName)) {// 判断文件是否创建成功，存储器不可用可能会失败。
            createNotification();
            createThread();

        } else {
            // 弹出失败的提示
            Toast.makeText(this, "创建文件失败！", Toast.LENGTH_SHORT).show();
            stopSelf(); // 结束服务
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_OK:
                    // 下载完成了，更新通知栏，实现点击安装
                    Uri uri = Uri.fromFile(FileUtil.updateFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri,
                            "application/vnd.android.package-archive");
                    pendingIntent = PendingIntent.getActivity(DownloadService.this,
                            0, intent, 0);
                    builder.setContentIntent(pendingIntent);
                    builder.setOngoing(false);
                    builder.setProgress(0, 0, false);
                    builder.setContentTitle(title);
                    builder.setTicker(endTicker);
                    builder.setContentText(endContent);
                    Notification nt = builder.build();
                    nt.flags |= Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(NOTIFICATIONID, nt);
                    // 自动安装
                    if (autoInstall) {
                        installApk();
                    }
                    stopSelf();
                    break;
                case DOWN_ERROR:// 下载出错
                    builder.setContentText("出错" + msg.obj);
                    builder.setOngoing(false);
                    builder.setTicker(errorTicker);
                    notificationManager.notify(NOTIFICATIONID, builder.build());
                    stopSelf();
                    break;
                case DOWN_PROGRESS:// 下载中
                    builder.setContentInfo(msg.arg1 + "MB");
                    builder.setProgress(100, msg.arg2, false);
                    builder.setOngoing(true);
                    notificationManager.notify(NOTIFICATIONID, builder.build());
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 安装应用程序
     */
    private void installApk() {
        Uri uri = Uri.fromFile(FileUtil.updateFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        DownloadService.this.startActivity(intent);
    }

    /**
     * 创建一下下载的线程
     */
    private void createThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    long downloadSize = downloadUpdateFile(down_url,
                            FileUtil.updateFile.toString());
                    if (downloadSize > 0) {
                        message.what = DOWN_OK;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    message.what = DOWN_ERROR;
                    message.obj = e.getMessage();
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    /**
     * 创建一个Notification
     */
    @SuppressWarnings("deprecation")
    private void createNotification() {
        notificationManager = (NotificationManager) this
                .getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(BaseApplication.getInstance());
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(startTicker)
                .setOngoing(true).setContentTitle(title)
                .setContentText(content);
        notificationManager.notify(NOTIFICATIONID, builder.build());
    }

    /**
     * 下载升级apk文件
     *
     * @param down_url url
     * @param file     保存文件名
     * @return 文件大小
     * @throws Exception
     */
    private long downloadUpdateFile(String down_url, String file)
            throws Exception {

        int down_step = down_step_custom;// 提示step
        int totalSize;// 文件总大小
        int downloadCount = 0;// 已经下载好的大小
        int updateCount = 0;// 已经上传显示的文件大小

        InputStream inputStream;
        OutputStream outputStream;
        URL url = new URL(down_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url
                .openConnection();
        httpURLConnection.setConnectTimeout(ConnectTIMEOUT);
        httpURLConnection.setReadTimeout(ReadTIMEOUT);
        // 获取下载文件的size
        totalSize = httpURLConnection.getContentLength();
        if (httpURLConnection.getResponseCode() != 200) {
            throw new Exception("文件不存在！");
        }
        int allSize = totalSize / 1024 / 1024;
        inputStream = httpURLConnection.getInputStream();
        outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉

        byte buffer[] = new byte[1024];
        int readsize = 0;

        while ((readsize = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, readsize);
            downloadCount += readsize;// 获取下载到的大小
            // 进度每次走1%
            if (updateCount == 0
                    || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
                updateCount += down_step;
                // 改变通知栏
                Message message = handler.obtainMessage(DOWN_PROGRESS);
                message.arg1 = allSize;
                message.arg2 = updateCount;
                handler.sendMessage(message);
            }
        }
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
        inputStream.close();
        outputStream.close();
        return downloadCount;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}