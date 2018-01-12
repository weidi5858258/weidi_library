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
 使用:
 一.不需要传递参数
 Message msg = HandlerUtils.getMessage();
 msg.what = 1;
 // 传递注册时的对象
 // 这一步需要注意,不要忘记了
 msg.obj = MainActivity.this;
 HandlerUtils.sendMessageDelayed(msg, 5000);

 HandlerUtils.sendEmptyMessageSync(MainActivity.this, 1);
 二.需要传递参数
 Message msg = HandlerUtils.getMessage();
 msg.what = 1;
 // 这一步需要注意,不要忘记了
 msg.obj = MainActivity.this;
 People people = new People();
 Student student = new Student();
 HandlerUtils.setMessage(msg, new Object[]{student, people});
 HandlerUtils.sendMessageDelayed(msg, 5000);

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
     * @param object
     * @param callback
     */
    public static void register(Object object, Callback callback) {
        InnerHandler.getInstance().addCallback(object, callback);
    }

    /***
     * 在Activity、Fragment、Service中的onDestroy()方法中调用，
     * 传递this就行了
     * @param object
     */
    public static void unregister(Object object) {
        InnerHandler.getInstance().exit(object);
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

    /***
     * @param msg
     */
    public static boolean sendMessageSync(Message msg) {
        return InnerHandler.getInstance().sendMsgSync(msg);
    }

    /***
     * 在子线程中需要传递多个参数到主线程去执行
     * @param msg
     * @param objArray
     */
    public static boolean sendMessageSync(Message msg, Object[] objArray) {
        return InnerHandler.getInstance().sendMsgSync(msg, objArray);
    }

    public static boolean sendMessageAsync(Message msg) {
        return InnerHandler.getInstance().sendMsg(msg);
    }

    public static boolean sendMessageAsync(Message msg, Object[] objArray) {
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
        return InnerHandler.getInstance().sendMsgAtTime(msg, uptimeMillis, true);
    }

    public static boolean sendMessageAtTime(Message msg, long uptimeMillis, Object[] objArray) {
        return InnerHandler.getInstance().sendMsgAtTime(msg, uptimeMillis, true, objArray);
    }

    public static boolean sendEmptyMessageSync(Object object, int what) {
        return InnerHandler.getInstance().sendEmptyMsgSync(object, what);
    }

    public static boolean sendEmptyMessageSync(Object object, int what, Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsgSync(object, what, objArray);
    }

    public static boolean sendEmptyMessageAsync(Object object, int what) {
        return InnerHandler.getInstance().sendEmptyMsgAsync(object, what);
    }

    public static boolean sendEmptyMessageAsync(Object object, int what, Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsgAsync(object, what, objArray);
    }

    public static boolean sendEmptyMessageDelayed(Object object, int what, long delayMillis) {
        return InnerHandler.getInstance().sendEmptyMsgDelayed(object, what, delayMillis);
    }

    public static boolean sendEmptyMessageDelayed(Object object, int what, long delayMillis,
                                                  Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsgDelayed(object, what, delayMillis, objArray);
    }

    public static boolean sendEmptyMessageAtTime(Object object, int what, long uptimeMillis) {
        return InnerHandler.getInstance().sendEmptyMsgAtTime(object, what, uptimeMillis);
    }

    public static boolean sendEmptyMessageAtTime(Object object, int what, long uptimeMillis,
                                                 Object[] objArray) {
        return InnerHandler.getInstance().sendEmptyMsgAtTime(object, what, uptimeMillis, objArray);
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
    private volatile static InnerHandler sInnerHandler;
    private static Looper mLooper;
    private static final HashMap<Message, Object[]> mMsgMap =
            new HashMap<Message, Object[]>();
    private static HashMap<Object, HandlerUtils.Callback> mHashMap =
            new HashMap<Object, HandlerUtils.Callback>();
    private static ArrayList<Message> mMsgsList = new ArrayList<Message>();
    private static Message sMessage;

    private InnerHandler(Looper looper) {
        super(looper);
        if (sMessage == null) {
            sMessage = this.obtainMessage();
        }
    }

    static void setLooper(Looper looper) {
        mLooper = looper;
    }

    static InnerHandler getInstance() {
        if (sInnerHandler == null) {
            synchronized (InnerHandler.class) {
                if (sInnerHandler == null) {
                    sInnerHandler = new InnerHandler(mLooper);
                }
            }
        }
        return sInnerHandler;
    }

    final void addCallback(Object object, HandlerUtils.Callback callback) {
        if (object == null || callback == null) {
            return;
        }
        synchronized (InnerHandler.this) {
            if (!mHashMap.containsKey(object)) {
                if (printLog)
                    Log.i(TAG, "addCallback():object = " + object);
                mHashMap.put(object, callback);
                if (printLog)
                    Log.i(TAG, "add mHashMap.size() = " + mHashMap.size());
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

    final boolean sendMsgSync(Message msg) {
        // Log.i(TAG, "sendMsgSync()msg: " + msg);
        boolean sendMsg = sendMsg(msg);
        synchronized (msg) {
            try {
                if (msg.obj != null) {
                    // Log.i(TAG, "msg.wait(): " + msg);
                    msg.wait();
                }
            } catch (Exception e) {
            }
        }
        return sendMsg;
    }

    final boolean sendMsgSync(Message msg, Object[] objArray) {
        // Log.i(TAG, "sendMsgSync()msg: " + msg);
        boolean sendMsg = sendMsg(msg, objArray);
        synchronized (msg) {
            try {
                if (msg.obj != null) {
                    // Log.i(TAG, "msg.wait(): " + msg);
                    msg.wait();
                }
            } catch (Exception e) {
            }
        }
        return sendMsg;
    }

    final boolean sendMsg(Message msg) {
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), true);
    }

    final boolean sendMsg(Message msg, Object[] objArray) {
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), true, objArray);
    }

    final boolean sendEmptyMsgSync(Object object, int what) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgSync(msg);
    }

    final boolean sendEmptyMsgSync(Object object, int what, Object[] objArray) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgSync(msg, objArray);
    }

    final boolean sendEmptyMsgAsync(Object object, int what) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), true);
    }

    final boolean sendEmptyMsgAsync(Object object, int what, Object[] objArray) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis(), true, objArray);
    }

    final boolean sendMsgDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, true);
    }

    final boolean sendMsgDelayed(Message msg, long delayMillis, Object[] objArray) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMsgAtTime(
                msg, SystemClock.uptimeMillis() + delayMillis, true, objArray);
    }

    final boolean sendEmptyMsgDelayed(
            Object object, int what, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, SystemClock.uptimeMillis() + delayMillis, true);
    }

    final boolean sendEmptyMsgDelayed(
            Object object, int what, long delayMillis, Object[] objArray) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(
                msg, SystemClock.uptimeMillis() + delayMillis, true, objArray);
    }

    final boolean sendEmptyMsgAtTime(
            Object object, int what, long uptimeMillis) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, uptimeMillis, true);
    }

    final boolean sendEmptyMsgAtTime(
            Object object, int what, long uptimeMillis, Object[] objArray) {
        Message msg = getMsg();
        msg.obj = object;
        msg.what = what;
        return sendMsgAtTime(msg, uptimeMillis, true, objArray);
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
            mHashMap.clear();
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
                    Log.i(TAG, "sendMsgAtTime " + msg.toString());
                if (printLog)
                    Log.i(TAG, "sendMsgAtTime mMsgsList.size() = " + mMsgsList.size());*/
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
                    Log.i(TAG, "sendMsgAtTime " + msg.toString());
                if (printLog)
                    Log.i(TAG, "sendMsgAtTime mMsgsList.size() = " + mMsgsList.size());*/
            }
        }
        mMsgMap.put(msg, objArray);
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

    final void exit(Object object) {
        synchronized (InnerHandler.this) {
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

    /***
     * msg中的obj属性已经被使用掉了,如果这个属性还想作其他的引用,那么需要重新再设计一下.
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        /*if (printLog)
            Log.i(TAG, "handleMessage(): " + msg);*/
        if (msg == null || mHashMap.isEmpty() || msg.obj == null) {
            return;
        }
        HandlerUtils.Callback callback = mHashMap.get(msg.obj);
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
        synchronized (msg) {
            msg.notify();
            // Log.i(TAG, "msg.notify(): " + msg);
            msg.obj = null;
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
