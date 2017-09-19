package com.weidi.volley.image;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;

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
public class ImageVolley<T> {

    private ImageHttpService imageHttpService;
    private ImageHttpCallback<T> imageHttpCallback;

    private ImageVolley() {
        imageHttpService = new ImageHttpService();
        imageHttpCallback = new ImageHttpCallback();
    }

    public static ImageVolley newInstance() {
        ImageVolley imageVolley = new ImageVolley();
        return imageVolley;
    }

    public ImageVolley setURL(String url) {
        imageHttpService.setURL(url);
        imageHttpCallback.setURL(url);
        return this;
    }

    public ImageVolley setHandler(Handler handler) {
        imageHttpCallback.setHandler(handler);
        return this;
    }

    public ImageVolley setContext(Context context) {
        imageHttpCallback.setContext(context);
        return this;
    }

    public ImageVolley setImageView(ImageView imageView) {
        imageHttpCallback.setImageView(imageView);
        return this;
    }

    public ImageVolley setFileSavePath(String path) {
        imageHttpCallback.setFileSavePath(path);
        return this;
    }

    /***
     * 一般是Bitmap
     * @param callback
     * @return
     */
    public ImageVolley setIDataCallback(ICustomerCallback<T> callback) {
        imageHttpCallback.setIDataCallback(callback);
        return this;
    }

    /***
     最后一步调用
     */
    public void execute() {
        imageHttpService.setIHttpCallback(imageHttpCallback);
        HttpTack httpTack = new HttpTack(this.imageHttpService);
        ThreadPoolManager.getInstance().execute(httpTack);
    }

}
