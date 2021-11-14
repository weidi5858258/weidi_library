package com.weidi.eventbus;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
如何使用:
 1.先注册
     Phone.register(Object object);
 2.在已经注册过的类中加以下代码
     private Object onEvent(int what, Object[] objArray) {
         Object result = null;
         switch (what) {
             case 0: {
                break;
            }
             default:
                break;
         }
         return result;
     }
 3.调用不同的call...方法发送消息,最终调用到上面的方法
 4.反注册
     Phone.unregister(Object object);

注意点:
 1. "String className"中的className必须是类的完整名称,如 "com.weidi.media.wdplayer.MainActivity"
 2. 定义"int what"时,最好把这些what放到一个单独的常量类中去,这样可以防止相同的what被不同类使用时移除掉.
 3. "Object[] objArray"中的objArray除基本数据类型,String类型,enum类型直接传递外,
     其他对象最好使用弱引用传递,防止内存泄露(不同组件之间发送消息最好这样使用).
     比如:
     new Object[]{
         new WeakReference<Object>(Activity.this),
         new WeakReference<Object>(Service.this),
         new WeakReference<Object>(某个强引用对象),
         12345,
         "HelloWorld"
     }
 4. 最好不要发送what为0的消息.原因为:
     像callUiDelayed(final Runnable r, long delayMillis)这种延时消息,
     在没有执行之前想要移除掉的话,可以使用removeUiMessages(0)进行移除.

优点:
 1.不同组件之间发送消息(如Activity向Service发送一个消息进行某种操作)
 2.相同组件之间发送消息(如Activity中延时发送消息,就像使用Handler发送消息一样)
 3.同一个进程内,不需要在某些类中再创建Handler或HandlerThread对象了
缺点:
 register(Object object)时会进行清除操作,防止相同类的不同对象被保存,导致使用
 "String className"匹配时不知道匹配哪一个对象,因此相同路径下的类只能保存一个对象.

方法:(使用的api就只有12个,其他已经被注释起来了,减轻心里负担)
 call:              代码在原来的线程中(主线程或子线程)执行.
 callDelayed:       想把代码运行在原来线程中,并延时执行.
 callUi:            想把代码运行在主线程中.(一般在子线程中使用)
 callUiDelayed:     想把代码运行在主线程中,并延时执行.
 callThread:        想把代码运行在子线程中.(一般在主线程中使用)
 callThreadDelayed: 想把代码运行在子线程中,并延时执行.
 */

public class Phone {

    /*public static abstract class AAsyncResult implements Parcelable {

        public abstract void onResult(Object object);

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            return;
        }
    }*/

    /*public static void init() {
        EventBus.getDefault();
    }*/

    public static void register(Object phoneNumber) {
        EventBus.getDefault().register(phoneNumber);
    }

    public static void unregister(Object phoneNumber) {
        EventBus.getDefault().unregister(phoneNumber);
    }

    public static void unregister(String className) {
        EventBus.getDefault().unregister(className);
    }

    /***
     不需要去调用
     */
    public static void onDestroy() {
        EventBus.getDefault().onDestroy();
    }

    public static void setContext(Context context) {
        EventBus.getDefault().setContext(context);
    }

    public static Context getContext() {
        return EventBus.getDefault().getContext();
    }

    public static Handler getThreadHandler() {
        return EventBus.getDefault().getThreadHandler();
    }

    public static Handler getUiHandler() {
        return EventBus.getDefault().getUiHandler();
    }

    /***
     能得到结果(代码可能在UI线程或者Thread线程)
     使用这个api
     @param className 类的完整名称,如 "com.weidi.media.wdplayer.MainActivity"
     @param what 消息标志
     @param objArray 传递的数据 传给onEvent(int what, Object[] objArray)的参数.
     */
    public static Object call(final String className,
                              final int what,
                              final Object[] objArray) {
        return EventBus.getDefault().post(className, what, objArray);
    }

    public static Object callDelayed(final String className,
                                     final int what,
                                     final long delayMillis,
                                     final Object[] objArray) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            return EventBus.getDefault().postUiDelayed(className, what, delayMillis, objArray);
        } else {
            return EventBus.getDefault().postThreadDelayed(className, what, delayMillis, objArray);
        }
    }

    /*public static Object call(final Class clazz,
                              final int what,
                              final Object[] objArray) {
        return EventBus.getDefault().post(clazz, what, objArray);
    }*/

    /***
     能得到结果(代码可能在UI线程或者Thread线程)
     */
    /*public static Object call(final Object object,
                              final int what,
                              final Object[] objArray) {
        return EventBus.getDefault().post(object, what, objArray);
    }*/
    public static Object callUi(final Runnable r) {
        return EventBus.getDefault().postUi(r);
    }

    /***
     调用此方法是在UI线程时能得到结果,否则为null
     使用这个api
     */
    public static Object callUi(final String className,
                                final int what,
                                final Object[] objArray) {
        return EventBus.getDefault().postUi(className, what, objArray);
    }

    /*public static Object callUi(final Class clazz,
                                final int what,
                                final Object[] objArray) {
        return EventBus.getDefault().postUi(clazz, what, objArray);
    }*/

    /*public static Object callUi(final Object object,
                                final int what,
                                final Object[] objArray) {
        return EventBus.getDefault().postUi(object, what, objArray);
    }*/

    public static Object callUiDelayed(final Runnable r, long delayMillis) {
        return EventBus.getDefault().postUiDelayed(r, delayMillis);
    }

    /***
     返回结果为null
     使用这个api
     */
    public static Object callUiDelayed(final String className,
                                       final int what,
                                       long delayMillis,
                                       final Object[] objArray) {
        return EventBus.getDefault().postUiDelayed(className, what, delayMillis, objArray);
    }

    /*public static Object callUiDelayed(final Class clazz,
                                       final int what,
                                       long delayMillis,
                                       final Object[] objArray) {
        return EventBus.getDefault().postUiDelayed(clazz, what, delayMillis, objArray);
    }*/

    /*public static Object callUiDelayed(final Object object,
                                       final int what,
                                       long delayMillis,
                                       final Object[] objArray) {
        return EventBus.getDefault().postUiDelayed(object, what, delayMillis, objArray);
    }*/

    public static Object callThread(final Runnable r) {
        return EventBus.getDefault().postThread(r);
    }

    /***
     调用此方法是在Thread线程时能得到结果,否则为null
     使用这个api
     */
    public static Object callThread(final String className,
                                    final int what,
                                    final Object[] objArray) {
        return EventBus.getDefault().postThread(className, what, objArray);
    }

    /*public static Object callThread(final Class clazz,
                                    final int what,
                                    final Object[] objArray) {
        return EventBus.getDefault().postThread(clazz, what, objArray);
    }*/

    /*public static Object callThread(final Object object,
                                    final int what,
                                    final Object[] objArray) {
        return EventBus.getDefault().postThread(object, what, objArray);
    }*/

    public static Object callThreadDelayed(final Runnable r, long delayMillis) {
        return EventBus.getDefault().postThreadDelayed(r, delayMillis);
    }

    /***
     返回结果为null
     使用这个api
     */
    public static Object callThreadDelayed(final String className,
                                           final int what,
                                           long delayMillis,
                                           final Object[] objArray) {
        return EventBus.getDefault().postThreadDelayed(className, what, delayMillis, objArray);
    }

    /*public static Object callThreadDelayed(final Class clazz,
                                           final int what,
                                           long delayMillis,
                                           final Object[] objArray) {
        return EventBus.getDefault().postThreadDelayed(clazz, what, delayMillis, objArray);
    }*/

    /*public static Object callThreadDelayed(final Object object,
                                           final int what,
                                           long delayMillis,
                                           final Object[] objArray) {
        return EventBus.getDefault().postThreadDelayed(object, what, delayMillis, objArray);
    }*/

    public static void removeMessages(int what) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            EventBus.getDefault().removeUiMessages(what);
        } else {
            EventBus.getDefault().removeThreadMessages(what);
        }
    }

    public static void removeUiMessages(int what) {
        EventBus.getDefault().removeUiMessages(what);
    }

    public static void removeThreadMessages(int what) {
        EventBus.getDefault().removeThreadMessages(what);
    }

    //////////////////////////////////////////////////////////////////////////////////

    private static class EventBus {

        private static final String TAG =
                EventBus.class.getSimpleName();

        // String保存的是类的全路径名,ArrayList<Message>保存的是发送到String所代表的类的消息
        // 只保存有delayMillis的消息
        private static final HashMap<String, ArrayList<Message>> STRING_MESSAGE_MAP =
                new HashMap<String, ArrayList<Message>>();
        private static final HashMap<Message, Object[]> THREAD_MSG_MAP =
                new HashMap<Message, Object[]>();
        private static final HashMap<Message, Object[]> UI_MSG_MAP =
                new HashMap<Message, Object[]>();
        // String保存的是类的全路径名,如"com.weidi.media.wdplayer.MainActivity"
        private static final HashMap<String, WeakReference<Object>> STRING_OBJECT_MAP =
                new HashMap<String, WeakReference<Object>>();
        private static final HashMap<WeakReference<Object>, Method> OBJECT_METHOD_MAP =
                new HashMap<WeakReference<Object>, Method>();
        private static volatile Message sThreadMessage = null;
        private static volatile Message sUiMessage = null;

        private Context mContext;
        private HandlerThread mHandlerThread;
        private Handler mThreadHandler;
        private Handler mUiHandler;

        private static class InstanceHolder {
            private static EventBus sEventBus = new EventBus();
        }

        private EventBus() {
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    EventBus.this.threadHandleMessage(msg);
                }
            };

            mUiHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    EventBus.this.uiHandleMessage(msg);
                }
            };

            sThreadMessage = getThreadMsg();
            sUiMessage = getUiMsg();
        }

        static EventBus getDefault() {
            return InstanceHolder.sEventBus;
        }

        synchronized void register(Object object) {
            if (object == null) {
                Log.e(TAG, "EventBus register() object = null");
                return;
            }

            Class clazz = object.getClass();
            Method method = null;
            try {
                method = clazz.getDeclaredMethod("onEvent", int.class, Object[].class);
                method.setAccessible(true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(TAG, "EventBus register(): " + object + " NoSuchMethodException");
                method = null;
            } catch (Exception e) {
                e.printStackTrace();
                method = null;
            }
            if (method == null) {
                Log.e(TAG, "EventBus register() method = null");
                return;
            }

            Log.i(TAG,
                    "register()   before: mObjectMethodMap.size(): " + (OBJECT_METHOD_MAP.size()));
            Log.i(TAG,
                    "register()   before: mStringObjectMap.size(): " + (STRING_OBJECT_MAP.size()));

            // 防止相同类的不同对象被保存进去
            String name = clazz.getName();// 类的全路径名
            removeItem(name);

            WeakReference<Object> reference = new WeakReference<>(object);
            STRING_OBJECT_MAP.put(name, reference);
            OBJECT_METHOD_MAP.put(reference, method);
            STRING_MESSAGE_MAP.put(name, new ArrayList<Message>());

            Log.i(TAG,
                    "register()    after: mObjectMethodMap.size(): " + (OBJECT_METHOD_MAP.size()));
            Log.i(TAG,
                    "register()    after: mStringObjectMap.size(): " + (STRING_OBJECT_MAP.size()));
        }

        synchronized void unregister(Object object) {
            if (object == null) {
                Log.e(TAG, "EventBus unregister() object = null");
                return;
            }

            unregister(object.getClass().getName());
        }

        synchronized void unregister(String className) {
            if (TextUtils.isEmpty(className)) {
                Log.e(TAG, "EventBus unregister() className is empty");
                return;
            }

            Log.i(TAG,
                    "unregister() before: mObjectMethodMap.size(): " + (OBJECT_METHOD_MAP.size()));
            Log.i(TAG,
                    "unregister() before: mStringObjectMap.size(): " + (STRING_OBJECT_MAP.size()));

            removeItem(className);
            // 会把当前类和其他类提交的Runnable延时任务都给取消掉的
            mUiHandler.removeMessages(0);
            mThreadHandler.removeMessages(0);

            Log.i(TAG,
                    "unregister()  after: mObjectMethodMap.size(): " + (OBJECT_METHOD_MAP.size()));
            Log.i(TAG,
                    "unregister()  after: mStringObjectMap.size(): " + (STRING_OBJECT_MAP.size()));
        }

        private void removeItem(String className) {
            Iterator<Map.Entry<WeakReference<Object>, Method>> iter1 =
                    OBJECT_METHOD_MAP.entrySet().iterator();
            while (iter1.hasNext()) {
                Map.Entry<WeakReference<Object>, Method> entry = (Map.Entry) iter1.next();
                WeakReference<Object> reference = entry.getKey();
                Method method = entry.getValue();
                Object keyObject = reference.get();
                if (keyObject == null
                        || TextUtils.equals(keyObject.getClass().getName(), className)) {
                    iter1.remove();
                    keyObject = null;
                    reference = null;
                    method = null;
                }
            }

            Iterator<Map.Entry<String, WeakReference<Object>>> iter2 =
                    STRING_OBJECT_MAP.entrySet().iterator();
            while (iter2.hasNext()) {
                Map.Entry<String, WeakReference<Object>> entry = (Map.Entry) iter2.next();
                String name = entry.getKey();
                WeakReference<Object> reference = entry.getValue();
                Object object = reference.get();
                if (TextUtils.equals(name, className)
                        || reference == null
                        || object == null) {
                    iter2.remove();
                    name = null;
                    object = null;
                    reference = null;
                }
            }

            ArrayList<Message> list = STRING_MESSAGE_MAP.get(className);
            if (list != null && !list.isEmpty()) {
                Iterator<Message> iter = list.iterator();
                Object[] objects = null;
                int length = 0;
                while (iter.hasNext()) {
                    Message msg = iter.next();
                    objects = UI_MSG_MAP.get(msg);
                    if (objects != null) {
                        length = objects.length;
                        for (int i = 0; i < length; i++) {
                            objects[i] = null;
                        }
                    }
                    objects = THREAD_MSG_MAP.get(msg);
                    if (objects != null) {
                        length = objects.length;
                        for (int i = 0; i < length; i++) {
                            objects[i] = null;
                        }
                    }
                    UI_MSG_MAP.remove(msg);
                    THREAD_MSG_MAP.remove(msg);
                    mUiHandler.removeMessages(msg.what);
                    mThreadHandler.removeMessages(msg.what);
                    msg.obj = null;
                    msg = null;
                }
                list.clear();
            }
            list = null;
            STRING_MESSAGE_MAP.remove(className);
        }

        /***
         * 不需要去调用
         */
        void onDestroy() {
            Log.i(TAG, "onDestroy()");
            mThreadHandler.removeCallbacksAndMessages(null);
            mUiHandler.removeCallbacksAndMessages(null);
            if (mHandlerThread != null) {
                mHandlerThread.quitSafely();
                mHandlerThread = null;
            }
            mThreadHandler = null;
            mUiHandler = null;
        }

        void setContext(Context context) {
            mContext = context.getApplicationContext();
        }

        Context getContext() {
            return mContext;
        }

        Handler getThreadHandler() {
            return mThreadHandler;
        }

        Handler getUiHandler() {
            return mUiHandler;
        }

        /***
         * 同步
         * 代码可能执行于UI线程或者Thread线程
         *
         * 使用这个api
         *
         * @param what 消息标志
         * @param objArray 传递的数据 传给onEvent(int what, Object[] objArray)的参数.
         */
        Object post(final String className,
                    final int what,
                    final Object[] objArray) {
            return dispatchEvent(className, what, objArray);
        }

        Object post(final Class clazz,
                    final int what,
                    final Object[] objArray) {
            return dispatchEvent(clazz, what, objArray);
        }

        Object post(final Object object,
                    final int what,
                    final Object[] objArray) {
            return dispatchEvent(object, what, objArray);
        }

        Object postUi(final Runnable r) {
            return mUiHandler.post(r);
        }

        // 使用这个api
        Object postUi(final String className,
                      final int what,
                      final Object[] objArray) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                return dispatchEvent(className, what, objArray);
            } else {
                sendMsgAtTimeByUi(
                        getUiMsg(className, what), SystemClock.uptimeMillis(), objArray);
                return null;
            }
        }

        Object postUi(final Class clazz,
                      final int what,
                      final Object[] objArray) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                return dispatchEvent(clazz, what, objArray);
            } else {
                sendMsgAtTimeByUi(
                        getUiMsg(clazz, what), SystemClock.uptimeMillis(), objArray);
                return null;
            }
        }

        Object postUi(final Object object,
                      final int what,
                      final Object[] objArray) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                return dispatchEvent(object, what, objArray);
            } else {
                sendMsgAtTimeByUi(
                        getUiMsg(object, what), SystemClock.uptimeMillis(), objArray);
                return null;
            }
        }

        Object postUiDelayed(final Runnable r, long delayMillis) {
            if (delayMillis < 0) {
                delayMillis = 0;
            }
            return mUiHandler.postDelayed(r, delayMillis);
        }

        // 使用这个api
        Object postUiDelayed(final String className,
                             final int what,
                             long delayMillis,
                             final Object[] objArray) {
            if (delayMillis < 0) {
                delayMillis = 0;
            }
            Message msg = getUiMsg(className, what);
            ArrayList<Message> list = STRING_MESSAGE_MAP.get(className);
            if (list != null) {
                list.add(msg);
            }
            sendMsgAtTimeByUi(
                    msg, SystemClock.uptimeMillis() + delayMillis, objArray);
            return null;
        }

        Object postUiDelayed(final Class clazz,
                             final int what,
                             long delayMillis,
                             final Object[] objArray) {
            return postUiDelayed(clazz.getName(), what, delayMillis, objArray);
        }

        Object postUiDelayed(final Object object,
                             final int what,
                             long delayMillis,
                             final Object[] objArray) {
            return postUiDelayed(object.getClass().getName(), what, delayMillis, objArray);
        }

        Object postThread(final Runnable r) {
            return mThreadHandler.post(r);
        }

        // 使用这个api
        Object postThread(final String className,
                          final int what,
                          final Object[] objArray) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                sendMsgAtTimeByThread(
                        getThreadMsg(className, what), SystemClock.uptimeMillis(), objArray);
                return null;
            } else {
                return dispatchEvent(className, what, objArray);
            }
        }

        Object postThread(final Class clazz,
                          final int what,
                          final Object[] objArray) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                sendMsgAtTimeByThread(
                        getThreadMsg(clazz, what), SystemClock.uptimeMillis(), objArray);
                return null;
            } else {
                return dispatchEvent(clazz, what, objArray);
            }
        }

        Object postThread(final Object object,
                          final int what,
                          final Object[] objArray) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                sendMsgAtTimeByThread(
                        getThreadMsg(object, what), SystemClock.uptimeMillis(), objArray);
                return null;
            } else {
                return dispatchEvent(object, what, objArray);
            }
        }

        Object postThreadDelayed(final Runnable r, long delayMillis) {
            if (delayMillis < 0) {
                delayMillis = 0;
            }
            return mThreadHandler.postDelayed(r, delayMillis);
        }

        // 使用这个api
        Object postThreadDelayed(final String className,
                                 final int what,
                                 long delayMillis,
                                 final Object[] objArray) {
            if (delayMillis < 0) {
                delayMillis = 0;
            }
            Message msg = getThreadMsg(className, what);
            ArrayList<Message> list = STRING_MESSAGE_MAP.get(className);
            if (list != null) {
                list.add(msg);
            }
            sendMsgAtTimeByThread(
                    msg, SystemClock.uptimeMillis() + delayMillis, objArray);
            return null;
        }

        Object postThreadDelayed(final Class clazz,
                                 final int what,
                                 long delayMillis,
                                 final Object[] objArray) {
            return postThreadDelayed(clazz.getName(), what, delayMillis, objArray);
        }

        Object postThreadDelayed(final Object object,
                                 final int what,
                                 long delayMillis,
                                 final Object[] objArray) {
            return postThreadDelayed(object.getClass().getName(), what, delayMillis, objArray);
        }

        void removeUiMessages(int what) {
            if (!UI_MSG_MAP.isEmpty()) {
                synchronized (UI_MSG_MAP) {
                    Iterator<Map.Entry<Message, Object[]>> iter = UI_MSG_MAP.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<Message, Object[]> entry = (Map.Entry) iter.next();
                        Message msg = entry.getKey();
                        Object[] objects = entry.getValue();
                        if (msg == null || msg.what == what) {
                            if (objects != null) {
                                for (Object object : objects) {
                                    object = null;
                                }
                                objects = null;
                            }
                            msg = null;
                            iter.remove();
                        }
                    }
                }
            }
            // 其实还应该把mStringMessageMap中的msg也给移除掉的,但是因为没有类名找不到这个list,所以没法移除
            // 不过在unregister的时候会找到这个list,然后移除掉所有的msg
            mUiHandler.removeMessages(what);
        }

        void removeThreadMessages(int what) {
            if (!THREAD_MSG_MAP.isEmpty()) {
                synchronized (THREAD_MSG_MAP) {
                    Iterator<Map.Entry<Message, Object[]>> iter =
                            THREAD_MSG_MAP.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<Message, Object[]> entry = (Map.Entry) iter.next();
                        Message msg = entry.getKey();
                        Object[] objects = entry.getValue();
                        if (msg == null || msg.what == what) {
                            if (objects != null) {
                                for (Object object : objects) {
                                    object = null;
                                }
                                objects = null;
                            }
                            msg = null;
                            iter.remove();
                        }
                    }
                }
            }
            mThreadHandler.removeMessages(what);
        }

        private final Message getThreadMsg() {
            Message msg = null;
            if (sThreadMessage == null) {
                msg = mThreadHandler.obtainMessage();
                sThreadMessage = msg;
            } else {
                msg = Message.obtain(sThreadMessage);
            }
            return msg;
        }

        private final Message getThreadMsg(String className, int what) {
            Message msg = getThreadMsg();
            msg.obj = className;
            msg.what = what;
            return msg;
        }

        private final Message getThreadMsg(Class clazz, int what) {
            Message msg = getThreadMsg();
            msg.obj = clazz;
            msg.what = what;
            return msg;
        }

        private final Message getThreadMsg(Object object, int what) {
            Message msg = getThreadMsg();
            msg.obj = object;
            msg.what = what;
            return msg;
        }

        private final Message getUiMsg() {
            Message msg = null;
            if (sUiMessage == null) {
                msg = mUiHandler.obtainMessage();
                sUiMessage = msg;
            } else {
                msg = Message.obtain(sUiMessage);
            }
            return msg;
        }

        private final Message getUiMsg(String className, int what) {
            Message msg = getUiMsg();
            msg.obj = className;
            msg.what = what;
            return msg;
        }

        private final Message getUiMsg(Class clazz, int what) {
            Message msg = getUiMsg();
            msg.obj = clazz;
            msg.what = what;
            return msg;
        }

        private final Message getUiMsg(Object object, int what) {
            Message msg = getUiMsg();
            msg.obj = object;
            msg.what = what;
            return msg;
        }

        private final boolean sendMsgAtTimeByThread(final Message msg,
                                                    final long uptimeMillis,
                                                    final Object[] objArray) {
            if (objArray != null) {
                synchronized (THREAD_MSG_MAP) {
                    THREAD_MSG_MAP.put(msg, objArray);
                }
            }
            return mThreadHandler.sendMessageAtTime(msg, uptimeMillis);
        }

        private final boolean sendMsgAtTimeByUi(final Message msg,
                                                final long uptimeMillis,
                                                final Object[] objArray) {
            if (objArray != null) {
                synchronized (UI_MSG_MAP) {
                    UI_MSG_MAP.put(msg, objArray);
                }
            }
            return mUiHandler.sendMessageAtTime(msg, uptimeMillis);
        }

        void clear() {
            if (STRING_OBJECT_MAP != null) {
                synchronized (STRING_OBJECT_MAP) {
                    STRING_OBJECT_MAP.clear();
                }
            }
            if (OBJECT_METHOD_MAP != null) {
                synchronized (OBJECT_METHOD_MAP) {
                    OBJECT_METHOD_MAP.clear();
                }
            }
        }

        /***
         * @param className "com.weidi.media.wdplayer.MainActivity"必须传这样的String
         * @param what
         * @param objArray
         * @return
         */
        private Object dispatchEvent(String className, int what, Object[] objArray) {
            if (TextUtils.isEmpty(className)) {
                Log.e(TAG, "EventBus dispatchEvent() : className is empty");
                return null;
            }
            WeakReference<Object> reference = STRING_OBJECT_MAP.get(className);
            if (reference == null) {
                Log.e(TAG, "EventBus dispatchEvent() : reference is null");
                // 说明在unregister时有关对象已经被移除掉了
                STRING_OBJECT_MAP.remove(className);
                OBJECT_METHOD_MAP.remove(null);
                return null;
            }
            Object object = reference.get();
            if (object == null) {
                Log.e(TAG, "EventBus dispatchEvent() : object is null");
                STRING_OBJECT_MAP.remove(className);
                OBJECT_METHOD_MAP.remove(reference);
                return null;
            }
            Method method = OBJECT_METHOD_MAP.get(reference);
            if (method == null) {
                Log.e(TAG, "EventBus dispatchEvent() : method is null");
                STRING_OBJECT_MAP.remove(className);
                OBJECT_METHOD_MAP.remove(reference);
                return null;
            }

            try {
                return method.invoke(object, what, objArray);
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

            return null;
        }

        private Object dispatchEvent(Class clazz, int what, Object[] objArray) {
            if (clazz == null) {
                Log.e(TAG, "EventBus dispatchEvent() : clazz is null");
                return null;
            }

            return dispatchEvent(clazz.getName(), what, objArray);
        }

        /***
         * 如果知道要调用"onEvent"方法的对象,
         * 那么调用就更加简单.
         * @param object
         * @param what
         * @param objArray
         * @return
         */
        private Object dispatchEvent(Object object, int what, Object[] objArray) {
            if (object == null) {
                Log.e(TAG, "EventBus dispatchEvent() : object is null");
                return null;
            }

            return dispatchEvent(object.getClass().getName(), what, objArray);
        }

        private void threadHandleMessage(Message msg) {
            if (msg == null) {
                return;
            }

            Object object = msg.obj;
            if (object == null) {
                return;
            }

            Object[] objArray = THREAD_MSG_MAP.get(msg);
            if (object instanceof String) {
                String name = (String) msg.obj;
                dispatchEvent(name, msg.what, objArray);
                ArrayList<Message> list = STRING_MESSAGE_MAP.get(name);
                if (list != null) {
                    list.remove(msg);
                }
            } else if (object instanceof Class) {
                dispatchEvent((Class) msg.obj, msg.what, objArray);
            } else {
                dispatchEvent(msg.obj, msg.what, objArray);
            }

            synchronized (THREAD_MSG_MAP) {
                THREAD_MSG_MAP.remove(msg);
                if (objArray != null) {
                    for (Object obj : objArray) {
                        obj = null;
                    }
                    objArray = null;
                }
                msg = null;
            }
        }

        private void uiHandleMessage(Message msg) {
            if (msg == null) {
                return;
            }

            Object object = msg.obj;
            if (object == null) {
                return;
            }

            Object[] objArray = UI_MSG_MAP.get(msg);
            if (object instanceof String) {
                String name = (String) msg.obj;
                dispatchEvent(name, msg.what, objArray);
                ArrayList<Message> list = STRING_MESSAGE_MAP.get(name);
                if (list != null) {
                    list.remove(msg);
                }
            } else if (object instanceof Class) {
                dispatchEvent((Class) msg.obj, msg.what, objArray);
            } else {
                dispatchEvent(msg.obj, msg.what, objArray);
            }

            synchronized (UI_MSG_MAP) {
                UI_MSG_MAP.remove(msg);
                if (objArray != null) {
                    for (Object obj : objArray) {
                        obj = null;
                    }
                    objArray = null;
                }
                msg = null;
            }
        }

    }
}

