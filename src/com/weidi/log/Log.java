package com.weidi.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Log {

    /**
     * if(Log.isSwitch()){
     * StackTraceElement[] mStackTraceElement = Thread.currentThread().getStackTrace();
     * if (mStackTraceElement != null && mStackTraceElement.length >= 2) {
     * StackTraceElement mStackTraceElement1 = (StackTraceElement) mStackTraceElement[1];
     * StackTraceElement mStackTraceElement2 = (StackTraceElement) mStackTraceElement[2];
     * Log.d(mStackTraceElement1.getClassName()+"."+mStackTraceElement1.getMethodName()
     * +" <--- " + mStackTraceElement2.getClassName()
     * + "." + mStackTraceElement2.getMethodName()
     * + " " + mStackTraceElement2.getLineNumber());
     * }
     * }
     */
    private volatile static boolean mSwitch = true;
    private volatile static String TAG = "com.weidi.artifact";
    private static BufferedWriter mBufferedWriter;

    public static void init() {
        mSwitch = true;
        try {
            File file = new File("/data/data/com.weidi.artifact/files/artifact.log");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            mBufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true)));
        } catch (Exception e) {
            e.printStackTrace();
            mBufferedWriter = null;
        }
    }

    public static boolean isSwitch() {
        return mSwitch;
    }

    public static void setSwitch(boolean isOpenLog) {
        mSwitch = isOpenLog;
    }

    /**********************************************************************************************/

    public static void v(String msg) {
        if (mSwitch && msg != null)
            android.util.Log.v(TAG, "---------->" + msg);
    }

    public static void v(String tag, String msg) {
        if (mSwitch && msg != null)
            android.util.Log.v(tag == null ? TAG : tag, "---------->" + msg);
    }

    public static void d(String msg) {
        if (mSwitch && msg != null)
            android.util.Log.d(TAG, "---------->" + msg);
    }

    public static void d(String tag, String msg) {
        if (mSwitch && msg != null)
            android.util.Log.d(tag == null ? TAG : tag, "---------->" + msg);
    }

    public static void i(String msg) {
        if (mSwitch && msg != null)
            android.util.Log.i(TAG, "---------->" + msg);
    }

    public static void i(String tag, String msg) {
        if (mSwitch && msg != null)
            android.util.Log.i(tag == null ? TAG : tag, "---------->" + msg);
    }

    public static void w(String msg) {
        if (mSwitch && msg != null)
            android.util.Log.w(TAG, "---------->" + msg);
    }

    public static void w(String tag, String msg) {
        if (mSwitch && msg != null)
            android.util.Log.w(tag == null ? TAG : tag, "---------->" + msg);
    }

    public static void e(String msg) {
        if (mSwitch && msg != null)
            android.util.Log.e(TAG, "---------->" + msg);
    }

    public static void e(String tag, String msg) {
        if (mSwitch && msg != null)
            android.util.Log.e(tag == null ? TAG : tag, "---------->" + msg);
    }

    /**********************************************************************************************/

    public static void writeToFile(String msg) {
        try {
            if (mBufferedWriter != null) {
                mBufferedWriter.write(msg);
                mBufferedWriter.newLine();//换行
                mBufferedWriter.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**********************************************************************************************/

    public static void e(String tag, Throwable tr) {
        e(tag, tr == null ? "" : tr.getMessage());
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (mSwitch && msg != null)
            android.util.Log.e(tag == null ? TAG : tag, msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        w(tag, tr == null ? "" : tr.getMessage());
    }

    public static void printStackTraceAndMore(Throwable e) {
        e.printStackTrace();
    }

}
