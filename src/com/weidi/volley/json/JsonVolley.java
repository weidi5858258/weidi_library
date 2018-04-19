package com.weidi.volley.json;

import android.os.Handler;

import com.weidi.volley.HttpTack;
import com.weidi.volley.ICustomerCallback;
import com.weidi.volley.ThreadPoolManager;

/**
 * Created by weidi on 2017/8/4.
 */

/***
 * 到时加上拦截器
 * 一步步优化
 * @param <T>
 */
public class JsonVolley<T> {

    private JsonHttpService jsonHttpService;
    private JsonHttpCallback<T> jsonHttpCallback;

    private JsonVolley() {
        jsonHttpService = new JsonHttpService();
        jsonHttpCallback = new JsonHttpCallback();
    }

    public static JsonVolley newInstance() {
        JsonVolley jsonVolley = new JsonVolley();
        return jsonVolley;
    }

    public JsonVolley setURL(String url) {
        jsonHttpService.setURL(url);
        return this;
    }

    public JsonVolley setRequest(Object request) {
        jsonHttpService.setRequest(request);
        return this;
    }

    public JsonVolley setHandler(Handler handler) {
        jsonHttpCallback.setHandler(handler);
        return this;
    }

    public JsonVolley setResponseClass(Class<T> responseClass) {
        jsonHttpCallback.setResponseClass(responseClass);
        return this;
    }

    public JsonVolley setIDataCallback(ICustomerCallback<T> callback) {
        jsonHttpCallback.setIDataCallback(callback);
        return this;
    }

    public void execute() {
        jsonHttpService.setIHttpCallback(jsonHttpCallback);
        HttpTack httpTack = new HttpTack(this.jsonHttpService);
        ThreadPoolManager.getInstance().execute(httpTack);
    }

    /***
     JsonVolley.newInstance()
     .setHandler(mHandler)
     .setURL("http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=218.4.255.255")
     .setRequest(null)
     .setResponseClass(ResponseData.class)
     .setIDataCallback(new ACustomerCallback<ResponseData>() {

    @Override public void onSuccess(ResponseData result) {
    }

    @Override public void onFailed(String response) {
    }

    @Override public boolean isIntercept() {
    // if (MainActivity.this.isDestroyed()) {
    // return true;
    // }
    return false;
    }
    })
     .execute();
     */

}
