package com.loyo.fasttest;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loyo.fasttest.common.BaseApplication;
import com.loyo.fasttest.common.Config;
import com.loyo.fasttest.common.download.DownloadService;
import com.loyo.fasttest.common.download.FileUtil;
import com.loyo.fasttest.common.http.Http;
import com.loyo.fasttest.common.http.JieBean;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private MyAdapter myAdapter;
    @ViewInject(R.id.srlayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.recuclerview)
    RecyclerView recyclerView;
    @ViewInject(R.id.btn_clear)
    Button btnClear;
    DownloadManager downloadManager;
    private long enqueue;
    private CompleteReceiver completeReceiver;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        getData();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 5);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("你将要删除已经下载的文件，释放空间，是否继续");
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FileUtil.deleteFile(FileUtil.updateDir);
                        Toast.makeText(MainActivity.this, "完成", Toast.LENGTH_LONG).show();
                    }
                });
                builder.show();
            }
        });


        completeReceiver = new CompleteReceiver();
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(completeReceiver);
        super.onDestroy();
    }

    private void getData() {
        Http.get(Config.listUrl, new Http.CallBack() {
            @Override
            public void onResult(JieBean jieBean) {
                if (jieBean.getInt("ret") == 1) {
                    List<JieBean> data = jieBean.getJieBeans("data");
                    Collections.reverse(data);//倒序
                    myAdapter.setData(data);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("拉取数据失败，可能是以下原因：\n1、设备没有打开网络设置；\n" +
                            "2、测试工具仅能在办公室内网使用，离开办公室不能使用\n" +
                            "3、fasttest的服务器ip变动了。");
                    builder.setCancelable(false);
                    builder.setNegativeButton("好的", null);
                    builder.show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 安装软件
     *
     * @param file
     */
    private void installApk(String file) {
        Log.i(TAG, "installApk: " + file);
        File apkfile = new File(file);
        if (!apkfile.exists()) {
            Log.i(TAG, "installApk: !!!!!");
            return;
        }
//        if (Build.VERSION.SDK_INT >= 24 /*Build.VERSION_CODES.N*/) {
//            Uri apkUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", apkfile);
//            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
//            intent.setData(apkUri);
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(intent);
//        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
            startActivity(i);
//        }
    }

    /**
     * 监听下载完成
     */
    class CompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(enqueue);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int culumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(culumnIndex)) {
                    culumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    installApk(cursor.getString(culumnIndex));
                } else {
                    Toast.makeText(MainActivity.this, "下载出错！", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<JieBean> data = new ArrayList<>();

        public void setData(List<JieBean> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final JieBean jieBean = data.get(position);
            holder.tvId.setText("＃" + jieBean.getString("id"));
            holder.tvTime.setText(jieBean.getDateTimeString("time"));
            holder.tvDes.setText(jieBean.getString("des"));
            holder.progressBar.setVisibility(View.GONE);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("是否下载");
                    builder.setMessage("你将要下载安装包 #" + jieBean.getString("id") + "\n是否下载?");
                    builder.setCancelable(false);
                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Config.url + jieBean.getString("path")))
                                    .setTitle(getResources().getString(R.string.app_name) + "正在下载＃" + jieBean.getString("id"))
                                    .setDescription("新安装包：#" + jieBean.getString("id"))
                                    //写入到应用的存储目录下，避免申请权限
                                    .setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, System.currentTimeMillis() + ".apk")
                                    .setVisibleInDownloadsUi(true).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            try {
                                enqueue = downloadManager.enqueue(request);
                                Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Toast.makeText(MainActivity.this, "错误：" + ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvId;
        public TextView tvTime;
        public TextView tvDes;
        public ProgressBar progressBar;
        public View view;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            tvId = (TextView) itemView.findViewById(R.id.item_id);
            tvTime = (TextView) itemView.findViewById(R.id.item_time);
            tvDes = (TextView) itemView.findViewById(R.id.item_des);
            progressBar = (ProgressBar) itemView.findViewById(R.id.item_pro);
        }
    }
}
