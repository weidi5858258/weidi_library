package com.weidi.volley.file;

import com.weidi.volley.ACustomerCallback;
import com.weidi.volley.HttpTack;
import com.weidi.volley.ThreadPoolManager;


/**
 * Created by weidi on 2017/8/4.
 */

/***
 * 到时加上拦截器
 * 一步步优化
 * @param <T>
 */
public class FileVolley<T> {

    private FileHttpService fileHttpService;
    private FileHttpCallback<T> fileHttpCallback;

    private FileVolley() {
        fileHttpService = new FileHttpService();
        fileHttpCallback = new FileHttpCallback();
    }

    public static FileVolley newInstance() {
        FileVolley fileVolley = new FileVolley();
        return fileVolley;
    }

    public FileVolley setURL(String url) {
        fileHttpService.setURL(url);
        fileHttpCallback.setURL(url);
        return this;
    }

    /*public FileVolley setHandler(Handler handler) {
        fileHttpCallback.setHandler(handler);
        return this;
    }*/

    public FileVolley setFileSavePath(String fileSavePath) {
        fileHttpCallback.setFileSavePath(fileSavePath);
        return this;
    }

    public FileVolley setFileSaveName(String fileSaveName) {
        fileHttpCallback.setFileSaveName(fileSaveName);
        return this;
    }

    /***
     * 一般是Bitmap
     * @param callback
     * @return
     */
    public FileVolley setIDataCallback(ACustomerCallback<T> callback) {
        fileHttpCallback.setIDataCallback(callback);
        return this;
    }

    /***
     最后一步调用
     */
    public void execute() {
        fileHttpService.setIHttpCallback(fileHttpCallback);
        HttpTack httpTack = new HttpTack(this.fileHttpService);
        ThreadPoolManager.getInstance().execute(httpTack);
    }

    /***
     FileVolley.newInstance()
     .setURL("http://101.44.1.135/hotfiles/1091000006CBD951/101.44.1.125/files/9091000006CBD951
     /dldir1.qq.com/weixin/Windows/WeChatSetup.exe")
     .setFileSavePath(fileSavePath.getAbsolutePath() + "/Pictures")
     .setFileSaveName(i+"_AutoVue20.2.1.0.1403085617.exe")
     .setIDataCallback(new ACustomerCallback<String>() {

    @Override public void onStart(int fileLength) {
    }

    @Override public void onProgress(int progress) {
    }

    @Override public void onSuccess(String result) {
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
