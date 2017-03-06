package com.loyo.fasttest.common.http;

import org.xutils.http.annotation.HttpResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/***************************************************
 * 作用：对json数据的封装，那json装到map,list里面，使用方便，不用try，如果不存在，直接返回0或者null
 * 所有的key会忽略大小写
 * 作者：Mr.Jie
 * 修改时间：2016年3月18日 下午8:43:36
 ***************************************************/
//添加注解，方便xutil的直接转换
@HttpResponse(parser = XutilConverJieBean.class)
public class JieBean {
	/**保存数据 */
	protected Map<String, Object> values;

	/**无参数构造*/
	public JieBean() {
		values = new HashMap<String, Object>();
	}

	/**
	 * 通过map初始化
	 * @param valuetemp map
     */
	public JieBean(Map<String, Object> valuetemp) {
		setValues(valuetemp);
	}
	/** 覆盖内容，一般用不到*/
	public void setValues(Map<String, Object> valuetemp) {
		values = valuetemp;
	}

	/**
	 * 添加数据
	 * @param key key
	 * @param obj 内容
     */
	public void addValue(String key, Object obj) {
		values.put(key.toLowerCase(), obj);
	}



	/** 合并两个JieBean，如果有相同的属性，会被覆盖 */
	public void appendValues(JieBean jieBean) {
		if (jieBean != null) {
			appendValues(jieBean.getValues());
		}
	}

	/**从根开始，添加一组数据,如果有相同的属性，会被覆盖 */
	public void appendValues(Map<String, Object> valuetemp) {
		if (!isEmpty(valuetemp)) {
			this.values.putAll(valuetemp);
		}
	}

	/**
	 * 获取所有的数据
	 * @return 包含所有数据的map
     */
	public Map<String, Object> getValues() {
		return values;
	}
	/**
	 * 获取一个value，支持使用路径多层查询，路径用|分割
	 * @param key 键，如果是路径，用|分割
	 * @return 返回值
	 */
	public Object getValue(String key) {
		Object value = null;
		try {
			if (!isEmpty(key)) {
				key=key.toLowerCase();
				//把key按照|分割一下
				String[] pathNames = key.split("\\|");
				int keyLen=pathNames.length;
				if ( keyLen== 1) {
					value = values.get(key.toLowerCase());
				} else {
//					value = getValueByPath(key);
					//先获取第一层
					JieBean thisJieBean=getJieBean(pathNames[0]);
					//为空就返回null
					if (thisJieBean==null)return null;
					for (int i = 1; i < keyLen; i++) {
						if(i!=keyLen-1){
							//如果是在寻找值，那麽每次返回都是一个子JieBean
							thisJieBean = thisJieBean.getJieBean(pathNames[i]);
						}else{
							//如果找到了值，那麽返回就是一个Object
							value= thisJieBean.getValue(pathNames[i]);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return value;
	}
	/**
	 * 提取一个数组
	 * @param key 键
	 * @return 一个包含数组的JieBean的list
     */
	public List<JieBean> getJieBeans(String key) {
		List<JieBean> jieBeen = null;
		try {
			Object value = getValue(key);
			if (value instanceof List) {
				jieBeen = (List<JieBean>) value;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jieBeen;
	}

	/**
	 * 提取一个字符串
	 * @param key
	 * @return key 对应的内容
	 */
	public String getString(String key) {
		Object value = getValue(key);
		String message = null;
		if (value != null) {
			message = value.toString();
		}
		return message;
	}

	/**
	 * 提取某个字段，返回成JieBean的类型,也就是提取原来内容的一部分，重新转换成JieBean
	 * 特别说明，提取的内容必须是一个map类型（JsonObject），不然会转换失败，返回null
	 * @param key 字段名
	 * @return JieBean
     */
	public JieBean getJieBean(String key) {
		JieBean jieBean = null;
		try {
			//转换才一个新的jiebean
			Object obj=getValue(key);
			if(null==obj)return null;
			jieBean = (JieBean) obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jieBean;
	}


	/**
	 * 获取一个整型字段，如果不存在或者不是整型的时候，返回defaultValue
	 * @param key 整型字段名
	 * @param defaultValue 不存在的默认值
     * @return
     */
	public int getInt(String key, int defaultValue) {
		int i = defaultValue;
		try {
			Object obj=getValue(key);
			if(null==obj)return defaultValue;
			i= Integer.parseInt(obj+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}

	/**
	 * 获取一个整型字段，如果不存在或者不是整型的时候，返回0
	 * @param key 整型字段名
	 * @return
     */
	public int getInt(String key) {
		return getInt(key,0);
	}


	/**
	 * 提取一个浮点类型的数据，如果不存在的时候，返回defaultValue
	 * @param key 字段名
	 * @param defaultValue 默认值
     * @return 返回对象的value，如果不存在，就返回defaultValue
     */
	public float getFloat(String key, float defaultValue) {
		float f = defaultValue;
		try {
			Object obj=getValue(key);
			if(null==obj)return defaultValue;
			f= Float.parseFloat(obj+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	/**
	 * 提取一个浮点类型的数据，如果不存在的时候，返回0
	 * @param key 字段名
	 * @return 返回对象的value，如果不存在，就返回0
     */
	public float getFloat(String key) {
		return getFloat(key,0f);
	}

	/**
	 * 提取一个Double类型的数据，如果不存在的时候，返回defaultValue
	 * @param key 字段名
	 * @param defaultValue 默认值
	 * @return 返回对象的value，如果不存在，就返回defaultValue
	 */
	public double getDouble(String key, double defaultValue) {
		double d = defaultValue;
		try {
			Object obj=getValue(key);
			if(null==obj)return defaultValue;
			d= Double.parseDouble(obj+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * 提取一个Double类型的数据，如果不存在的时候，返回0
	 * @param key 字段名
	 * @return 返回对象的value，如果不存在，就返回0
	 */
	public double getDouble(String key) {
		return getDouble(key,0);
	}

	/**
	 * 提取一个布尔类型
	 * @param key 字段名
	 * @param defaultValue 默认值
     * @return 如果key不存在，就返回默认值
     */
	public boolean getBoolean(String key, boolean defaultValue) {
		boolean b = defaultValue;
		try {
			Object obj=getValue(key);
			if(null==obj)return defaultValue;
			b = Boolean.valueOf(defaultValue+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	/**
	 * 提取一个布尔类型
	 * @param key 字段名
	 * @return 如果key不存在，就返回false
	 */
	public boolean getBoolean(String key) {
		return getBoolean(key,false);
	}

	/**
	 * 提取一个日期，时间
	 * @param key 字段名
	 * @return 如果提取失败，返回null
     */
	public Date getDate(String key){
		return  parse2DateTime(getValue(key)+"");
	}
	/**
	 * 按照指定格式获取一个时间日期
	 * @param key 字段名
	 * @param formatStr 获取以后的格式
     * @return 格式化以后的的字符串
     */
	public String getDateTimeString(String key, String formatStr){
		String result=null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
			result= sdf.format(getDate(key));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 返回时间日期的字符串，默认格式：yyyy-MM-dd HH:mm:ss
	 * @param key 字段名
	 * @return 格式化以后的字符串
     */
	public String getDateTimeString(String key){
		return getDateTimeString(key,"yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public String toString() {
		return values.toString();
	}

	/**
	 * 清空这个实例的内容
	 */
	public void clear() {
		values.clear();
	}



	/**
	 * 判断当前实例是不是空
	 * @return
     */
	public boolean isEmpty() {
		return values==null||values.isEmpty();
	}

	/**
	 * 判断数据是否为空
	 * @param obj 要判断的类型
	 * @return 如果是空，返回true
     */
	private static boolean isEmpty(Object obj) {
		boolean flag = true;
		if (obj != null) {
			if (obj instanceof String) {
				flag = (obj.toString().trim().length() == 0);
			} else if (obj instanceof Collection<?>) {
				flag = ((Collection<?>) obj).size() == 0;
			} else if (obj instanceof Map) {
				flag = ((Map<?,?>) obj).size() == 0;
			} else if (obj instanceof JieBean) {
				flag = ((JieBean) obj).getValues().size() == 0;
			} else if (obj instanceof Object[]) {
				flag = ((Object[]) obj).length == 0;
			} else {
				flag = false;
			}
		}
		return flag;
	}

	/**
	 * 字符串转换日期时间
	 * @param dateStr 包含日期和时间的字符串，可以是时间戳(13位的时间戳)
	 * @return 转换以后的时间日期，如果转换失败，返回null
     */
	private static Date parse2DateTime(String dateStr) {
		Date result = null;
		SimpleDateFormat sdf = null;
		try {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result = sdf.parse(dateStr);
		} catch (Exception e) {
			try {
				result = sdf.parse(stampToDateString(dateStr));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 将时间戳转换为时间
	 * @param stamp 时间戳
	 * @return
     */
	private static String stampToDateString(String stamp){
		try{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long lt = new Long(stamp);
			Date date = new Date(lt);
			return simpleDateFormat.format(date);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
