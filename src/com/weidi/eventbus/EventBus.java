package com.weidi.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
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
                public void handleMessage(Message msg) {
                    /*synchronized (EventBus.this) {
                    }*/
                    if (msg == null || mClass == null) {
                        return;
                    }

                    mObjResult = dispatchEvent(mClass, msg.what, msg.obj);
                    synchronized (objLock) {
                        objLock.notifyAll();
                    }
                    if (mResultAsyncInterface != null) {
                        mResultAsyncInterface.onResult(mObjResult);
                    }

                    // super.handleMessage(msg);
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
    private Class mClass;
    private Object mObjResult;
    private Message mMessage;

    // 用于异步执行后返回结果，如果有结果的话
    //////////////////////////////////////////////////////////////////////////////////////

    private ResultAsyncInterface mResultAsyncInterface;

    public interface ResultAsyncInterface {

        void onResult(Object object);

    }

    // 用完之后赋为null
    public EventBus setResultAsyncInterface(ResultAsyncInterface resultAsyncInterface) {
        mResultAsyncInterface = resultAsyncInterface;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////

    public void init() {
        synchronized (this) {
            if (classMethodHashMap != null) {
                classMethodHashMap.clear();
            }
        }
    }

    public void register(Object object) {
        synchronized (this) {
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
    }

    public void unregister(Object object) {
        synchronized (this) {
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
    }

    public EventBus setClass(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        mClass = clazz;
        return this;
    }

    /***
     *
     * @param what 消息标志
     * @param objectData 传递的数据
     */
    public void postAsync(final int what, final Object objectData) {
        if (mClass == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return;
        }

        Message msg = null;
        if (mMessage != null) {
            msg = mMessage;
        } else {
            msg = mUiHandler.obtainMessage();
        }
        msg.what = what;
        msg.obj = objectData;
        mMessage = msg;
        mUiHandler.sendMessage(msg);
    }

    /***
     *
     * @param what 消息标志
     * @param objectData 传递的数据 异步时返回的结果一直为null
     */
    public Object postSync(final int what, final Object objectData) {
        if (mClass == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return null;
        }

        // It's not main thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Message msg = null;
            if (mMessage != null) {
                msg = mMessage;
            } else {
                msg = mUiHandler.obtainMessage();
            }
            msg.what = what;
            msg.obj = objectData;
            mMessage = msg;
            mUiHandler.sendMessage(msg);
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
        return dispatchEvent(mClass, what, objectData);
    }

    /***
     * 一般使用上面两个方法好了
     * @param clazz 向哪个类发送消息
     * @param what 消息标志
     * @param objectData 传递的数据
     */
    public Object post(final Class clazz, final int what, final Object objectData) {
        if (clazz == null) {
            throw new NullPointerException("EventBus post() : class = null");
        }
        if (classMethodHashMap == null || classMethodHashMap.isEmpty()) {
            return null;
        }

        // It's not main thread.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (mUiHandler != null) {
                mUiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mObjResult = dispatchEvent(clazz, what, objectData);
                        synchronized (objLock) {
                            objLock.notifyAll();
                        }
                    }
                });
            }

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

        return dispatchEvent(clazz, what, objectData);
    }

    private Object dispatchEvent(Class clazz, int what, Object objectData) {
        String sampleName = clazz.getSimpleName();

        Iterator<Map.Entry<Object, Method>> iter = null;
        synchronized (EventBus.this) {
            iter = classMethodHashMap.entrySet().iterator();
        }
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
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
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

    /*private ArrayList<EventListener> mEventListenerList = new ArrayList<EventListener>();
    public interface EventListener {

        void onEvent(int what, Object object);

    }*/

    /*public void register(EventListener listener) {
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
    }*/

    //////////////////////////////////////////////////////////////////////////////////////

}
