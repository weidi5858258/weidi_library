package com.weidi.handler;

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
 使用:
 一.不需要传递参数
 方式一:
 Message msg = HandlerUtils2.getMessage();
 msg.what = 1;
 // 传递注册时的对象
 // 这一步需要注意,不要忘记了
 msg.obj = MainActivity.class;
 HandlerUtils2.sendMessageDelayed(msg, 5000);
 方式二:
 HandlerUtils2.sendEmptyMessage(MainActivity.this, 1);

 二.需要传递参数
 Message msg = HandlerUtils2.getMessage();
 msg.what = 1;
 // 这一步需要注意,不要忘记了
 msg.obj = MainActivity.class;
 People people = new People();
 Student student = new Student();
 HandlerUtils2.setMessage(msg, new Object[]{student, people});
 HandlerUtils2.sendMessageDelayed(msg, 5000);

 注意:
 首先使用这个工具的目的是为了在子线程中执行任务时
 需要在某个时刻主线程更新一下UI,因此如果使用异步
 方式发送了非常多的消息,那么在主线程中还是来不及
 执行,因而会把界面抯塞掉.因此要自己估计好,如果会
 向主线程发送非常多的消息时,最好使用同步,让主线程
 来一个消息,执行掉一个消息,这样消息就不会堆积起来.
 用异步方式一下子发送消息最好在100个以内.
 因此在循环语句中先要估计一下,这个循环会
 执行多少次,如果超出100个,那么使用同步比较好.

 测试结果来看还是用同步比较好.
 同步执行的代码也不要太耗时.
 */
public class HandlerUtils {

    public interface Callback {
        void handleMessage(Message msg, Object[] objArray);
    }

    /***
     * 在Application中初始化
     * @param looper
     */
    public static void init(Looper looper) {
        InnerHandler.setLooper(looper);
        InnerHandler.getInstance();
    }

    /***
     * 一般使用在Activity、Fragment、Service中
     * 在其他类（没有生命周期的类）中使用时，
     * 使用完这个类，需要主动调用一下unregister()方法
     * @param clazz
     * @param callback
     */
    public static void register(Class clazz, HandlerUtils.Callback callback) {
        InnerHandler.getInstance().addCallback(clazz, callback);
    }

    /***
     * 在Activity、Fragment、Service中的onDestroy()方法中调用，
     * 传递this就行了
     * @param clazz
     */
    public static void unregister(Class clazz) {
        InnerHandler.getInstance().exit(clazz);
    }

    public static boolean hasMessages(int what) {
        return InnerHandler.getInstance().hasMessages(what);
    }

    /***
     * 想得到一个消息使用这个方法
     * @return
     */
    public static Message getMessage() {
        return InnerHandler.getInstance().getMsg();
    }

    public static Message getMessage(Class clazz, int what) {
        return InnerHandler.getInstance().getMsg(clazz, what);
    }

    /***
     * @param msg
     */
    public static boolean sendMessage(Message msg) {
        return InnerHandler.getInstance().sendMsg(msg);
    }

    /***
     * 在子线程中需要传递多个参数到主线程去执行
     * @param msg
     * @param objArray
     */
    public static boolean sendMessage(Message msg, Object[] objArray) {
        return InnerHandler.getInstance().sendMsg(msg, objArray);
    }

    /***
     *
     * @param msg
     * @param delayMillis 一个时间段，不是一个时刻点
     * @return
     */
    public static boolean sendMessageDelayed(Message msg, long delayMillis) {
        return InnerHandler.getInstance().sendMsgDelayed(msg, delayMillis);
    }

    public static boolean sendMessageDelayed(Message msg, long delayMillis, Object[] objArray) {
        return InnerHandler.getInstance().sendMsgDelayed(msg, delayMillis, objArray);
    }

    /***
     *
     * @param msg
     * @param uptimeMillis 某个时刻点，不是一个时间段
     * @return
     */
    public static boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        return InnerHandler.getInstance().sendMsgAtTime(msg, uptimeMillis, false);
    }

    public static boolean sendMessageAtTime(Message msg, long uptimeMillis, Object[] objArray) {
        return InnerHandler.getInstance().sendMsgAtTime(msg, uptimeMillis, false, objArray);
    }

    /*public static boolean sendEmptyMessage(Class clazz, int what) {
        return InnerHandler.getInstance().sendEmptyMsg(clazz, what);
    }

    public static boolean sendEmptyMessage(Class clazz, int what, Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsg(clazz, what, objArray);
    }*/

    public static boolean sendEmptyMessage(Class clazz, int what) {
        return InnerHandler.getInstance().sendEmptyMsg(clazz, what);
    }

    public static boolean sendEmptyMessage(Class clazz, int what, Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsg(clazz, what, objArray);
    }

    public static boolean sendEmptyMessageDelayed(Class clazz, int what, long delayMillis) {
        return InnerHandler.getInstance().sendEmptyMsgDelayed(clazz, what, delayMillis);
    }

    public static boolean sendEmptyMessageDelayed(Class clazz, int what, long delayMillis,
                                                  Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsgDelayed(clazz, what, delayMillis, objArray);
    }

    public static boolean sendEmptyMessageAtTime(Class clazz, int what, long uptimeMillis) {
        return InnerHandler.getInstance().sendEmptyMsgAtTime(clazz, what, uptimeMillis);
    }

    public static boolean sendEmptyMessageAtTime(Class clazz, int what, long uptimeMillis,
                                                 Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsgAtTime(clazz, what, uptimeMillis, objArray);
    }

    public static boolean sendMessageAtFrontOfQueue(Message msg) {
        return InnerHandler.getInstance().sendMsgAtFrontOfQueue(msg);
    }

    // 调用上面那些方法需要注册与反注册
    // 调用下面两个方法不需要,但是下面方法实现不了同步

    public static boolean post(Runnable r) {
        return InnerHandler.getInstance().postRunnableDelayed(r, 0);
    }

    public static boolean postDelayed(Runnable r, long delayMillis) {
        return InnerHandler.getInstance().postRunnableDelayed(r, delayMillis);
    }

    public static void removeCallbacks(Runnable r) {
        InnerHandler.getInstance().removeCallback(r);
    }

    public static void removeMessages(int what) {
        InnerHandler.getInstance().removeMessage(what);
    }

    public static void removeAllMessages() {
        InnerHandler.getInstance().removeAllMsgs();
    }

}

/***
 * 不要调用父类的发送消息的方法
 */
class InnerHandler extends Handler {

    private static final String TAG = "InnerHandler";
    private static final boolean printLog = false;
    private static final HashMap<Message, Object[]> mMsgMap =
            new HashMap<Message, Object[]>();
    private static HashMap<Class, HandlerUtils.Callback> mCallbackMap =
            new HashMap<Class, HandlerUtils.Callback>();
    private static ArrayList<Message> mMsgsList = new ArrayList<Message>();
    private volatile static InnerHandler sInnerHandler = null;
    private static Looper mLooper = null;
    private static Message sMessage = null;

    private InnerHandler(Looper looper) {
        super(looper);
        if (sMessage == null) {
            sMessage = this.obtainMessage();
        }
    }

    static void setLooper(Looper looper) {
        mLooper = looper;
        sInnerHandler = null;
    }

    static InnerHandler getInstance() {
        if (sInnerHandler == null) {
            synchronized (InnerHandler.class) {
                if (sInnerHandler == null) {
                    mMsgMap.clear();
                    mCallbackMap.clear();
                    mMsgsList.clear();
                    sMessage = null;
                    sInnerHandler = new InnerHandler(mLooper);
                }
            }
        }
        return sInnerHandler;
    }

    final void addCallback(Class clazz, HandlerUtils.Callback callback) {
        if (clazz == null || callback == null) {
            return;
        }
        synchronized (InnerHandler.this) {
            if (!mCallbackMap.containsKey(clazz)) {
                mCallbackMap.put(clazz, callback);
                if (printLog) {
                    Log.i(TAG, "addCallback():clazz = " + clazz);
                    Log.i(TAG, "add mCallbackMap.size() = " + mCallbackMap.size());
                }
            }
        }
    }

    final boolean hasMessage(int what) {
        return hasMessages(what);
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

    final Message getMsg(Class clazz, int what) {
        Message msg = getMsg();
        if (msg != null) {
            msg.obj = clazz;
            msg.what = what;
        }
        return msg;
    }

    final boolean sendMsg(Message msg) {
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), false);
    }

    final boolean sendMsg(Message msg, Object[] objArray) {
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), false, objArray);
    }

    final boolean sendEmptyMsg(Class clazz, int what) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), false);
    }

    final boolean sendEmptyMsg(Class clazz, int what, Object[] objArray) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), false, objArray);
    }

    final boolean sendMsgDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, false);
    }

    final boolean sendMsgDelayed(Message msg, long delayMillis, Object[] objArray) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMsgAtTime(
                msg, SystemClock.uptimeMillis() + delayMillis, false, objArray);
    }

    final boolean sendEmptyMsgDelayed(
            Class clazz, int what, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, false);
    }

    final boolean sendEmptyMsgDelayed(
            Class clazz, int what, long delayMillis, Object[] objArray) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(
                msg, SystemClock.uptimeMillis() + delayMillis, false, objArray);
    }

    final boolean sendEmptyMsgAtTime(
            Class clazz, int what, long uptimeMillis) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg, uptimeMillis, false);
    }

    final boolean sendEmptyMsgAtTime(
            Class clazz, int what, long uptimeMillis, Object[] objArray) {
        Message msg = getMsg();
        msg.obj = clazz;
        msg.what = what;
        return sendMsgAtTime(msg, uptimeMillis, false, objArray);
    }

    /*final boolean postRunnableDelayed(Runnable r, long delayMillis) {
        Message msg = getMsg();
        setField(msg, r);
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, false);
    }*/

    final boolean postRunnableDelayed(Runnable r, long delayMillis) {
        return postDelayed(r, delayMillis);
    }

    final void removeMessage(int what) {
        removeMessages(what);
    }

    final void removeAllMsgs() {
        synchronized (InnerHandler.this) {
            for (Message msg : mMsgsList) {
                if (msg == null) {
                    continue;
                }
                removeMessages(msg.what);
            }
            mCallbackMap.clear();
            mMsgsList.clear();
        }
    }

    final void removeCallback(Runnable r) {
        removeCallbacks(r);
    }

    final boolean sendMsgAtTime(
            Message msg,
            long uptimeMillis,
            boolean needAddToList) {
        if (needAddToList) {
            synchronized (InnerHandler.this) {
                mMsgsList.add(msg);
                /*if (printLog)
                    MLog.i(TAG, "sendMsgAtTime " + msg.toString());
                if (printLog)
                    MLog.i(TAG, "sendMsgAtTime mMsgsList.size() = " + mMsgsList.size());*/
            }
        }
        return sendMessageAtTime(msg, uptimeMillis);
    }

    final boolean sendMsgAtTime(
            Message msg,
            long uptimeMillis,
            boolean needAddToList,
            Object[] objArray) {
        if (needAddToList) {
            synchronized (InnerHandler.this) {
                mMsgsList.add(msg);
                /*if (printLog)
                    MLog.i(TAG, "sendMsgAtTime " + msg.toString());
                if (printLog)
                    MLog.i(TAG, "sendMsgAtTime mMsgsList.size() = " + mMsgsList.size());*/
            }
        }
        if (objArray != null) {
            mMsgMap.put(msg, objArray);
        }
        return sendMessageAtTime(msg, uptimeMillis);
    }

    final boolean sendMsgAtFrontOfQueue(Message msg) {
        synchronized (InnerHandler.this) {
            mMsgsList.add(msg);
            Log.i(TAG, msg.toString());
            if (printLog)
                Log.i(TAG, "sendMsgAtFrontOfQueue mMsgsList.size() = " + mMsgsList.size());
        }
        return sendMessageAtFrontOfQueue(msg);
    }

    final void exit(Class clazz) {
        if (clazz == null) {
            return;
        }
        synchronized (InnerHandler.this) {
            if (printLog)
                Log.i(TAG, "exit():clazz = " + clazz);
            if (mCallbackMap.containsKey(clazz)) {
                mCallbackMap.remove(clazz);
                if (printLog)
                    Log.i(TAG, "exit mCallbackMap.size() = " + mCallbackMap.size());
            }
            if (printLog)
                Log.i(TAG, "exit all mMsgsList.size() = " + mMsgsList.size());
            Iterator<Message> iter = mMsgsList.iterator();
            while (iter.hasNext()) {
                Message msg = iter.next();
                if (clazz.getSimpleName().equals(
                        ((Class) msg.obj).getSimpleName())) {
                    iter.remove();
                    if (printLog)
                        Log.i(TAG, "exit mMsgsList.size() = " + mMsgsList.size());
                }
            }
        }
    }

    /***
     * msg中的obj属性已经被使用掉了,如果这个属性还想作其他的引用,那么需要重新再设计一下.
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        if (msg == null
                || mCallbackMap.isEmpty()
                || msg.obj == null
                || !(msg.obj instanceof Class)) {
            return;
        }
        if (printLog)
            Log.i(TAG, "handleMessage(): " + msg);
        HandlerUtils.Callback callback = mCallbackMap.get((Class) msg.obj);
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
            /*if (printLog)
                Log.i(TAG, "mMsgsList.size() after = " + mMsgsList.size());*/
        }
        /*synchronized (msg) {
            msg.notify();
            Log.i(TAG, "msg.notify(): " + msg);
            msg.obj = null;
        }*/
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
