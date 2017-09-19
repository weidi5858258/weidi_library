package com.weidi.utils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


/**
 * Gson类库的封装工具类，专门负责解析json数据</br> 
 * 内部实现了Gson对象的单例 
 */
public class JsonUtil {

	private static Gson gson = null;

	static {
		if (gson == null) {
			gson = new Gson();
		}
	}
	/**
	 * 将json转换成 的JsonObject对象
	 * @param jsonStr
	 * @param pojoCalss
	 * @return
	 */
	public static JSONObject jsonToJsonObject(String json) {
		JSONObject jsonObject=null;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return jsonObject;
	}
	/**
	 * 将json转换成google 的JsonObject对象
	 * @param jsonStr
	 * @param pojoCalss
	 * @return
	 */
	public static JSONArray jsonToJsonArray(String json) {
		JSONArray jsonObject=null;
		try {
			jsonObject = new JSONArray(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	/**
	 * 将json转换成google 的JsonObject对象
	 * @param jsonStr
	 * @param pojoCalss
	 * @return
	 */
	public static JsonObject jsonToGoogleJsonObject(String jsonStr) {
		JsonObject jsonObject = null;
		if (jsonStr != null&&!jsonStr.equals("")) {
			jsonObject= new JsonParser().parse(jsonStr).getAsJsonObject();
		}
		return jsonObject;
	}
	/**
	 * 将json转换成google 的JsonArray对象
	 * @param jsonStr
	 * @return
	 */
	public static JsonArray jsonToGoogleJsonArray(String jsonStr) {
		JsonArray jsonarray = null;

		if (jsonStr != null&&!jsonStr.equals("")) {
			jsonarray= new JsonParser().parse(jsonStr).getAsJsonArray();
		}
		return jsonarray;
	}
	/**
	 * 将json转换成bean对象
	 * @param jsonStr
	 * @param pojoCalss
	 * @return
	 */
	public static Object jsonToBean(String jsonStr,  java.lang.reflect.Type type) {
		Object obj = null;

		if (gson != null) {
			obj = gson.fromJson(jsonStr, type);
		}
		return obj;
	}
	/**
	 * 将json转换成bean对象
	 * @param jsonStr
	 * @param pojoCalss
	 * @return
	 */
	public static Object jsonToBean(String jsonStr, Class<?> cl) {
		Object obj = null;

		if (gson != null) {
			obj = gson.fromJson(jsonStr, cl);
		}
		return obj;
	}
	/**
	 * 将对象转换成json格式
	 * @param ts
	 * @return
	 */
	public static String objectToJson(Object ts) {
		String jsonStr = null;
		if (gson != null) {
			jsonStr = gson.toJson(ts);
		}
		return jsonStr;
	}

	/**
	 * 将对象转换成json格式(并自定义日期格式)
	 * @param ts
	 * @return
	 */
	public static String objectToJsonDateSerializer(Object ts,
													final String dateformat) {
		String jsonStr = null;
		gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(Date.class,
						new JsonSerializer<Date>() {
							public JsonElement serialize(Date src,
														 Type typeOfSrc,
														 JsonSerializationContext context) {
								SimpleDateFormat format = new SimpleDateFormat(
										dateformat);
								return new JsonPrimitive(format.format(src));
							}


						}).setDateFormat(dateformat).create();
		if (gson != null) {
			jsonStr = gson.toJson(ts);
		}
		return jsonStr;
	}
	/**
	 * 将json格式转换成list对象
	 *
	 * @param jsonStr
	 * @return
	 */
	public static List<?> jsonToList(String jsonStr) {
		List<?> objList = null;

		if (gson != null) {
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<?>>() {
			}.getType();
			objList = gson.fromJson(jsonStr, type);
		}
		return objList;
	}
	/**
	 * 将json格式转换成list对象，并准确指定类型
	 * @param jsonStr
	 * @param type
	 * @return
	 */
	public static List<?> jsonToList(String jsonStr, java.lang.reflect.Type type) {
		List<?> objList = null;

		if (gson != null) {
			objList = gson.fromJson(jsonStr, type);
		}
		return objList;
	}
	/**
	 * 将json格式转换成map对象
	 *
	 * @param jsonStr
	 * @return
	 */
	public static Map<?, ?> jsonToMap(String jsonStr) {
		Map<?, ?> objMap = null;
		if (gson != null) {
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<?, ?>>() {
			}.getType();
			objMap = gson.fromJson(jsonStr, type);
		}
		return objMap;
	}
	public static Map toMap(String json) {
		JsonParser jsonparer = new JsonParser();// 初始化解析json格式的对象
		JsonObject obj = jsonparer.parse(json).getAsJsonObject();
		String comment = obj.toString();
		java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<String, Map<String, String>>>() {
		}.getType();
		Map<String, Map<String, String>> m = gson.fromJson(comment.toString(),type);
		System.out.println("Map中的内容: " + m);// 得到一个Map即可遍历其中的数据

		// 遍历
		for (Map.Entry<String, Map<String, String>> m2 : m.entrySet()) {
			String key = m2.getKey();
			System.out.print(key + "-");
			Map<String, String> m3 = m2.getValue();
			for (Entry<String, String> m4 : m3.entrySet()) {
				String key2 = m4.getKey();
				System.out.print(key2 + "-");
				String value = m4.getValue();
				System.out.println(value);
			}
		}
		return m;
	}

	/**
	 * 根据
	 *
	 * @param jsonStr
	 * @param key
	 * @return
	 */
	public static Object getJsonValue(String jsonStr, String key) {
		Object rulsObj = null;
		Map<?, ?> rulsMap = jsonToMap(jsonStr);
		if (rulsMap != null && rulsMap.size() > 0) {
			rulsObj = rulsMap.get(key);
		}
		return rulsObj;
	}
	/**
	 * 将json转换成bean对象
	 *
	 * @param jsonStr
	 * @param cl
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T jsonToBeanDateSerializer(String jsonStr, Class<T> cl,
												 final String pattern) {
		Object obj = null;

		gson = new GsonBuilder()
				.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
					public Date deserialize(JsonElement json, Type typeOfT,
											JsonDeserializationContext context)
							throws JsonParseException {
						SimpleDateFormat format = new SimpleDateFormat(pattern);
						String dateStr = json.getAsString();
						try {
							return format.parse(dateStr);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						return null;
					}


				}).setDateFormat(pattern).create();
		if (gson != null) {
			obj = gson.fromJson(jsonStr, cl);
		}
		return (T) obj;
	}

}
