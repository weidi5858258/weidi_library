package com.weidi.volley.image;

import android.text.TextUtils;

import com.weidi.volley.AHttpService;
import com.weidi.volley.IHttpCallback;
import com.weidi.volley.IHttpService;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by weidi on 2017/8/4.
 */

/***
 加载地方：
 1.网络
 2.手机存储卡
 */
public class ImageHttpService extends AHttpService {

    private String mUrl;
    private IHttpCallback mIHttpCallback;

    @Override
    public IHttpService setURL(String url) {
        this.mUrl = url;
        return this;
    }

    @Override
    public IHttpService setIHttpCallback(IHttpCallback callback) {
        this.mIHttpCallback = callback;
        return this;
    }

    @Override
    public void execute() {
        if (TextUtils.isEmpty(this.mUrl)) {
            if (mIHttpCallback != null) {
                mIHttpCallback.onFailed(URL_IS_NULL+"");
            }
            return;
        }
        if (this.mUrl.startsWith("http://") || this.mUrl.startsWith("https://")) {
            requestHttp();
        } else {
            requestPath();
        }
    }

    private void requestHttp() {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(this.mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(30000);// 连接的超时时间
            urlConnection.setReadTimeout(15000);// 响应的超时时间
            urlConnection.setUseCaches(false);// 不使用缓存
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (mIHttpCallback != null) {
                    mIHttpCallback.onSuccess(
                            new BufferedInputStream(urlConnection.getInputStream()));
                }
            } else {
                if (mIHttpCallback != null) {
                    mIHttpCallback.onFailed(this.mUrl+" responseCode: "+responseCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mIHttpCallback != null) {
                mIHttpCallback.onFailed(this.mUrl+" responseCode: "+HTTP_EXCEPTION);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void requestPath() {
        if (mIHttpCallback != null) {
            mIHttpCallback.onSuccess((InputStream) null);
        }
    }

}
