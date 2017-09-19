package com.weidi.volley.json;

import com.alibaba.fastjson.JSON;
import com.weidi.volley.AHttpService;
import com.weidi.volley.IHttpCallback;
import com.weidi.volley.IHttpService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by weidi on 2017/8/4.
 */

public class JsonHttpService extends AHttpService {

    private String mUrl;
    private byte[] mRequestData;
    private IHttpCallback mIHttpCallback;

    @Override
    public IHttpService setURL(String url) {
        this.mUrl = url;
        return this;
    }

    @Override
    public IHttpService setRequest(Object requestData) {
        if (requestData != null) {
            // 把request转化成String
            String jsonRequest = JSON.toJSONString(requestData);
            try {
                if (jsonRequest != null) {
                    mRequestData = jsonRequest.getBytes("UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public IHttpService setIHttpCallback(IHttpCallback callback) {
        this.mIHttpCallback = callback;
        return this;
    }

    @Override
    public void execute() {

        postRequest();

    }

    private void postRequest() {
        URL url = null;
        HttpURLConnection urlConnection = null;
        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (this.mUrl == null) {
                if (mIHttpCallback != null) {
                    mIHttpCallback.onFailed(URL_IS_NULL+"");
                }
                return;
            }
            /*if (this.mRequestData == null) {
                if (mIHttpCallback != null) {
                    mIHttpCallback.onFailed(2);
                }
                return;
            }*/
            url = new URL(this.mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(6000);// 连接的超时时间
            urlConnection.setUseCaches(false);// 不使用缓存
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setReadTimeout(3000);// 响应的超时时间
            urlConnection.setDoInput(true);// 设置这个连接是否可以写入数据
            urlConnection.setDoOutput(true);// 设置这个连接是否可以输出数据
            urlConnection.setRequestMethod("POST");// 设置请求的方式
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            urlConnection.connect();
            if (mRequestData != null) {
                outputStream = urlConnection.getOutputStream();
                bufferedOutputStream = new BufferedOutputStream(outputStream);
                bufferedOutputStream.write(mRequestData);
                bufferedOutputStream.flush();// 刷新缓冲区，发送数据
            }
            InputStream inputStream = null;
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                if (mIHttpCallback != null) {
                    mIHttpCallback.onSuccess(inputStream);
                }
            } else {
                if (mIHttpCallback != null) {
                    mIHttpCallback.onFailed(this.mUrl+" responseCode: "+responseCode);
                }
            }
        } catch (Exception e) {
            if (mIHttpCallback != null) {
                mIHttpCallback.onFailed(this.mUrl+" responseCode: "+HTTP_EXCEPTION);
            }
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

}
