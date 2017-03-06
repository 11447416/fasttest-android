package com.loyo.fasttest.common.http;

import android.util.Log;
import android.widget.Toast;

import com.loyo.fasttest.common.BaseApplication;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * 对xutils的http请求封装，方便使用
 * Author:张杰
 * DateTime:16-11-19 下午9:36
 */
public class Http {
    private static String TAG="http";
    public interface CallBack {
        void onResult(JieBean jieBean);
    }

    /**
     * 调用xutils发送httpPOST请求
     *
     * @param url      网址
     * @param data     数据
     * @param callBack 返回数据
     */
    public static void send(String url, SortedMap<String, String> data, final CallBack callBack) {
        RequestParams requestParams = new RequestParams(url);
        //把参数写到请求体里面去
        if (null != data) {
            for (Map.Entry<String,String> set : data.entrySet()) {
                requestParams.addQueryStringParameter(set.getKey(),set.getValue());
            }
        }
        x.http().post(requestParams, new Callback.CommonCallback<JieBean>() {
            @Override
            public void onSuccess(JieBean result) {
                callBack.onResult(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JieBean jieBean = new JieBean();
                jieBean.addValue("ret", 0);
                jieBean.addValue("msg", "错误：请检查网络");
                callBack.onResult(jieBean);
                ex.printStackTrace();
                Toast.makeText(BaseApplication.getInstance(),"链接网络失败",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    public static void get(String url, final CallBack callBack){
        Log.i(TAG, "get:url-> "+url);
        RequestParams requestParams=new RequestParams(url);
        x.http().get(requestParams, new Callback.CommonCallback<JieBean>() {
            @Override
            public void onSuccess(JieBean result) {
                callBack.onResult(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JieBean jieBean = new JieBean();
                jieBean.addValue("ret", 0);
                jieBean.addValue("msg", "错误：请检查网络");
                callBack.onResult(jieBean);
                ex.printStackTrace();
                Toast.makeText(BaseApplication.getInstance(),"链接网络失败",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private static String getContent(Map<String, String> data) {
        if(null==data)return "null";
        Set<Map.Entry<String, String>> entrySet = data.entrySet();
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : entrySet) {
            if (stringBuffer.length() != 0) {
                stringBuffer.append("&");
            }
            stringBuffer.append(entry.getKey() + "=" + entry.getValue());
        }
        return stringBuffer.toString();
    }
}
