package com.weidi.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    // CPU核心数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    // private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int MAXIMUM_POOL_SIZE = 20;

    private volatile static ExecutorService cachedService = null;
    private volatile static ExecutorService singleService = null;
    private volatile static ExecutorService fixedService = null;
    private volatile static ExecutorService scheduledService = null;
    private volatile static ExecutorService singleScheduledService = null;

    /**
     * @Description 不固定数目的线程池（线程里的任务执行时间不要超过60s）
     * 一般使用这个方法就行了
     */
    public static ExecutorService getCachedThreadPool() {
        // 双重校验锁，在JDK1.5之后，才能够正常达到单例效果
        if (cachedService == null) {
            synchronized (ThreadPool.class) {
                if (cachedService == null) {
                    cachedService = Executors.newCachedThreadPool();
                }
            }
        }
        return cachedService;
    }

    /**
     * @Description 单个数目的线程池（线程里的任务执行时间没有限制）
     */
    public static ExecutorService getSingleThreadPool() {
        if (singleService == null) {
            synchronized (ThreadPool.class) {
                if (singleService == null) {
                    singleService = Executors.newSingleThreadExecutor();
                }
            }
        }
        return singleService;
    }

    /**
     * 使用这个好了
     * @Description 固定数目的线程池（线程里的任务执行时间没有限制）
     */
    public static ExecutorService getFixedThreadPool() {
        if (fixedService == null) {
            synchronized (ThreadPool.class) {
                if (fixedService == null) {
                    fixedService = Executors.newFixedThreadPool(MAXIMUM_POOL_SIZE);
                }
            }
        }
        return fixedService;
    }

    /**
     * @Description 固定数目的线程池（线程里的任务执行时间没有限制）
     */
    public static ExecutorService getFixedThreadPool(int nThreads) {
        if (fixedService == null) {
            synchronized (ThreadPool.class) {
                if (fixedService == null) {
                    if (nThreads <= 0) {
                        fixedService = Executors.newFixedThreadPool(MAXIMUM_POOL_SIZE);
                    } else {
                        fixedService = Executors.newFixedThreadPool(nThreads);
                    }
                }
            }
        }
        return fixedService;
    }

    /**
     * @Description 固定数目的线程池（线程里的任务按时间、顺序执行）
     */
    public static ExecutorService getScheduledThreadPool(int corePoolSize) {
        if (scheduledService == null) {
            synchronized (ThreadPool.class) {
                if (scheduledService == null) {
                    if (corePoolSize <= 0) {
                        scheduledService = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
                    } else {
                        scheduledService = Executors.newScheduledThreadPool(corePoolSize);
                    }
                }
            }
        }
        return scheduledService;
    }

    /**
     * @Description 单个数目的线程池（线程里的任务按时间、顺序执行）
     */
    public static ExecutorService getSingleScheduledThreadPool() {
        if (singleScheduledService == null) {
            synchronized (ThreadPool.class) {
                if (singleScheduledService == null) {
                    singleScheduledService = Executors.newSingleThreadScheduledExecutor();
                }
            }
        }
        return singleScheduledService;
    }

}
