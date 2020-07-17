package com.weidi.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by root on 17-2-23.
 */

public class MyToast {

    private volatile static Toast mToast;
    private static Context mContext;
    private static final int MSG_SHOW_TEXT = 1;

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            // 不要直接使用getInstance().cancel();
            //mHandler.removeCallbacks(runnable);
            getInstance().setText((String) msg.obj);
            if (msg.arg1 == 0) {
                getInstance().setDuration(Toast.LENGTH_SHORT);
            } else {
                getInstance().setDuration(Toast.LENGTH_LONG);
            }
            getInstance().show();
            //mHandler.postDelayed(runnable, 5000);
        }
    };

    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getInstance().cancel();
        }
    };

    /**
     * 1.在Application中先设置一下
     *
     * @param context
     */
    public static void setContext(Context context) {
        mContext = context;
    }

    /**
     * 2.在Application中先调用一下
     *
     * @return
     */
    public static Toast getInstance() {
        if (mToast == null) {
            synchronized (MyToast.class) {
                if (mToast == null) {
                    if (mContext == null) {
                        throw new NullPointerException("MyToast mContext is null.");
                    }
                    mToast = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
                    mToast.setGravity(
                            //Gravity.CENTER_HORIZONTAL | Gravity.TOP,
                            Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM,
                            0,
                            100);
                }
            }
        }
        return mToast;
    }

    public static void show(String text) {
        show(text, 0);
    }

    public static void show(int strId) {
        if (mContext != null) {
            show(mContext.getString(strId), 0);
        }
    }

    public static void show(int strId, int duration) {
        if (mContext != null) {
            show(mContext.getString(strId), duration);
        }
    }

    public static void show(String text, int duration) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            getInstance().setText(text);
            if (duration == 0) {
                getInstance().setDuration(Toast.LENGTH_SHORT);
            } else {
                getInstance().setDuration(Toast.LENGTH_LONG);
            }
            getInstance().show();
        } else {
            mHandler.removeMessages(MSG_SHOW_TEXT);
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_SHOW_TEXT;
            msg.obj = text;
            msg.arg1 = duration;
            // 两条消息之间的显示间隔时间最好大于5ms,比如6ms
            mHandler.sendMessageDelayed(msg, 5);
        }


    }

    public static void dismiss() {
        getInstance().cancel();
    }

}
