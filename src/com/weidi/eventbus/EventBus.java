package com.weidi.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
                public synchronized void handleMessage(Message msg) {
                    if (msg == null || mClass == null) {
                        return;
                    }

                    mResultObj = dispatchEvent(mClass, msg.what, msg.obj);
                    synchronized (objLock) {
                        objLock.notifyAll();
                    }

                    super.handleMessage(msg);
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

    private static final HashMap<Object, Method> classMethodHashMap = new HashMap<Object, Method>();
    private Object objLock = new Object();
    private Class mClass;
    private Object mResultObj;
    private Message mMessage;
    // 默认是同步,也就是说只有被调用处(即onEvent(){...})执行完了,才能在调用处继续往下走
    private boolean mIsAsync;

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
                e.printStackTrace();
            }

            if (method == null || classMethodHashMap == null) {
                return;
            }
            if (!classMethodHashMap.containsKey(object)) {
                classMethodHashMap.put(object, method);
            }
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

    public EventBus setIsAsync(boolean isAsync) {
        mIsAsync = isAsync;
        return this;
    }

    /***
     *
     * @param what 消息标志
     * @param objectData 传递的数据 异步时返回的结果一直为null
     */
    public Object post(final int what, final Object objectData) {
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

            if(mIsAsync){
                return null;
            }else{
                synchronized (objLock) {
                    try {
                        objLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return mResultObj;
            }
        }
        // It's main thread.
        if (mIsAsync) {
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
            return null;
        } else {
            return dispatchEvent(mClass, what, objectData);
        }
    }

    /***
     *
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
                        mResultObj = dispatchEvent(clazz, what, objectData);
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
                    e.printStackTrace();
                }
            }
            return mResultObj;
        }

        return dispatchEvent(clazz, what, objectData);
    }

    private Object dispatchEvent(Class clazz, int what, Object objectData) {
        String sampleName = clazz.getSimpleName();

        Iterator<Map.Entry<Object, Method>> iter = classMethodHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object keyObject = entry.getKey();
            if (keyObject.getClass().getSimpleName().equals(sampleName)) {
                Method method = (Method) entry.getValue();
                try {
                    return method.invoke(keyObject, what, objectData);
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
