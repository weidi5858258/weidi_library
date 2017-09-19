package com.weidi.volley;

/**
 * Created by weidi on 2017/8/4.
 */
/***
 不同的子类实现这个接口，完成不同的功能
 子类有些共同的操作可以放在这里
 */
public abstract class AHttpService implements IHttpService {

    @Override
    public IHttpService setRequest(Object requestData) {
        return this;
    }

}
