package com.weidi.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/***
 * Created by root on 17-2-4.
 * 应用内通信
 */
public class EventBus {

    private static final String TAG = "EventBus";

    private volatile static EventBus sEventBus;

    private Handler mUiHandler;

    public EventBus() {
        if (mUiHandler == null) {
            mUiHandler = new Handler(Looper.getMainLooper()) {

                @Override
                public synchronized void handleMessage(Message msg) {
                    Log.i(TAG, "EventBus handleMessage() start");
                    if (msg == null || msg.obj == null || !(msg.obj instanceof MyMessage)) {
                        return;
                    }

                    MyMessage myMessage = (MyMessage) msg.obj;

                    mObjResult = dispatchEvent(myMessage.clazz, myMessage.what, myMessage.obj);
                    synchronized (objLock) {
                        objLock.notifyAll();
                    }
                    if (myMessage.aAsyncResult != null) {
                        myMessage.aAsyncResult.onResult(mObjResult);
                    }

                    Log.i(TAG, "EventBus handleMessage() end");
                }
            };
        }
    }

    public static EventBus getDefault() {
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

    private static final HashMap<Object, Method> classMethodHashMap =
            new HashMap<Object, Method>();
    // 属于当前对象为好，所以不设置成static
    private final Object objLock = new Object();
    private Object mObjResult;
    private static Message mMessage;

    public synchronized void init() {
        if (classMethodHashMap != null) {
            classMethodHashMap.clear();
        }
    }

    public synchronized void register(Object object) {
        if (object == null) {
            throw new NullPointerException("EventBus register() : class = null");
        }
        Class clazz = object.getClass();
        Method method = null;

        try {
            method = clazz.getDeclaredMethod("onEvent", int.class, Object.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "EventBus register() : NoSuchMethodException");
            e.printStackTrace();
        }

        if (method == null || classMethodHashMap == null) {
            return;
        }
        classMethodHashMap.put(object, method);
    }

    public synchronized void unregister(Object object) {
        if (object == null) {
            throw new NullPointerException("EventBus unregister() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return;
        }
        if (!classMethodHashMap.containsKey(object)) {
            return;
        }
        String sampleName = object.getClass().getSimpleName();

        Iterator<Map.Entry<Object, Method>> iter = classMethodHashMap.entrySet().iterator();
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
     * 同步,有结果返回
     * 这里"同步"的意思是A处调用postSync()方法后
     * 只有等到消息接收处的代码执行完并返回结果了,
     * 才会再继续执行A处下面的代码
     *
     * @param what 消息标志
     * @param objectData 传递的数据
     */
    public synchronized Object postSync(
            final Class clazz, final int what, final Object objectData) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return null;
        }

        // It's not main thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Message msg = null;
            if (mMessage != null) {
                msg = Message.obtain(mMessage);
                Log.i(TAG, "EventBus postSync() obtain(mMessage)");
            } else {
                msg = mUiHandler.obtainMessage();
                Log.i(TAG, "EventBus postSync() obtainMessage()");
            }
            MyMessage myMessage = new MyMessage();
            myMessage.clazz = clazz;
            myMessage.what = what;
            myMessage.obj = objectData;
            msg.obj = myMessage;
            mMessage = msg;
            mUiHandler.sendMessage(msg);
            Log.i(TAG, "EventBus postSync() sendMessage()");
            synchronized (objLock) {
                try {
                    objLock.wait();
                } catch (InterruptedException e) {
                    mObjResult = null;
                    e.printStackTrace();
                }
            }
            return mObjResult;
        }
        // It's main thread.
        return dispatchEvent(clazz, what, objectData);
    }

    public synchronized Object postSync(
            final Class clazz, final int what, final Object objectData,
            final AAsyncResult aAsyncResult) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return null;
        }

        // It's not main thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Message msg = null;
            if (mMessage != null) {
                msg = Message.obtain(mMessage);
                Log.i(TAG, "EventBus postSync() obtain(mMessage)");
            } else {
                msg = mUiHandler.obtainMessage();
                Log.i(TAG, "EventBus postSync() obtainMessage()");
            }
            MyMessage myMessage = new MyMessage();
            myMessage.clazz = clazz;
            myMessage.what = what;
            myMessage.obj = objectData;
            myMessage.aAsyncResult = aAsyncResult;
            msg.obj = myMessage;
            mMessage = msg;
            mUiHandler.sendMessage(msg);
            Log.i(TAG, "EventBus postSync() sendMessage()");
            synchronized (objLock) {
                try {
                    objLock.wait();
                } catch (InterruptedException e) {
                    mObjResult = null;
                    e.printStackTrace();
                }
            }
            return mObjResult;
        }
        // It's main thread.
        return dispatchEvent(clazz, what, objectData);
    }

    /***
     * 不需要返回结果
     *
     * @param what 消息标志
     * @param objectData 传递的数据
     */
    public void postAsync(
            final Class clazz, final int what, final Object objectData) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return;
        }

        Message msg = null;
        if (mMessage != null) {
            msg = Message.obtain(mMessage);
            Log.i(TAG, "EventBus postAsync() obtain(mMessage)");
        } else {
            msg = mUiHandler.obtainMessage();
            Log.i(TAG, "EventBus postAsync() obtainMessage()");
        }
        MyMessage myMessage = new MyMessage();
        myMessage.clazz = clazz;
        myMessage.what = what;
        myMessage.obj = objectData;
        msg.obj = myMessage;
        mMessage = msg;
        mUiHandler.sendMessage(msg);
        Log.i(TAG, "EventBus postAsync() sendMessage()");
    }

    /***
     * 需要返回结果
     *
     * @param what 消息标志
     * @param objectData 传递的数据
     */
    public void postAsync(
            final Class clazz, final int what, final Object objectData,
            final AAsyncResult aAsyncResult) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return;
        }

        Message msg = null;
        if (mMessage != null) {
            msg = Message.obtain(mMessage);
            Log.i(TAG, "EventBus postAsync() obtain(mMessage)");
        } else {
            msg = mUiHandler.obtainMessage();
            Log.i(TAG, "EventBus postAsync() obtainMessage()");
        }
        MyMessage myMessage = new MyMessage();
        myMessage.clazz = clazz;
        myMessage.what = what;
        myMessage.obj = objectData;
        myMessage.aAsyncResult = aAsyncResult;
        msg.obj = myMessage;
        mMessage = msg;
        mUiHandler.sendMessage(msg);
        Log.i(TAG, "EventBus postAsync() sendMessage()");
    }

    private Object dispatchEvent(Class clazz, int what, Object objectData) {
        String sampleName = clazz.getSimpleName();

        Iterator<Map.Entry<Object, Method>> iter = null;
        iter = classMethodHashMap.entrySet().iterator();
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
                        return method.invoke(keyObject, what, objectData);
                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "EventBus dispatchEvent() : IllegalAccessException");
                    e.printStackTrace();
                    synchronized (objLock) {
                        objLock.notifyAll();
                    }
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "EventBus dispatchEvent() : InvocationTargetException");
                    e.printStackTrace();
                    synchronized (objLock) {
                        objLock.notifyAll();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "EventBus dispatchEvent() : Exception");
                    e.printStackTrace();
                    synchronized (objLock) {
                        objLock.notifyAll();
                    }
                }
                break;
            }
        }
        return null;
    }

    /*private Object onEvent(int what, Object object) {
        Object result = null;
        switch (what){

            default:
        }
        return result;
    }*/

    //////////////////////////////////////////////////////////////////////////////////////

    // 下面的代码不打算使用
    //////////////////////////////////////////////////////////////////////////////////////

    private ArrayList<EventListener> mEventListenerList = new ArrayList<EventListener>();

    public interface EventListener {

        void onEvent(int what, Object object);

    }

    public void register(EventListener listener) {
        if (mEventListenerList != null && !mEventListenerList.contains(listener)) {
            mEventListenerList.add(listener);
        }
    }

    public void unregister(EventListener listener) {
        if (mEventListenerList != null && mEventListenerList.contains(listener)) {
            mEventListenerList.remove(listener);
        }
    }

    public synchronized void post(int what, Object object) {
        if (mEventListenerList != null && !mEventListenerList.isEmpty()) {
            int count = mEventListenerList.size();
            for (int i = 0; i < count; i++) {
                mEventListenerList.get(i).onEvent(what, object);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////

    public static abstract class AAsyncResult implements Parcelable {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            return;
        }

        public abstract void onResult(Object object);
    }

    private static class MyMessage implements Parcelable {

        Class clazz;
        int what;
        Object obj;
        AAsyncResult aAsyncResult;

        public MyMessage() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.clazz);
            dest.writeInt(this.what);
            if (obj != null) {
                try {
                    Parcelable p = (Parcelable) obj;
                    dest.writeInt(1);
                    dest.writeParcelable(p, flags);
                } catch (ClassCastException e) {
                    throw new RuntimeException(
                            "Can't marshal non-Parcelable objects across processes.");
                }
            } else {
                dest.writeInt(0);
            }
            dest.writeParcelable(this.aAsyncResult, flags);
        }

        protected MyMessage(Parcel in) {
            this.clazz = (Class) in.readSerializable();
            this.what = in.readInt();
            this.obj = in.readParcelable(Object.class.getClassLoader());
            this.aAsyncResult = in.readParcelable(AAsyncResult.class.getClassLoader());
        }

        public static final Parcelable.Creator<MyMessage> CREATOR =
                new Parcelable.Creator<MyMessage>() {

                    @Override
                    public MyMessage createFromParcel(Parcel source) {
                        return new MyMessage(source);
                    }

                    @Override
                    public MyMessage[] newArray(int size) {
                        return new MyMessage[size];
                    }
                };
    }

}
