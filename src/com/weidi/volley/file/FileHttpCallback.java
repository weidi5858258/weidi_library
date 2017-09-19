package com.weidi.volley.file;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.weidi.volley.AHttpCallback;
import com.weidi.volley.ACustomerCallback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by weidi on 2017/8/4.
 */

public class FileHttpCallback<T> extends AHttpCallback {

    private static final String TAG = "FileHttpCallback";
    private ACustomerCallback<T> mACustomerCallback;
    private String mUrl;
    // 文件保存的路径
    private String fileSavePath;
    private String fileSaveName;


    private static final int ONSTART = 0x0000;
    private static final int ONPROGRESS = 0x0001;
    private static final int ONSUCCESS = 0x0002;
    private static final int ONFAILED = 0x0003;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (mACustomerCallback == null && mACustomerCallback.isIntercept()) {
                return;
            }

            switch (msg.what) {
                case ONSTART:
                    mACustomerCallback.onStart(msg.arg1);
                    break;

                case ONPROGRESS:
                    mACustomerCallback.onProgress(msg.arg1);
                    break;

                case ONSUCCESS:
                    mACustomerCallback.onSuccess((T) msg.obj);
                    break;

                case ONFAILED:
                    mACustomerCallback.onFailed((String) msg.obj);
                    break;

                default:
            }

            // super.handleMessage(msg);
        }
    };

    @Override
    public void onSuccess(HttpURLConnection httpURLConnection) {
        if (httpURLConnection == null
                && !this.mUrl.startsWith("http://")
                && !this.mUrl.startsWith("https://")) {
            return;
        }

        downloadFile(httpURLConnection);
    }

    @Override
    public void onSuccess(InputStream inputStream) {
        if (inputStream == null
                && !this.mUrl.startsWith("http://")
                && !this.mUrl.startsWith("https://")) {
            return;
        }

        // downloadFile(inputStream);
    }

    @Override
    public void onFailed(final String response) {
        doResult(ONFAILED, response);
    }

    public FileHttpCallback setIDataCallback(ACustomerCallback<T> callback) {
        this.mACustomerCallback = callback;
        return this;
    }

    public FileHttpCallback setURL(String url) {
        this.mUrl = url;
        if (!TextUtils.isEmpty(this.mUrl)) {
            int index = this.mUrl.lastIndexOf("/");
            this.fileSaveName = this.mUrl.subSequence(index + 1, this.mUrl.length()).toString();
            Log.i(TAG, "fileSaveName: " + this.fileSaveName);
        }
        return this;
    }

    public FileHttpCallback setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
        return this;
    }

    public FileHttpCallback setFileSaveName(String fileSaveName) {
        this.fileSaveName = fileSaveName;
        return this;
    }

    private void doResult(int what, String result) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    private void downloadFile(HttpURLConnection httpURLConnection) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            // put inputstream save to local as file
            if (!TextUtils.isEmpty(this.mUrl)
                    && !TextUtils.isEmpty(this.fileSavePath)
                    && !TextUtils.isEmpty(this.fileSaveName)
                    && (this.mUrl.startsWith("http://")
                    || this.mUrl.startsWith("https://"))) {
                File file = new File(this.fileSavePath);
                if (file != null && file.isDirectory()) {
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            this.fileSavePath = null;
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

                    if (TextUtils.isEmpty(this.fileSavePath)) {
                        doResult(ONFAILED, "文件保存的地方没有写权限");
                        return;
                    }

                    File saveFile = new File(this.fileSavePath, this.fileSaveName);
                    int saveFileLength = httpURLConnection.getContentLength();
                    if (saveFile != null
                            && (!saveFile.exists()
                            || (saveFile.exists() && saveFile.length() < saveFileLength))) {
                        if (saveFile.exists()) {
                            Log.i(TAG, "saveFile.length() = " + saveFile.length());
                            saveFile.delete();
                        }
                        byte[] buffer = new byte[1024];
                        int length = -1;
                        fileOutputStream = new FileOutputStream(saveFile);
                        inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                        int tempFileLength = 0;

                        Message msg = mHandler.obtainMessage();
                        msg.what = ONSTART;
                        msg.arg1 = saveFileLength;
                        mHandler.sendMessage(msg);

                        while ((length = inputStream.read(buffer)) != -1) {
                            // Log.i(TAG, "length = " + length);
                            if (mACustomerCallback != null
                                    && mACustomerCallback.isIntercept()) {
                                return;
                            }
                            fileOutputStream.write(buffer, 0, length);
                            fileOutputStream.flush();

                            tempFileLength += length;
                            Message tempMsg = Message.obtain(msg);
                            tempMsg.what = ONPROGRESS;
                            tempMsg.arg1 = tempFileLength;
                            mHandler.sendMessage(tempMsg);
                        }
                    }

                    doResult(ONSUCCESS, "下载完成");
                }
            } else {
                doResult(ONFAILED, "下载地址,文件保存路径,文件名有问题");
            }
        } catch (Exception e) {
            e.printStackTrace();
            doResult(ONFAILED, "下载过程中出现异常");
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
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
    }

}
