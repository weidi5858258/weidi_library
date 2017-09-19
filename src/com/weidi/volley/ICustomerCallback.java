package com.weidi.volley;

/**
 * Created by weidi on 2017/8/4.
 */

public interface ICustomerCallback<T> {

    void onSuccess(T result);

    void onFailed(String response);

    /***
     * 作用：例如Activity退出的时候任务还在执行，那么这个任务就不需要再返回结果了
     * @return 默认为false，当设置true时表示拦截掉，不需要返回结果
     */
    boolean isIntercept();

}
