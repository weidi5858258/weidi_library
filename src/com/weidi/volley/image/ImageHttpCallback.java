package com.weidi.volley.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.weidi.imageload.ImageSizeUtils;
import com.weidi.volley.AHttpCallback;
import com.weidi.volley.ICustomerCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.weidi.volley.IHttpService.FILE_NOT_EXISTS;
import static com.weidi.volley.IHttpService.FILE_NOT_READ;
import static com.weidi.volley.IHttpService.RETURN_OBJECT_IS_NULL;

/**
 * Created by weidi on 2017/8/4.
 */

public class ImageHttpCallback<T> extends AHttpCallback {

    private static final String TAG = "FileHttpCallback";
    private ICustomerCallback<T> mICustomerCallback;
    private Handler mHandler;
    private String mUrl;
    // Bitmap保存的路径
    private String fileSavePath;
    // 下面两个至少要设置一个，不然返回的bitmap为null
    // 我现在是这样设计的
    private Context mContext;
    private ImageView mImageView;

    @Override
    public void onSuccess(HttpURLConnection httpURLConnection) {

    }

    @Override
    public void onSuccess(InputStream inputStream) {
        if (inputStream == null
                && !this.mUrl.startsWith("http://")
                && !this.mUrl.startsWith("https://")) {
            loadLocalPictureFile();
            return;
        }

        downloadAndSaveBitmap(inputStream);
    }

    @Override
    public void onFailed(final String response) {
        doResult(response);
    }

    public ImageHttpCallback setIDataCallback(ICustomerCallback<T> callback) {
        this.mICustomerCallback = callback;
        return this;
    }

    public ImageHttpCallback setHandler(Handler handler) {
        this.mHandler = handler;
        if (this.mHandler == null) {
            this.mHandler = new Handler(Looper.getMainLooper());
        }
        return this;
    }

    public ImageHttpCallback setContext(Context context) {
        this.mContext = context;
        return this;
    }

    public ImageHttpCallback setImageView(ImageView imageView) {
        this.mImageView = imageView;
        return this;
    }

    public ImageHttpCallback setURL(String url) {
        this.mUrl = url;
        return this;
    }

    public ImageHttpCallback setFileSavePath(String path) {
        this.fileSavePath = path;
        return this;
    }

    private static String md5(String str) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            digest = md.digest(str.getBytes());
            return bytes2hex02(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String bytes2hex02(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bytes) {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF & b);
            // 每个字节8为，转为16进制标志，2个16进制位
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }

        return sb.toString();
    }

    private void doResult(final Bitmap bitmap) {
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
                    if (bitmap != null) {
                        mICustomerCallback.onSuccess((T) bitmap);
                    } else {
                        // Object is null
                        mICustomerCallback.onFailed(
                                ImageHttpCallback.this.mUrl
                                        + " responseCode: " + RETURN_OBJECT_IS_NULL);
                    }
                }
            });
        }
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

    private void downloadAndSaveBitmap(InputStream inputStream) {
        Bitmap bitmap = null;
        FileOutputStream fileOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] buffer = null;
        try {

            // 加上try是为了产生异常时不影响其他操作
            try {
                // put inputstream save to local as file
                if (!TextUtils.isEmpty(this.mUrl)
                        && !TextUtils.isEmpty(this.fileSavePath)
                        && (this.mUrl.startsWith("http://")
                        || this.mUrl.startsWith("https://"))) {
                    File file = new File(this.fileSavePath);
                    if (file != null && file.isDirectory()) {
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!file.canWrite()) {
                            this.fileSavePath = null;
                            /*Log.i(TAG, file.getAbsolutePath()
                                    + " : This directory cann't write.");*/
                        } else {
                            this.fileSavePath = file.getAbsolutePath();
                            /*Log.i(TAG, file.getAbsolutePath()
                                    + " : Picture is saved this directory.");*/
                        }

                        File pictureFile = new File(this.fileSavePath, md5(this.mUrl) + ".jpg");
                        if (pictureFile != null
                                && (!pictureFile.exists()
                                || (pictureFile.exists() && pictureFile.length() == 0))) {

                            byteArrayOutputStream = new ByteArrayOutputStream();
                            buffer = new byte[1024];
                            int len;
                            while ((len = inputStream.read(buffer)) > -1) {
                                byteArrayOutputStream.write(buffer, 0, len);
                            }
                            byteArrayOutputStream.flush();

                            inputStream = new ByteArrayInputStream(
                                    byteArrayOutputStream.toByteArray());
                            fileOutputStream = new FileOutputStream(pictureFile);
                            while ((len = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, len);
                            }
                            fileOutputStream.flush();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (byteArrayOutputStream != null) {
                inputStream = new ByteArrayInputStream(
                        byteArrayOutputStream.toByteArray());
            }

            inputStream.mark(inputStream.available());
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            // 此时没有被加载到内存中
            bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
            opts.inSampleSize = -1;
            if (mImageView != null) {
                // 获取imageview想要显示的宽和高
                ImageSizeUtils.ImageSize imageViewSize =
                        ImageSizeUtils.getImageViewSize(mImageView);
                opts.inSampleSize = ImageSizeUtils.caculateInSampleSize(
                        opts, imageViewSize.width, imageViewSize.height);
            } else {
                if (mContext != null) {
                    DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
                    opts.inSampleSize = ImageSizeUtils.caculateInSampleSize(
                            // 实际图片的宽高跟手机屏幕的宽高相比较
                            opts, displayMetrics.widthPixels, displayMetrics.heightPixels);
                }
            }
            opts.inJustDecodeBounds = false;
            inputStream.reset();
            if (opts.inSampleSize != -1) {
                bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doResult(bitmap);
    }

    private void loadLocalPictureFile() {
        Bitmap bitmap = null;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            // 加上try是为了产生异常时不影响其他操作
            try {
                // put inputstream save to local as file
                if (!TextUtils.isEmpty(this.mUrl)
                        && !TextUtils.isEmpty(this.fileSavePath)) {
                    File pictureFile = new File(this.mUrl);
                    if (pictureFile != null && pictureFile.isFile() && pictureFile.length() != 0) {
                        if (!pictureFile.exists()) {
                            doResult(FILE_NOT_EXISTS+"");
                            return;
                        }
                        if (!pictureFile.canRead()) {
                            doResult(FILE_NOT_READ+"");
                            return;
                        }

                        File needToSavePictureFile = new File(
                                this.fileSavePath, md5(this.mUrl) + ".jpg");
                        if (needToSavePictureFile != null
                                && (!needToSavePictureFile.exists()
                                || (needToSavePictureFile.exists()
                                && needToSavePictureFile.length() == 0))) {
                            byte[] buffer = new byte[1024];
                            int len;
                            fileInputStream = new FileInputStream(pictureFile);
                            fileOutputStream = new FileOutputStream(needToSavePictureFile);
                            while ((len = fileInputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, len);
                            }
                            fileOutputStream.flush();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            // 此时没有被加载到内存中
            bitmap = BitmapFactory.decodeFile(this.mUrl, opts);
            opts.inSampleSize = -1;
            if (mImageView != null) {
                // 获取imageview想要显示的宽和高
                ImageSizeUtils.ImageSize imageViewSize =
                        ImageSizeUtils.getImageViewSize(mImageView);
                opts.inSampleSize = ImageSizeUtils.caculateInSampleSize(
                        opts, imageViewSize.width, imageViewSize.height);
            } else {
                if (mContext != null) {
                    DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
                    opts.inSampleSize = ImageSizeUtils.caculateInSampleSize(
                            // 实际图片的宽高跟手机屏幕的宽高相比较
                            opts, displayMetrics.widthPixels, displayMetrics.heightPixels);
                }
            }
            opts.inJustDecodeBounds = false;
            if (opts.inSampleSize != -1) {
                bitmap = BitmapFactory.decodeFile(this.mUrl, opts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doResult(bitmap);
    }

}
