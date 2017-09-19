package com.weidi.volley;

/**
 * Created by weidi on 2017/8/3.
 */

public interface IHttpService {

    int HTTP_EXCEPTION = -1;

    int URL_IS_NULL = 1;

    int RETURN_OBJECT_IS_NULL = 2;

    int FILE_NOT_EXISTS = 3;

    int FILE_NOT_READ = 3;

    int IMAGE_LOADER_TYPE_HTTP = 1000;
    int IMAGE_LOADER_TYPE_PATH = 1001;

    IHttpService setURL(String url);

    IHttpService setRequest(Object requestData);

    IHttpService setIHttpCallback(IHttpCallback callback);

    void execute();

}
