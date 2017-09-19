package com.weidi.volley;

/**
 * Created by weidi on 2017/8/3.
 */

public final class HttpTack<R> implements Runnable {

    private IHttpService mIHttpService;

    public HttpTack(IHttpService service) {
        this.mIHttpService = service;
    }

    @Override
    public void run() {
        // 执行耗时任务
        mIHttpService.execute();
    }

}
