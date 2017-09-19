package com.weidi.volley;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by weidi on 2017/8/3.
 */

public class ThreadPoolManager {

    private static final String TAG = "ThreadPoolManager";

    private static volatile ThreadPoolManager sThreadPoolManager;

    /***
     在Java多线程应用中，队列的使用率很高，
     多数生产消费模型的首选数据结构就是队列(先进先出)。
     Java提供的线程安全的Queue可以分为阻塞队列和非阻塞队列，
     其中阻塞队列的典型例子是BlockingQueue，
     非阻塞队列的典型例子是ConcurrentLinkedQueue，
     在实际应用中要根据实际需要选用阻塞队列或者非阻塞队列。

     注：什么叫线程安全？这个首先要明确。线程安全就是说多线程访问同一代码，不会产生不确定的结果。
     并行和并发区别

     1、并行是指两者同时执行一件事，比如赛跑，两个人都在不停的往前跑；
     2、并发是指资源有限的情况下，两者交替轮流使用资源，
     比如一段路(单核CPU资源)同时只能过一个人，
     A走一段后，让给B，B用完继续给A ，交替使用，目的是提高效率

     由于LinkedBlockingQueue实现是线程安全的，
     实现了先进先出等特性，是作为生产者消费者的首选，
     LinkedBlockingQueue 可以指定容量，也可以不指定，
     不指定的话，默认最大是Integer.MAX_VALUE，
     其中主要用到put和take方法(阻塞方法)，
     put方法在队列满的时候会阻塞直到有队列成员被消费，
     take方法在队列空的时候会阻塞，
     直到有队列成员被放进来。

     阻塞式，就是没有任务可以执行时，会暂停在那里，一直等待新任务的到来
     LinkedBlockingQueue不接受null
     */
    private LinkedBlockingQueue<Runnable> mLinkedBlockingQueue =
            new LinkedBlockingQueue<Runnable>();

    private ThreadPoolExecutor mThreadPoolExecutor;

    private ThreadPoolManager() {
        /***
         int corePoolSize,
         int maximumPoolSize,
         long keepAliveTime,
         TimeUnit unit,
         BlockingQueue<Runnable> workQueue,
         ThreadFactory threadFactory,
         RejectedExecutionHandler handler
         */
        mThreadPoolExecutor = new ThreadPoolExecutor(
                4,// 根据CPU核心数来定
                20,// 最大并发数量，这里并不是设置成越大越好，太大了，手机会很卡的
                120,// 每个线程最长执行时间
                TimeUnit.SECONDS,// 执行时间的单位
                new ArrayBlockingQueue<Runnable>(4),// 工作队列，设置成阻塞队列
                rejectedExecutionHandler);// 超出线程最大数量时，超出部分的线程的处理

        // 相当于永动机
        new Thread(mRunnable).start();
    }

    public static ThreadPoolManager getInstance() {
        if (sThreadPoolManager == null) {
            synchronized (ThreadPoolManager.class) {
                if (sThreadPoolManager == null) {
                    sThreadPoolManager = new ThreadPoolManager();
                }
            }
        }

        return sThreadPoolManager;
    }

    /***
     提供给外面用于添加任务
     */
    public void execute(Runnable runnable) {
        if (runnable != null
                && mLinkedBlockingQueue != null
                && !mLinkedBlockingQueue.contains(runnable)) {
            // add方法在添加元素的时候，若超出了队列的长度会直接抛出异常
            // 因为在创建队列时没有指定大小,所以默认是整数的最大值,因此我们添加的任务不太可能会走出
            // offer方法在添加元素时，如果发现队列已满无法添加的话，会直接返回false
            // 在此处,add方法跟put方法的效果都是一样的
            mLinkedBlockingQueue.add(runnable);
        }
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            while (true) {
                Runnable runnable = null;
                try {
                    /***
                     进行消费
                     如果队列中没有东西可以消费时,会一直阻塞着

                     poll: 若队列为空，返回null
                     remove: 若队列为空，抛出NoSuchElementException异常
                     take: 若队列为空，发生阻塞，等待有元素
                     */
                    runnable = mLinkedBlockingQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (runnable != null && mThreadPoolExecutor != null) {
                    // 线程执行器
                    mThreadPoolExecutor.execute(runnable);
                }
            }
        }
    };

    /***
     比方说中最多有20个线程可以同时工作,
     但是第21个线程到来时,这第21个线程该怎么
     处理,这里就是把它重新加入队列中
     */
    private RejectedExecutionHandler rejectedExecutionHandler =
            new RejectedExecutionHandler() {

                @Override
                public void rejectedExecution(
                        Runnable runnable,
                        ThreadPoolExecutor threadPoolExecutor) {
                    if (runnable != null && mLinkedBlockingQueue != null) {
                        try {
                            // 重新加入
                            // 对于put方法，若向队尾添加元素的时候发现队列
                            // 已经满了会发生阻塞一直等待空间，以加入元素
                            mLinkedBlockingQueue.put(runnable);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

}
