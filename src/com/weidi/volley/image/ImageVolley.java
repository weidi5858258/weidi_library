package com.weidi.volley.image;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.weidi.application.WeidiApplication;
import com.weidi.volley.HttpTack;
import com.weidi.volley.ICustomerCallback;
import com.weidi.volley.ThreadPoolManager;

import java.io.File;

import static com.weidi.volley.image.ImageHttpCallback.md5;


/**
 * Created by weidi on 2017/8/4.
 */

/***
 图片加载基本功能已经完成
 现在可以实现的功能有:
 1.设置一个ImageView显示图片,图片大小是ImageView的大小,快速滑动时也不会错乱
 2.如果设置Context而不设置ImageView,也不设置mMustDownloadSourceImage这个布尔值,那么下载的图片是手机屏幕的大小
 3.如果设置了mMustDownloadSourceImage这个布尔值,那么最终保存的图片是原图
 如果图片下载后,用同样的url去下载跟原来图片尺寸大小不同的话,那么需要删除原来的图片才能继续下载不同尺寸的图片
 没有做几级缓存的处理,图片只存在磁盘上,磁盘没有就下载,有就返回.这样做体验上没有什么问题
 */
public class ImageVolley<T> {

    private static final String TAG = "ImageVolley";
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

    public ImageVolley setMustDownloadSourceImage(boolean mustDownloadSourceImage) {
        imageHttpCallback.setMustDownloadSourceImage(mustDownloadSourceImage);
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
        if (isImageExist()) {
            return;
        }
        imageHttpService.setIHttpCallback(imageHttpCallback);
        HttpTack httpTack = new HttpTack(this.imageHttpService);
        ThreadPoolManager.getInstance().execute(httpTack);
    }

    private boolean isImageExist() {
        Context context = imageHttpCallback.getContext();
        ImageView imageView = imageHttpCallback.getImageView();
        String url = imageHttpCallback.getURL();
        String fileSavePath = imageHttpCallback.getFileSavePath();

        if (TextUtils.isEmpty(url)) {
            // 不需要再往下执行
            Log.i(TAG, "ImageVolley: url is null.");
            return true;
        }
        if (TextUtils.isEmpty(fileSavePath)) {
            if (context == null) {
                if (imageView != null) {
                    context = imageView.getContext();
                } else {
                    context = WeidiApplication.getContext();
                }
                if (context == null) {
                    Log.i(TAG, "ImageVolley: context is null.");
                    return true;
                }
            }
            // /storage/emulated/0/Android/data/com.weidi.artifact/cache
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                fileSavePath = context.getExternalCacheDir().getPath();
            } else {
                fileSavePath = context.getCacheDir().getPath();
            }
            imageHttpCallback.setFileSavePath(fileSavePath);
        }
        String md5 = md5(url);
        File pictureFile = new File(fileSavePath, md5 + ".png");
        if (pictureFile.exists()) {
            if (imageView != null && md5.equals(imageView.getTag())) {
                imageView.setImageBitmap(
                        BitmapFactory.decodeFile(pictureFile.getAbsolutePath()));
            }
            // 只要文件存在,就不再执行后面的代码了
            // Log.i(TAG, "isImageExist: " + md5 + " is exist.");
            return true;
        }
        return false;
    }

    /***
     ImageVolley.newInstance()
     .setHandler(mHandler)
     .setImageView(imageView)
     .setURL(url)
     .setIDataCallback(new ICustomerCallback<Bitmap>() {

    @Override public void onSuccess(Bitmap result) {
    Log.d(TAG, "imageView = " + imageView +
    " layoutPosition = " + layoutPosition);
    // customViewHolder.setImageBitmap(R.id.iv_image_icon, result);
    }

    @Override public void onFailed(String response) {
    Log.d(TAG, "imageView = " + imageView +
    " layoutPosition = " + layoutPosition +
    " onFailed(): " + response);
    customViewHolder.setImageResource(
    R.id.iv_image_icon, R.drawable.item_bg);
    }

    @Override public boolean isIntercept() {
    return false;
    }
    })
     .execute();
     */

    /***
     // 下面路径都不能写文件
     if (Environment.getDataDirectory().canWrite()) {
     Log.i(TAG, "Environment.getDataDirectory().canWrite()");
     }
     if (Environment.getDownloadCacheDirectory().canWrite()) {
     Log.i(TAG, "Environment.getDownloadCacheDirectory().canWrite()");
     }
     if (Environment.getExternalStorageDirectory().canWrite()) {
     Log.i(TAG, "Environment.getExternalStorageDirectory().canWrite()");
     }
     if (Environment.getExternalStoragePublicDirectory("pictures").canWrite()) {
     Log.i(TAG, "Environment.getExternalStoragePublicDirectory(\"pictures\")" +
     ".canWrite()");
     }*/

}
