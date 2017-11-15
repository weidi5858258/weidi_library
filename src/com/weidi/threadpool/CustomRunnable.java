package com.weidi.threadpool;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;

public class CustomRunnable implements Runnable {

    private static final String TAG = "CustomRunnable";

    private static final int RUNBEFORE = 0;
    private static final int RUNAFTER = 1;
    private static final int RUNERROR = 2;
    private static final int ONPROGRESSUPDATE = 3;

    private final Object mObject = new Object();
    private boolean mCanNext;
    private static Message mMessage;

    private InnerHandler mHandler = null;
    private CallBack mCallBack = null;

    private int runBeforeSleepTime = 0;
    private int runAfterSleepTime = 0;

    public static interface CallBack {

        void runBefore();

        Object running();

        void onProgressUpdate(Object object);

        void runAfter(Object object);

        void runError();

    }

    public CustomRunnable() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("CustomRunnable Object isn't created at main thread!!!");
        }
        if (mHandler == null) {
            // 每个任务都是创建一次Handler对象,需要改进一下.改为一个应用只创建一个Handler对象.
            mHandler = new InnerHandler(this);
        }
    }

    @Override
    public void run() {
        if (mCallBack != null) {
            try {
                // runBefore
                mHandler.sendEmptyMessage(RUNBEFORE);

                synchronized (mObject) {
                    while (!mCanNext) {
                        try {
                            mObject.wait();
                        } catch (Exception e) {
                        }
                    }
                }

                if (runBeforeSleepTime > 0) {
                    SystemClock.sleep(runBeforeSleepTime);
                    runBeforeSleepTime = 0;
                }

                // running
                Object object = null;
                if (mCanNext) {
                    object = mCallBack.running();
                }

                // runAfter
                Message msg = getMessage();
                msg.what = RUNAFTER;
                msg.obj = object;
                mMessage = msg;
                mHandler.sendMessageDelayed(msg, runAfterSleepTime);
            } catch (Exception e) {
                mHandler.sendEmptyMessage(RUNERROR);
                Log.e(TAG, "CustomRunnable:run(): " + e);
                e.printStackTrace();
            } finally {
                //                release();
            }
        }
    }

    /***
     * 异步执行的,也就是说子线程向主线程"发送命令"需要主线程执行一些任务时,
     * 子线程发送完命令就接着执行下面的工作,并不等待主线程执行完后再执行下面的工作.
     * @param object
     */
    public final void publishProgress(Object object) {
        Message msg = getMessage();
        msg.what = ONPROGRESSUPDATE;
        msg.obj = object;
        mMessage = msg;
        mHandler.sendMessage(msg);
    }

    public CustomRunnable setCallBack(CallBack callBack) {
        mCallBack = callBack;
        return this;
    }

    public CustomRunnable setRunBeforeSleepTime(int seconds) {
        if (seconds < 0) {
            runBeforeSleepTime = 0;
        } else {
            runBeforeSleepTime = seconds;
        }
        return this;
    }

    public CustomRunnable setRunAfterSleepTime(int seconds) {
        if (seconds < 0) {
            runAfterSleepTime = 0;
        } else {
            runAfterSleepTime = seconds;
        }
        return this;
    }

    private Message getMessage() {
        Message msg = null;
        if (mMessage != null) {
            msg = Message.obtain(mMessage);
        } else {
            msg = mHandler.obtainMessage();
        }
        return msg;
    }

    private static class InnerHandler extends Handler {

        private WeakReference<CustomRunnable> mCustomRunnable;

        private InnerHandler(CustomRunnable customRunnable) {
            mCustomRunnable = new WeakReference<CustomRunnable>(customRunnable);
        }

        @Override
        public void handleMessage(Message msg) {
            CustomRunnable customRunnable = mCustomRunnable.get();
            if (customRunnable == null || customRunnable.mCallBack == null) {
                return;
            }

            switch (msg.what) {
                case RUNBEFORE:
                    customRunnable.mCallBack.runBefore();
                    // customRunnable.runBeforeSleepTime = 0;// don't set
                    if (!customRunnable.mCanNext) {
                        synchronized (customRunnable.mObject) {
                            customRunnable.mObject.notifyAll();
                            customRunnable.mCanNext = true;
                        }
                    }
                    break;
                case RUNAFTER:
                    customRunnable.mCallBack.runAfter(msg.obj);
                    customRunnable.runBeforeSleepTime = 0;
                    customRunnable.runAfterSleepTime = 0;

                    customRunnable.mCanNext = false;
                    break;
                case RUNERROR:
                    customRunnable.mCallBack.runError();
                    customRunnable.runBeforeSleepTime = 0;
                    customRunnable.runAfterSleepTime = 0;

                    customRunnable.mCanNext = false;
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

    /**
     *
     */
    public void cancel() {
        if (mCallBack != null) {
            mHandler.removeMessages(RUNBEFORE);
            mHandler.removeMessages(RUNAFTER);
            mHandler.removeMessages(RUNERROR);
            mHandler.removeMessages(ONPROGRESSUPDATE);
            mCallBack = null;
            mHandler = null;
        }
    }

    private void release() {

    }

    /**
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
