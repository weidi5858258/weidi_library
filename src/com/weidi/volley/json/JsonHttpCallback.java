package com.weidi.volley.json;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.weidi.volley.AHttpCallback;
import com.weidi.volley.ICustomerCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import static com.weidi.volley.IHttpService.HTTP_EXCEPTION;
import static com.weidi.volley.IHttpService.RETURN_OBJECT_IS_NULL;

/**
 * Created by weidi on 2017/8/4.
 */

public class JsonHttpCallback<T> extends AHttpCallback {

    private Class<T> responseClass;
    private ICustomerCallback<T> mICustomerCallback;
    private Handler mHandler;

    @Override
    public void onSuccess(HttpURLConnection httpURLConnection) {

    }

    @Override
    public void onSuccess(InputStream inputStream) {
        if (mICustomerCallback != null) {
            if (mICustomerCallback.isIntercept()) {
                return;
            }
        }

        String content = getContent(inputStream);
        if (content == null) {
            doResult("responseCode: " + RETURN_OBJECT_IS_NULL);
            return;
        }

        final T response = JSON.parseObject(content, responseClass);

        if (mICustomerCallback != null) {
            if (this.mHandler == null) {
                this.mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (response != null) {
                        mICustomerCallback.onSuccess(response);
                    } else {
                        // Object is null
                        mICustomerCallback.onFailed("responseCode: " + RETURN_OBJECT_IS_NULL);
                    }
                }
            });
        }
    }

    @Override
    public void onFailed(final String response) {
        doResult(response);
    }

    public JsonHttpCallback setResponseClass(Class<T> responseClass) {
        this.responseClass = responseClass;
        return this;
    }

    public JsonHttpCallback setIDataCallback(ICustomerCallback<T> callback) {
        this.mICustomerCallback = callback;
        return this;
    }

    public JsonHttpCallback setHandler(Handler handler) {
        this.mHandler = handler;
        if (this.mHandler == null) {
            this.mHandler = new Handler(Looper.getMainLooper());
        }
        return this;
    }

    private String getContent(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String aLine = null;
            while ((aLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(aLine);
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            doResult("responseCode: "+HTTP_EXCEPTION);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void doResult(final String response) {
        if (mICustomerCallback != null) {
            if (this.mHandler == null) {
                this.mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (mICustomerCallback.isIntercept()) {
                        return;
                    }
                    mICustomerCallback.onFailed(response);
                }
            });
        }
    }


}
