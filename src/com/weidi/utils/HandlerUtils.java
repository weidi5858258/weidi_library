package com.weidi.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/***
 * 子线程向主线程传一个对象时现在不适用,
 * 因为msg.obj这个属性已经被使用掉了.
 */
public class HandlerUtils {

    /***
     * 在Application中初始化
     * @param looper
     */
    public static void init(Looper looper) {
        MyHandler.setLooper(looper);
        MyHandler.getInstance();
    }

    /***
     * 一般使用在Activity、Fragment、Service中
     * 在其他类（没有生命周期的类）中使用时，
     * 使用完这个类，需要主动调用一下unregister()方法
     * @param object
     * @param callback
     */
    public static void register(Object object, Handler.Callback callback) {
        MyHandler.getInstance().addCallback(object, callback);
    }

    /***
     * 在Activity、Fragment、Service中的onDestroy()方法中调用，
     * 传递this就行了
     * @param object
     */
    public static void unregister(Object object) {
        MyHandler.getInstance().exit(object);
    }

    /***
     * 想得到一个消息使用这个方法
     * @return
     */
    public static Message getMessage() {
        return MyHandler.getInstance().getMsg();
    }

    public static boolean sendMessageSync(Message msg) {
        return MyHandler.getInstance().sendMsgSync(msg);
    }

    public static boolean sendMessageAsync(Message msg) {
        return MyHandler.getInstance().sendMsg(msg);
    }

    public static boolean sendEmptyMessageSync(Object object, int what) {
        return MyHandler.getInstance().sendEmptyMsgSync(object, what);
    }

    public static boolean sendEmptyMessageAsync(Object object, int what) {
        return MyHandler.getInstance().sendEmptyMsgAsync(object, what);
    }

    /***
     *
     * @param msg
     * @param delayMillis 一个时间段，不是一个时刻点
     * @return
     */
    public static boolean sendMessageDelayed(Message msg, long delayMillis) {
        return MyHandler.getInstance().sendMsgDelayed(msg, delayMillis);
    }

    public static boolean sendEmptyMessageDelayed(Object object, int what, long delayMillis) {
        return MyHandler.getInstance().sendEmptyMsgDelayed(object, what, delayMillis);
    }

    /***
     *
     * @param msg
     * @param uptimeMillis 某个时刻点，不是一个时间段
     * @return
     */
    public static boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        return MyHandler.getInstance().sendMsgAtTime(msg, uptimeMillis, true);
    }

    public static boolean sendEmptyMessageAtTime(Object object, int what, long uptimeMillis) {
        return MyHandler.getInstance().sendEmptyMsgAtTime(object, what, uptimeMillis);
    }

    public static boolean sendMessageAtFrontOfQueue(Message msg) {
        return MyHandler.getInstance().sendMsgAtFrontOfQueue(msg);
    }

    public static boolean post(Runnable r) {
        return MyHandler.getInstance().postRunnableDelayed(r, 0);
    }

    public static boolean postDelayed(Runnable r, long delayMillis) {
        return MyHandler.getInstance().postRunnableDelayed(r, delayMillis);
    }

    public static void removeAllMessages() {
        MyHandler.getInstance().removeAllMsgs();
    }

}

/***
 * 不要调用父类的发送消息的方法
 */
class MyHandler extends Handler {

    private static final String TAG = "MyHandler";
    private static final boolean printLog = false;
    private static MyHandler sMyHandler;
    private static Looper mLooper;
    private static HashMap<Object, Callback> mHashMap =
            new HashMap<Object, Callback>();
    private static ArrayList<Message> mMsgsList = new ArrayList<Message>();
    private final Object mObjectLock = new Object();
    private static Message sMessage;
    private boolean mIsSendMsgSync = false;

    private MyHandler(Looper looper) {
        super(looper);
        if (sMessage == null) {
            sMessage = this.obtainMessage();
        }
    }

    static void setLooper(Looper looper) {
        mLooper = looper;
    }

    static MyHandler getInstance() {
        if (sMyHandler == null) {
            synchronized (MyHandler.class) {
                if (sMyHandler == null) {
                    sMyHandler = new MyHandler(mLooper);
                }
            }
        }
        return sMyHandler;
    }

    final void addCallback(Object object, Callback callback) {
        if (object == null || callback == null) {
            return;
        }
        synchronized (MyHandler.this) {
            if (!mHashMap.containsKey(object)) {
                if (printLog)
                    Log.i(TAG, "addCallback():object = " + object);
                mHashMap.put(object, callback);
                if (printLog)
                    Log.i(TAG, "add mHashMap.size() = " + mHashMap.size());
            }
        }
    }

    final Message getMsg() {
        Message msg = null;
        if (sMessage == null) {
            msg = this.obtainMessage();
            sMessage = msg;
        } else {
            msg = Message.obtain(sMessage);
        }
        return msg;
    }

    final boolean sendMsgSync(Message msg) {
        mIsSendMsgSync = true;
        boolean sendMsg = sendMsg(msg);
        synchronized (mObjectLock) {
            if (mIsSendMsgSync) {
                try {
                    if (printLog)
                        Log.i(TAG, "mObjectLock.wait()");
                    mObjectLock.wait();
                    mIsSendMsgSync = false;
                } catch (Exception e) {
                }
            }
        }
        return sendMsg;
    }

    /*final boolean sendMsgAsync(Message msg) {
        return sendMsg(msg);
    }*/

    final boolean sendMsg(Message msg) {
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), true);
    }

    final boolean sendEmptyMsgSync(Object object, int what) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgSync(msg);
    }

    final boolean sendEmptyMsgAsync(Object object, int what) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), true);
    }

    final boolean sendMsgDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, true);
    }

    final boolean sendEmptyMsgDelayed(Object object, int what, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, true);
    }

    final boolean sendEmptyMsgAtTime(Object object, int what, long uptimeMillis) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, uptimeMillis, true);
    }

    final boolean postRunnableDelayed(Runnable r, long delayMillis) {
        Message msg = getMsg();
        setField(msg, r);
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, false);
    }

    final boolean sendMsgAtTime(Message msg, long uptimeMillis, boolean needAddToList) {
        if (needAddToList) {
            synchronized (MyHandler.this) {
                mMsgsList.add(msg);
                if (printLog)
                    Log.i(TAG, "sendMsgAtTime " + msg.toString());
                if (printLog)
                    Log.i(TAG, "sendMsgAtTime mMsgsList.size() = " + mMsgsList.size());
            }
        }
        return sendMessageAtTime(msg, uptimeMillis);
    }

    final boolean sendMsgAtFrontOfQueue(Message msg) {
        synchronized (MyHandler.this) {
            mMsgsList.add(msg);
            Log.i(TAG, msg.toString());
            if (printLog)
                Log.i(TAG, "sendMsgAtFrontOfQueue mMsgsList.size() = " + mMsgsList.size());
        }
        return sendMessageAtFrontOfQueue(msg);
    }

    final void exit(Object object) {
        synchronized (MyHandler.this) {
            if (printLog)
                Log.i(TAG, "exit():object = " + object);
            if (mHashMap.containsKey(object)) {
                mHashMap.remove(object);
                if (printLog)
                    Log.i(TAG, "exit mHashMap.size() = " + mHashMap.size());
            }
            if (printLog)
                Log.i(TAG, "exit all mMsgsList.size() = " + mMsgsList.size());
            Iterator<Message> iter = mMsgsList.iterator();
            while (iter.hasNext()) {
                Message msg = iter.next();
                if (msg.obj == object) {
                    iter.remove();
                    if (printLog)
                        Log.i(TAG, "exit mMsgsList.size() = " + mMsgsList.size());
                }
            }
        }
    }

    final void removeAllMsgs() {
        synchronized (MyHandler.this) {
            for (Message msg : mMsgsList) {
                if (msg == null) {
                    continue;
                }
                removeMessages(msg.what);
            }
            mHashMap.clear();
            mMsgsList.clear();
        }
    }

    /***
     * msg中的obj属性已经被使用掉了,如果这个属性还想作其他的引用,那么需要重新再设计一下.
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        if (msg == null || mHashMap.isEmpty()) {
            return;
        }
        final Object object = msg.obj;
        if (printLog)
            Log.i(TAG, "handleMessage():object = " + object);
        if (object != null) {
            Callback callback = null;
            if (printLog)
                Log.i(TAG, "mMsgsList.size() before = " + mMsgsList.size());
            synchronized (MyHandler.this) {
                callback = mHashMap.get(object);
            }
            if (callback != null) {
                callback.handleMessage(msg);
            }
            synchronized (mObjectLock) {
                if (mIsSendMsgSync) {
                    mObjectLock.notify();
                    if (printLog)
                        Log.i(TAG, "mObjectLock.notify()");
                }
            }
        }
        synchronized (MyHandler.this) {
            if (mMsgsList.contains(msg)) {
                mMsgsList.remove(msg);
                if (printLog)
                    Log.i(TAG, "mMsgsList.size() after = " + mMsgsList.size());
            }
        }
        // super.handleMessage(msg);
    }

    private void setField(Message msg, Runnable runnable) {
        try {
            Class clazz = Class.forName("android.os.Message");
            Field field = clazz.getDeclaredField("callback");
            field.setAccessible(true);
            field.set(msg, runnable);
            // Log.i(TAG, "setField: " + runnable);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
