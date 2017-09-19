package com.weidi.volley;

import android.os.Handler;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by weidi on 2017/8/3.
 */

public interface IHttpCallback<T> {

    void onSuccess(HttpURLConnection httpURLConnection);

    void onSuccess(InputStream inputStream);

    void onFailed(String response);

}
