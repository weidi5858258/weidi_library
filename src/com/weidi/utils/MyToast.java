package com.weidi.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.weidi.library.R;

/**
 * Created by root on 17-2-23.
 */

public class MyToast {

    private static final String TAG = MyToast.class.getSimpleName();
    private volatile static Toast sToast;
    private static Context mContext;
    private static final int MSG_SHOW_TEXT = 1;
    private static final int MSG_REMOVE_VIEW = 2;

    private static Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_SHOW_TEXT:
                    if (!mIsAddedView) {
                        addView();
                    }
                    if (mTextView != null) {
                        mTextView.setText((String) msg.obj);

                        mUiHandler.removeMessages(MSG_REMOVE_VIEW);
                        mUiHandler.sendEmptyMessageDelayed(MSG_REMOVE_VIEW, 1000);
                    }
                    break;
                case MSG_REMOVE_VIEW:
                    removeView();
                    break;
                default:
                    break;
            }
            // 不要直接使用getInstance().cancel();
            //mHandler.removeCallbacks(runnable);
            /*getInstance().setText((String) msg.obj);
            if (msg.arg1 == Toast.LENGTH_SHORT) {
                getInstance().setDuration(Toast.LENGTH_SHORT);
            } else {
                getInstance().setDuration(Toast.LENGTH_LONG);
            }
            getInstance().show();*/
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
        if (sToast == null) {
            synchronized (MyToast.class) {
                if (sToast == null) {
                    if (mContext == null) {
                        throw new NullPointerException("MyToast mContext is null.");
                    }
                    sToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
                    sToast.setGravity(
                            //Gravity.CENTER_HORIZONTAL | Gravity.TOP,
                            Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM,
                            0,
                            100);
                }
            }
        }

        return sToast;
    }

    public static void show(String text) {
        show(text, Toast.LENGTH_SHORT);
    }

    public static void show(int strId) {
        if (mContext != null) {
            show(mContext.getString(strId), Toast.LENGTH_SHORT);
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
            if (!mIsAddedView) {
                addView();
            }
            if (mTextView != null) {
                mTextView.setText(text);

                mUiHandler.removeMessages(MSG_REMOVE_VIEW);
                mUiHandler.sendEmptyMessageDelayed(MSG_REMOVE_VIEW, 1000);
            }
        } else {
            mUiHandler.removeMessages(MSG_SHOW_TEXT);
            Message msg = mUiHandler.obtainMessage();
            msg.what = MSG_SHOW_TEXT;
            msg.obj = text;
            // 两条消息之间的显示间隔时间最好大于5ms,比如6ms
            // 如果两条消息显示间隔时间大于8秒,那么这些消息才能显示
            mUiHandler.sendMessageDelayed(msg, 8);
        }

        /*if (Looper.getMainLooper() == Looper.myLooper()) {
            getInstance().setText(text);
            if (duration == Toast.LENGTH_SHORT) {
                getInstance().setDuration(Toast.LENGTH_SHORT);
            } else {
                getInstance().setDuration(Toast.LENGTH_LONG);
            }
            getInstance().show();
        } else {
            mUiHandler.removeMessages(MSG_SHOW_TEXT);
            Message msg = mUiHandler.obtainMessage();
            msg.what = MSG_SHOW_TEXT;
            msg.obj = text;
            msg.arg1 = duration;
            // 两条消息之间的显示间隔时间最好大于5ms,比如6ms
            mUiHandler.sendMessageDelayed(msg, 50);
        }*/
    }

    public static void dismiss() {
        getInstance().cancel();
    }

    //////////////////////////////////////////////////////////////////////

    private static boolean mIsAddedView = false;
    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    private static View mView;
    private static TextView mTextView;

    // 需要动态申请权限的
    private static synchronized void addView() {
        if (mContext == null || mIsAddedView) {
            return;
        }

        UiModeManager uiModeManager =
                (UiModeManager) mContext.getSystemService(Context.UI_MODE_SERVICE);
        int whatIsDevice = uiModeManager.getCurrentModeType();

        mIsAddedView = true;
        LayoutInflater inflate = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflate.inflate(R.layout.transient_notification, null);
        mTextView = (TextView) mView.findViewById(R.id.message);

        mWindowManager =
                (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        // 下面两个的作用都是使弹出窗口的背景透明(本来是黑色的)
        // mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity = Gravity.CENTER_HORIZONTAL + Gravity.TOP;
        if (whatIsDevice != Configuration.UI_MODE_TYPE_WATCH) {
            //mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.height = 80;
        } else {
            mLayoutParams.width = 250;
            mLayoutParams.height = 50;
        }
        mWindowManager.addView(mView, mLayoutParams);
    }

    private static synchronized void removeView() {
        if (mIsAddedView && mView != null) {
            mWindowManager.removeView(mView);
            mIsAddedView = false;
        }
    }

}
