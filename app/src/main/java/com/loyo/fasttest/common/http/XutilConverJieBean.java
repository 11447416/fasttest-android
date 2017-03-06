package com.loyo.fasttest.common.http;

import android.util.Log;

import org.xutils.http.app.ResponseParser;
import org.xutils.http.request.UriRequest;

import java.lang.reflect.Type;

/**
 * xUtils的一个解析类，主要是用来处理Json 的String到JieBean的
 * Author:张杰
 * DateTime:16-11-19 下午9:31
 */
public class XutilConverJieBean implements ResponseParser {
    @Override
    public void checkResponse(UriRequest request) throws Throwable {

    }

    @Override
    public Object parse(Type resultType, Class<?> resultClass, String result) throws Throwable {
        Log.i("niceMoneyHttp", "parse: "+result);
        //直接转换成JieBean
        JieBean jieBean = JsonUtil.parseToJieBean(result);
        //如果是出现错误
        if(null==jieBean){
            jieBean=new JieBean();
            jieBean.addValue("ret",0);
            jieBean.addValue("msg","数据解析错误");
        }
        return  jieBean;
    }
}
