package com.weidi.threadpool;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;

public class CustomRunnable2 implements Runnable {

    private static final String TAG = "CustomRunnable";

    private static final int RUNBEFORE = 0;
    private static final int RUNAFTER = 1;
    private static final int RUNERROR = 2;
    private static final int ONPROGRESSUPDATE = 3;

    private InnerHandler mHandler = null;
    private CallBack mCallBack = null;

    private int runBeforeSleepTime = 0;
    private int runAfterSleepTime = 0;

    public static interface CallBack {
        /**
         * 主线程
         */
        void runBefore();

        /**
         * 子线程
         */
        Object running();

        /**
         * 主线程
         */
        void onProgressUpdate(Object object);

        /**
         * 主线程
         */
        void runAfter(Object object);

        /**
         * 主线程
         */
        void runError();
    }

    public CustomRunnable2() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("CustomRunnable对象的创建不是在主线程中进行!!!");
        }
        if (mHandler == null) {
            mHandler = new InnerHandler(this);
        }
    }

    @Override
    public void run() {
        if (mCallBack != null) {
            try {
                mHandler.sendEmptyMessage(RUNBEFORE);
                if (runBeforeSleepTime != 0) {
                    SystemClock.sleep(runBeforeSleepTime);
                    runBeforeSleepTime = 0;
                }

                Object object = mCallBack.running();

                Message msg = mHandler.obtainMessage();
                msg.what = RUNAFTER;
                msg.obj = object;
                mHandler.sendMessageDelayed(msg, runAfterSleepTime);
            } catch (Exception e) {
                mHandler.sendEmptyMessage(RUNERROR);
                Log.e(TAG, "CustomRunnable:run()方法出现异常: " + e);
            } finally {
                //                release();
            }
        }
    }

    public final void publishProgress(Object object) {
        Message msg = mHandler.obtainMessage();
        msg.what = ONPROGRESSUPDATE;
        msg.obj = object;
        mHandler.sendMessage(msg);
    }

    public CustomRunnable2 setCallBack(CallBack callBack) {
        mCallBack = callBack;
        return this;
    }

    public CustomRunnable2 setRunBeforeSleepTime(int seconds) {
        if (seconds < 0) {
            runBeforeSleepTime = 0;
        } else {
            runBeforeSleepTime = seconds;
        }
        return this;
    }

    public CustomRunnable2 setRunAfterSleepTime(int seconds) {
        if (seconds < 0) {
            runAfterSleepTime = 0;
        } else {
            runAfterSleepTime = seconds;
        }
        return this;
    }

    private static class InnerHandler extends Handler {

        private WeakReference<CustomRunnable2> mCustomRunnable;

        private InnerHandler(CustomRunnable2 customRunnable) {
            mCustomRunnable = new WeakReference<CustomRunnable2>(customRunnable);
        }

        @Override
        public void handleMessage(Message msg) {
            CustomRunnable2 customRunnable = mCustomRunnable.get();
            if (customRunnable == null || customRunnable.mCallBack == null) {
                return;
            }

            switch (msg.what) {
                case RUNBEFORE:
                    customRunnable.mCallBack.runBefore();
                    customRunnable.runBeforeSleepTime = 0;
                    break;
                case RUNAFTER:
                    customRunnable.mCallBack.runAfter(msg.obj);
                    customRunnable.runAfterSleepTime = 0;
                    break;
                case RUNERROR:
                    customRunnable.mCallBack.runError();
                    customRunnable.runBeforeSleepTime = 0;
                    customRunnable.runAfterSleepTime = 0;
                    break;
                case ONPROGRESSUPDATE:
                    customRunnable.mCallBack.onProgressUpdate(msg.obj);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private void release() {

    }

    /**
     直接复制使用
     final CustomRunnable mCustomRunnable = new CustomRunnable();
     mCustomRunnable.setCallBack(
     new CustomRunnable.CallBack() {

    @Override public void runBefore() {

    }

    @Override public Object running() {

    return null;
    }

    @Override public void onProgressUpdate(Object object) {

    }

    @Override public void runAfter(Object object) {

    }

    @Override public void runError() {

    }

    });
     ThreadPool.getCachedThreadPool().execute(mCustomRunnable);
     */

}
