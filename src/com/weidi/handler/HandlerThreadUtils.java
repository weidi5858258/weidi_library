package com.weidi.handler;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by root on 17-12-18.
 */

public class HandlerThreadUtils {

    public interface Callback {
        void handleMessage(Message msg, Object[] objArray);
    }

    /***
     * 在Application中初始化
     */
    public static void init() {
        InnerHandlerThread.getInstance().init();
    }

    /***
     * 一般使用在Activity、Fragment、Service中
     * 在其他类（没有生命周期的类）中使用时，
     * 使用完这个类，需要主动调用一下unregister()方法
     * @param clazz
     * @param callback 不能为null,因为发送消息后总要有个地方处理事件
     */
    public static void register(Class clazz, HandlerThreadUtils.Callback callback) {
        InnerHandlerThread.getInstance()
                .addCallback(clazz, callback);
    }

    /***
     * 在Activity、Fragment、Service中的onDestroy()方法中调用，
     * 传递this就行了
     * @param clazz
     */
    public static void unregister(Class clazz) {
        InnerHandlerThread.getInstance()
                .exit(clazz);
    }

    public static boolean hasMessages(int what) {
        return InnerHandlerThread.getInstance()
                .hasMessage(what);
    }

    /***
     * 想得到一个消息使用这个方法
     * @return
     */
    public static Message getMessage() {
        return InnerHandlerThread.getInstance()
                .getMsg();
    }

    public static Message getMessage(int what, Class clazz) {
        return InnerHandlerThread.getInstance()
                .getMsg(what, clazz);
    }

    /***
     * @param msg
     */
    public static boolean sendMessage(Message msg) {
        return InnerHandlerThread.getInstance()
                .sendMsg(msg);
    }

    /***
     * 在子线程中需要传递多个参数到主线程去执行
     * @param msg
     * @param objArray
     */
    public static void sendMessage(Message msg, Object[] objArray) {
        InnerHandlerThread.getInstance()
                .sendMsg(msg, objArray);
    }

    /***
     *
     * @param msg
     * @param delayMillis 一个时间段，不是一个时刻点
     * @return
     */
    public static boolean sendMessageDelayed(Message msg, long delayMillis) {
        return InnerHandlerThread.getInstance()
                .sendMsgDelayed(msg, delayMillis);
    }

    public static boolean sendMessageDelayed(Message msg, long delayMillis, Object[] objArray) {
        return InnerHandlerThread.getInstance()
                .sendMsgDelayed(msg, delayMillis, objArray);
    }

    /***
     *
     * @param msg
     * @param uptimeMillis 某个时刻点，不是一个时间段
     * @return
     */
    public static boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        return InnerHandlerThread.getInstance()
                .sendMsgAtTime(msg, uptimeMillis, false);
    }

    public static boolean sendMessageAtTime(Message msg, long uptimeMillis, Object[] objArray) {
        return InnerHandlerThread.getInstance()
                .sendMsgAtTime(msg, uptimeMillis, false, objArray);
    }

    public static boolean sendEmptyMessage(Class clazz, int what) {
        return InnerHandlerThread.getInstance()
                .sendEmptyMsg(clazz, what);
    }

    public static boolean sendEmptyMessage(Class clazz, int what, Object[] objArray) {
        return InnerHandlerThread.getInstance()
                .sendEmptyMsg(clazz, what, objArray);
    }

    public static boolean sendEmptyMessageDelayed(Class clazz, int what, long delayMillis) {
        return InnerHandlerThread.getInstance()
                .sendEmptyMsgDelayed(clazz, what, delayMillis);
    }

    public static boolean sendEmptyMessageDelayed(Class clazz, int what, long delayMillis,
                                                  Object[] objArray) {
        return InnerHandlerThread.getInstance()
                .sendEmptyMsgDelayed(clazz, what, delayMillis, objArray);
    }

    public static boolean sendEmptyMessageAtTime(Class clazz, int what, long uptimeMillis) {
        return InnerHandlerThread.getInstance()
                .sendEmptyMsgAtTime(clazz, what, uptimeMillis);
    }

    public static boolean sendEmptyMessageAtTime(Class clazz, int what, long uptimeMillis,
                                                 Object[] objArray) {
        return InnerHandlerThread.getInstance()
                .sendEmptyMsgAtTime(clazz, what, uptimeMillis, objArray);
    }

    public static boolean sendMessageAtFrontOfQueue(Message msg) {
        return InnerHandlerThread.getInstance()
                .sendMsgAtFrontOfQueue(msg);
    }

    public static void removeMessages(int what) {
        InnerHandlerThread.getInstance()
                .removeMessage(what);
    }

    public static void removeAllMessages() {
        InnerHandlerThread.getInstance()
                .removeAllMsgs();
    }

}

/***
 * 处理耗时任务,只不过这些任务是按队列一个一个执行的.
 * 也就是说一个任务执行完了,才会执行下一个任务.
 * 因此,如果要达到并发的效果,那么不要用这个工具.
 * 也就是要立刻执行的任务就不适合使用这个工具,如真要用,
 * 需要把队列里的消息全部清空,然后再执行任务.
 * 使用这个工具的场景一般是隔多长时间进行某次任务的执行,
 * 或者到某个时间点时需要进行什么样的工作.
 */
final class InnerHandlerThread extends HandlerThread {

    private static final String TAG = "InnerHandlerThread";
    private static final boolean printLog = false;
    private static final HashMap<Message, Object[]> mMsgMap =
            new HashMap<Message, Object[]>();
    private static final HashMap<Class, HandlerThreadUtils.Callback> mCallbackMap =
            new HashMap<Class, HandlerThreadUtils.Callback>();
    private static ArrayList<Message> mMsgsList = new ArrayList<Message>();
    private volatile static InnerHandlerThread sInnerHandlerThread = null;
    private volatile static Handler sInnerHandler = null;
    private volatile static Message sMessage = null;

    private InnerHandlerThread(String name) {
        super(name);
    }

    static InnerHandlerThread getInstance() {
        if (sInnerHandlerThread == null) {
            synchronized (InnerHandlerThread.class) {
                if (sInnerHandlerThread == null) {
                    sInnerHandlerThread = new InnerHandlerThread("InnerHandlerThread");
                    sInnerHandlerThread.start();
                }
            }
        }
        return sInnerHandlerThread;
    }

    // first
    final void init() {
        mMsgMap.clear();
        mCallbackMap.clear();
        mMsgsList.clear();
        sInnerHandlerThread = null;
        sInnerHandler = null;
        sMessage = null;
        // getInstance().start();
        createHandler();
    }

    /***
     *  通过这个Handler发送的消息,都是在子线程中处理的,
     *  因此刷新UI的操作就不能用这个Handler发送消息.
     * @return
     */
    final Handler getHandler() {
        createHandler();
        return sInnerHandler;
    }

    final void addCallback(Class clazz, HandlerThreadUtils.Callback callback) {
        if (clazz == null || callback == null) {
            return;
        }
        synchronized (InnerHandlerThread.this) {
            if (!mCallbackMap.containsKey(clazz)) {
                mCallbackMap.put(clazz, callback);
                if (printLog) {
                    Log.i(TAG, "addCallback() object: " + clazz);
                    Log.i(TAG, "addCallback() mCallbackMap.size(): " + mCallbackMap.size());
                }
            }
        }
    }

    final boolean hasMessage(int what) {
        return getHandler().hasMessages(what);
    }

    final Message getMsg() {
        Message msg = null;
        if (sMessage == null) {
            msg = getHandler().obtainMessage();
            sMessage = msg;
        } else {
            msg = Message.obtain(sMessage);
        }
        return msg;
    }

    final Message getMsg(int what, Class clazz) {
        Message msg = getMsg();
        if (msg != null) {
            msg.what = what;
            msg.obj = clazz;
        }
        return msg;
    }

    final boolean sendMsg(Message msg) {
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis(),
                false);
    }

    final boolean sendMsg(Message msg, Object[] objArray) {
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis(),
                false, objArray);
    }

    final boolean sendEmptyMsg(Class clazz, int what) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis(),
                false);
    }

    final boolean sendEmptyMsg(Class clazz, int what, Object[] objArray) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis(),
                false,
                objArray);
    }

    final boolean sendMsgDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis() + delayMillis,
                true);
    }

    final boolean sendMsgDelayed(Message msg, long delayMillis, Object[] objArray) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis() + delayMillis,
                false,
                objArray);
    }

    final boolean sendEmptyMsgDelayed(
            Class clazz, int what, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis() + delayMillis,
                true);
    }

    final boolean sendEmptyMsgDelayed(
            Class clazz, int what, long delayMillis, Object[] objArray) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg,
                SystemClock.uptimeMillis() + delayMillis,
                true,
                objArray);
    }

    final boolean sendEmptyMsgAtTime(
            Class clazz, int what, long uptimeMillis) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg,
                uptimeMillis,
                true);
    }

    final boolean sendEmptyMsgAtTime(
            Class clazz, int what, long uptimeMillis, Object[] objArray) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg,
                uptimeMillis,
                true,
                objArray);
    }

    final void removeMessage(int what) {
        getHandler().removeMessages(what);
    }

    final void removeAllMsgs() {
        synchronized (InnerHandlerThread.this) {
            for (Message msg : mMsgsList) {
                if (msg == null) {
                    continue;
                }
                getHandler().removeMessages(msg.what);
            }
            mCallbackMap.clear();
            mMsgsList.clear();
        }
    }

    final boolean sendMsgAtTime(
            Message msg,
            long uptimeMillis,
            boolean needAddToList) {
        if (needAddToList) {
            synchronized (InnerHandlerThread.this) {
                mMsgsList.add(msg);
                /*if (printLog)
                    MLog.i(TAG, "sendMsgAtTime " + msg.toString());
                if (printLog)
                    MLog.i(TAG, "sendMsgAtTime mMsgsList.size() = " + mMsgsList.size());*/
            }
        }
        return getHandler().sendMessageAtTime(msg, uptimeMillis);
    }

    final boolean sendMsgAtTime(
            Message msg,
            long uptimeMillis,
            boolean needAddToList,
            Object[] objArray) {
        if (needAddToList) {
            synchronized (InnerHandlerThread.this) {
                mMsgsList.add(msg);
                /*if (printLog)
                    MLog.i(TAG, "sendMsgAtTime " + msg.toString());
                if (printLog)
                    MLog.i(TAG, "sendMsgAtTime mMsgsList.size() = " + mMsgsList.size());*/
            }
        }
        mMsgMap.put(msg, objArray);
        return getHandler().sendMessageAtTime(msg, uptimeMillis);
    }

    final boolean sendMsgAtFrontOfQueue(Message msg) {
        synchronized (InnerHandlerThread.this) {
            mMsgsList.add(msg);
            Log.i(TAG, msg.toString());
            if (printLog)
                Log.i(TAG, "sendMsgAtFrontOfQueue mMsgsList.size() = " + mMsgsList.size());
        }
        return getHandler().sendMessageAtFrontOfQueue(msg);
    }

    final void exit(Class clazz) {
        synchronized (InnerHandlerThread.this) {
            if (mCallbackMap.containsKey(clazz)) {
                mCallbackMap.remove(clazz);
                if (printLog) {
                    Log.i(TAG, "exit():clazz = " + clazz);
                    Log.i(TAG, "exit mCallbackMap.size() = " + mCallbackMap.size());
                }
            }
            if (printLog)
                Log.i(TAG, "exit start mMsgsList.size() = " + mMsgsList.size());
            Iterator<Message> iter = mMsgsList.iterator();
            while (iter.hasNext()) {
                Message msg = iter.next();
                if (clazz.getSimpleName().equals(((Class) msg.obj).getSimpleName())) {
                    iter.remove();
                    if (printLog)
                        Log.i(TAG, "exit end mMsgsList.size() = " + mMsgsList.size());
                }
            }
        }
    }

    private void createHandler() {
        if (sInnerHandler == null) {
            sInnerHandler = new Handler(getInstance().getLooper()) {

                @Override
                public void handleMessage(Message msg) {
                    if (msg == null
                            || mCallbackMap.isEmpty()
                            || msg.obj == null
                            || !(msg.obj instanceof Class)) {
                        if (printLog)
                            Log.e(TAG, "createHandler() handleMessage() return");
                        return;
                    }

                    if (printLog)
                        Log.i(TAG, "createHandler() handleMessage(): " + msg);
                    HandlerThreadUtils.Callback callback = mCallbackMap.get((Class) msg.obj);
                    if (callback != null) {
                        if (mMsgMap.containsKey(msg)) {
                            callback.handleMessage(msg, mMsgMap.get(msg));
                        } else {
                            callback.handleMessage(msg, null);
                        }
                    }
                    if (mMsgMap.containsKey(msg)) {
                        mMsgMap.remove(msg);
                    }
                    if (mMsgsList.contains(msg)) {
                        mMsgsList.remove(msg);
                    }
                    // super.handleMessage(msg);
                }
            };
        }
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