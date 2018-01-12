package com.weidi.utils;

import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventBusUtils {

    public static abstract class AAsyncResult implements Parcelable {

        public abstract void onResult(Object object);

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            return;
        }
    }

    public static void init() {
        EventBus.getDefault();
    }

    public static void register(Object object) {
        EventBus.getDefault().register(object);
    }

    public static void unregister(Object object) {
        EventBus.getDefault().unregister(object);
    }

    /***
     * 不需要去调用
     */
    public static void onDestroy() {
        EventBus.getDefault().onDestroy();
    }

    /***
     * 同步,有结果返回
     * 这里"同步"的意思是A处调用postSync()方法后
     * 只有等到消息接收处的代码执行完并返回结果了,
     * 才会再继续执行A处下面的代码
     *
     * @param what 消息标志
     * @param objArray 传递的数据 传给onEvent(int what, Object[] objArray)的参数.
     */
    public static Object postSync(
            final Class clazz,
            final int what,
            final Object[] objArray) {
        return EventBus.getDefault().postSync(clazz, what, objArray);
    }

    /***
     * 不需要返回结果
     *
     * @param what 消息标志
     * @param objArray 传递的数据
     */
    public static void postAsync(
            final Class clazz,
            final int what,
            final Object[] objArray) {
        EventBus.getDefault().postAsync(clazz, what, objArray);
    }

    /***
     * 需要返回结果
     *
     * @param what 消息标志
     * @param objArray 传递的数据
     */
    public static void postAsync(
            final Class clazz,
            final int what,
            final Object[] objArray,
            final AAsyncResult aAsyncResult) {
        EventBus.getDefault().postAsync(clazz, what, objArray, aAsyncResult);
    }

    public static void postDelayed(
            final Class clazz,
            final int what,
            final Object[] objArray,
            final long delayMillis) {
        EventBus.getDefault().postDelayed(clazz, what, objArray, delayMillis);
    }

    /***
     * 需要返回结果
     *
     * @param what 消息标志
     * @param objArray 传递的数据
     */
    public static void postDelayed(
            final Class clazz,
            final int what,
            final Object[] objArray,
            final AAsyncResult aAsyncResult,
            final long delayMillis) {
        EventBus.getDefault().postDelayed(clazz, what, objArray, aAsyncResult, delayMillis);
    }

}

class EventBus {

    private static final String TAG = "EventBus";

    private volatile static EventBus sEventBus;

    EventBus() {
        clear();

        HandlerUtils.register(this, new HandlerUtils.Callback() {

            @Override
            public void handleMessage(Message msg, Object[] objArray) {
                // Log.i(TAG, "EventBus handleMessage() start");

                if (msg == null) {
                    return;
                }
                if (objArray != null) {
                    if (objArray.length == 1) {
                        mObjResult = dispatchEvent(
                                (Class) objArray[0],
                                msg.what,
                                null);
                    } else if (objArray.length == 2) {
                        mObjResult = dispatchEvent(
                                (Class) objArray[0],
                                msg.what,
                                (Object[]) objArray[1]);
                    }
                    if (objArray.length == 3) {
                        if (objArray[2] != null) {
                            ((EventBusUtils.AAsyncResult) objArray[2]).onResult(mObjResult);
                        }
                    }
                }

                // Log.i(TAG, "EventBus handleMessage() end");
            }
        });
    }

    static EventBus getDefault() {
        if (sEventBus == null) {
            synchronized (EventBus.class) {
                if (sEventBus == null) {
                    sEventBus = new EventBus();
                }
            }
        }
        return sEventBus;
    }

    //////////////////////////////////////////////////////////////////////////////////////

    /***
     下面的实现方式是定向型的，效率上面可能会高一些
     如果某种动作只在特定类里面发生，那么用下面的方式
     如果有多个类需要做相同的操作，那么用上面的方式
     1.不需要实现接口
     2.不同类之间的what相同也不会造成问题，如果用上面的方式就会有问题
     3.传递的数据都在主线程中执行
     */

    private static final HashMap<Message, Object[]> mMsgMap =
            new HashMap<Message, Object[]>();
    private static final HashMap<Object, Method> mClassMethodHashMap =
            new HashMap<Object, Method>();
    private Object mObjResult;

    synchronized void register(Object object) {
        if (object == null) {
            throw new NullPointerException("EventBus register() : object = null");
        }
        Class clazz = object.getClass();
        Method method = null;

        try {
            method = clazz.getDeclaredMethod("onEvent", int.class, Object[].class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            method = null;
            Log.e(TAG, "EventBus register() : " + object + " NoSuchMethodException");
            e.printStackTrace();
        } catch (Exception e) {
            method = null;
            e.printStackTrace();
        }

        if (method == null || mClassMethodHashMap == null) {
            return;
        }
        mClassMethodHashMap.put(object, method);
    }

    synchronized void unregister(Object object) {
        if (object == null) {
            throw new NullPointerException("EventBus unregister() : class = null");
        }
        if (mClassMethodHashMap == null || mClassMethodHashMap.isEmpty()) {
            return;
        }
        if (!mClassMethodHashMap.containsKey(object)) {
            return;
        }
        String sampleName = object.getClass().getSimpleName();

        Iterator<Map.Entry<Object, Method>> iter = mClassMethodHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object keyObject = entry.getKey();
            if (keyObject.getClass().getSimpleName().equals(sampleName)) {
                iter.remove();
                break;
            }
        }
    }

    /***
     * 不需要去调用
     */
    void onDestroy() {
        Log.i(TAG, "onDestroy()");
        HandlerUtils.unregister(this);
    }

    /***
     * 同步,有结果返回
     * 这里"同步"的意思是A处调用postSync()方法后
     * 只有等到消息接收处的代码执行完并返回结果了,
     * 才会再继续执行A处下面的代码
     *
     * @param what 消息标志
     * @param objArray 传递的数据 传给onEvent(int what, Object[] objArray)的参数.
     */
    Object postSync(
            final Class clazz,
            final int what,
            final Object[] objArray) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (mClassMethodHashMap == null || mClassMethodHashMap.isEmpty()) {
            return null;
        }

        // It's not main thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Message msg = HandlerUtils.getMessage();
            msg.what = what;
            msg.obj = EventBus.this;
            HandlerUtils.sendMessageSync(
                    msg, new Object[]{clazz, objArray});
            return mObjResult;
        }

        // It's main thread.
        return dispatchEvent(clazz, what, objArray);
    }

    /***
     * 不需要返回结果
     *
     * @param what 消息标志
     * @param objArray 传递的数据
     */
    void postAsync(
            final Class clazz,
            final int what,
            final Object[] objArray) {
        postDelayed(clazz, what, objArray, 0);
    }

    /***
     * 需要返回结果
     *
     * @param what 消息标志
     * @param objArray 传递的数据
     */
    void postAsync(
            final Class clazz,
            final int what,
            final Object[] objArray,
            final EventBusUtils.AAsyncResult aAsyncResult) {
        postDelayed(clazz, what, objArray, aAsyncResult, 0);
    }

    void postDelayed(
            final Class clazz,
            final int what,
            final Object[] objArray,
            final long delayMillis) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (mClassMethodHashMap == null || mClassMethodHashMap.isEmpty()) {
            return;
        }
        Message msg = HandlerUtils.getMessage();
        msg.what = what;
        msg.obj = EventBus.this;
        HandlerUtils.sendMessageDelayed(
                msg, delayMillis, new Object[]{clazz, objArray});
    }

    /***
     * 需要返回结果
     *
     * @param what 消息标志
     * @param objArray 传递的数据
     */
    void postDelayed(
            final Class clazz,
            final int what,
            final Object[] objArray,
            final EventBusUtils.AAsyncResult aAsyncResult,
            final long delayMillis) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (mClassMethodHashMap == null || mClassMethodHashMap.isEmpty()) {
            return;
        }

        Message msg = HandlerUtils.getMessage();
        msg.what = what;
        msg.obj = EventBus.this;
        HandlerUtils.sendMessageDelayed(
                msg, delayMillis, new Object[]{clazz, objArray, aAsyncResult});
    }

    void clear() {
        if (mClassMethodHashMap != null) {
            synchronized (mClassMethodHashMap) {
                mClassMethodHashMap.clear();
            }
        }
    }

    private Object dispatchEvent(Class clazz, int what, Object[] objArray) {
        String sampleName = clazz.getSimpleName();

        Iterator<Map.Entry<Object, Method>> iter = null;
        iter = mClassMethodHashMap.entrySet().iterator();
        if (iter == null) {
            return null;
        }
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object keyObject = entry.getKey();
            if (keyObject.getClass().getSimpleName().equals(sampleName)) {
                Method method = (Method) entry.getValue();
                try {
                    /***
                     这里可能还有bug。就是keyObject是Activity或者Fragment时，退出这些组件后
                     如果再调用下面的代码，就有可能报异常。
                     */
                    if (method != null) {
                        /*Log.e(TAG, "EventBus dispatchEvent()keyObject: " + keyObject
                                + " what: " + what
                                + " objArray: " + objArray);*/
                        return method.invoke(keyObject, what, objArray);
                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "EventBus dispatchEvent() : IllegalAccessException");
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "EventBus dispatchEvent() : InvocationTargetException");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "EventBus dispatchEvent() : Exception");
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    /*private Object onEvent(int what, Object[] objArray) {
        Object result = null;
        switch (what){

            default:
        }
        return result;
    }*/

}
