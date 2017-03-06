package com.loyo.fasttest.common.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/***************************************************
 * 作用：把String 直接转换成封装的JieBean实体
 * 作者：Mr.Jie
 * 修改时间：2016年3月18日 下午8:45:06
 ***************************************************/
public class JsonUtil {
    /**
     *把String解析成JieBean实体，中间通过JSONObject完成
     * @param responseText 需要解析的字符串
     * @return 解析完成的JieBean
     */
    public static JieBean parseToJieBean(String responseText) {
        JieBean jieBean = new JieBean();
        JSONObject jsonObj = null;//先把字符串转换成JSONobject对象
        if (responseText == null||responseText.length()==0) {
            return null;
         }
        try{
            if (responseText.trim().startsWith("[")) {
                //如果是一个数组，解析成数组
                jsonObj = new JSONObject();
                jsonObj.put("list", new JSONArray(responseText));
            } else {
                //是json对象
                jsonObj = new JSONObject(responseText);
            }
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // 转换为统一的jieBean
        jieBean = convertJSONObject(jsonObj);
        return jieBean;
    }

    /**
     * 把jsonObj解析成JieBean实体
     * @param jsonObj 需要解析的JSONObject
     * @return JieBean
     */
    private static JieBean convertJSONObject(JSONObject jsonObj) {
        if (jsonObj != null) {
            JieBean jieBean = new JieBean();
            // 遍历所有的KEY值
            Iterator<String> keys = jsonObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    // 获取具体对象
                    Object obj = jsonObj.get(key);
                    if (obj != null) {
                        if (obj instanceof JSONObject) {
                            // 添加属性(递归添加)
                            jieBean.addValue(key,
                                    convertJSONObject((JSONObject) obj));

                        } else if (obj instanceof JSONArray) {
                            // 列表对象
                            List<Object> listItems = new ArrayList<Object>();
                            // 将JSONArray逐个解析
                            JSONArray tempArray = (JSONArray) obj;
                            for (int i = 0; i < tempArray.length(); i++) {
                                Object itempObj = tempArray.get(i);
                                if (itempObj instanceof JSONObject) {
                                    // 递归添加
                                    listItems.add(convertJSONObject(tempArray
                                            .getJSONObject(i)));
                                } else {
                                    listItems.add(itempObj);
                                }
                            }
                            jieBean.addValue(key, listItems);

                        } else {
                            jieBean.addValue(key, obj.toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return jieBean;
        }else{
            return null;
        }
    }
}
