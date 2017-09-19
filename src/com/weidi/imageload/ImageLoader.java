package com.weidi.imageload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.weidi.threadpool.ThreadPool;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/***
 Content Provider，asset，res的图片
 第一个：mSemaphorePoolThreadHandler = new Semaphore(0); 用于控制我们的mPoolThreadHandler的初始化完成，
 我们在使用mPoolThreadHandler会进行判空，如果为null，会通过mSemaphorePoolThreadHandler.acquire()进行阻塞；
 当mPoolThreadHandler初始化结束，我们会调用.release();解除阻塞。
 第二个：mSemaphoreThreadPool = new Semaphore(threadCount);这个信号量的数量和我们加载图片的线程个数一致；
 每取一个任务去执行，我们会让信号量减一；每完成一个任务，会让信号量+1，再去取任务；目的是什么呢？
 为什么当我们的任务到来时，如果此时在没有空闲线程，任务则一直添加到TaskQueue中，当线程完成任务，
 可以根据策略去TaskQueue中去取任务，只有这样，我们的LIFO才有意义。
 */
public class ImageLoader {

    private static final String TAG = "ImageLoader";
    private volatile static ImageLoader mImageLoader;

    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;

    private static final int DEAFULT_THREAD_COUNT = 1;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;

    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    private boolean isDiskCacheEnable = true;

    public enum Type {
        FIFO, LIFO;
    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    public static ImageLoader getInstance() {
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mImageLoader;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(threadCount, type);
                }
            }
        }
        return mImageLoader;
    }

    /**
     * 根据path为imageview设置图片
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final ImageView imageView, final String path, final boolean isFromNet) {
        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // 获取得到图片，为imageview回调设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageview = holder.imageView;
                    String path = holder.path;
                    // 将path与getTag存储路径进行比较
                    if (imageview.getTag().toString().equals(path)) {
                        imageview.setImageBitmap(bm);
                    }
                }
            };
        }

        // 根据path在缓存中获取bitmap
        Bitmap bitmap = getBitmapFromLruCache(path);

        if (bitmap != null) {
            Log.i(TAG, "getBitmapFromLruCache: " + path);
            refreashBitmap(imageView, path, bitmap);
        } else {
            addTask(buildTask(imageView, path, isFromNet));
        }
    }

    public void loadImage(final ImageView imageView, final int resId) {
        Bitmap bitmap = loadImageFromLocal(imageView, resId);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 初始化
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        initBackThread();

        // 获取我们应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

        };

        mTaskQueue = new LinkedList<Runnable>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    /**
     * 初始化后台轮询线程
     */
    private void initBackThread() {
        // 后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        // 线程池去取出一个任务进行执行
                        ThreadPool.getCachedThreadPool().execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                // 释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();
    }


    /**
     * 根据传入的参数，新建一个任务
     *
     * @param path
     * @param imageView
     * @param isFromNet
     * @return
     */
    private Runnable buildTask(
            final ImageView imageView, final String path, final boolean isFromNet) {
        return new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                if (isFromNet) {
                    File file = getDiskCacheDir(imageView.getContext(), md5(path));
                    // 如果在缓存文件中发现
                    if (file.exists()) {
                        Log.i(TAG, "Find image path: " + path);
                        bitmap = loadImageFromLocal(imageView, file.getAbsolutePath());
                    } else {
                        // 检测是否开启硬盘缓存
                        if (isDiskCacheEnable) {
                            boolean downloadState = DownloadImgUtils.downloadImgByUrl(path, file);
                            // 如果下载成功
                            if (downloadState) {
                                Log.i(TAG,
                                        "Download image path: " + path
                                                + " The save path: "
                                                + file.getAbsolutePath());
                                bitmap = loadImageFromLocal(imageView, file.getAbsolutePath());
                            }
                        } else {
                            // 直接从网络加载
                            Log.i(TAG, "Download image path: " + path);
                            bitmap = DownloadImgUtils.downloadImgByUrl(path, imageView);
                        }
                    }
                } else {
                    bitmap = loadImageFromLocal(imageView, path);
                }
                // 3、把图片加入到缓存
                addBitmapToLruCache(path, bitmap);
                refreashBitmap(imageView, path, bitmap);
                mSemaphoreThreadPool.release();
            }
        };
    }

    private Bitmap loadImageFromLocal(final ImageView imageView, final String path) {
        Bitmap bitmap = null;
        // 加载图片
        // 图片的压缩
        // 1、获得图片需要显示的大小
        ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageViewSize(imageView);
        // 2、压缩图片
        bitmap = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height);
        return bitmap;
    }

    private Bitmap loadImageFromLocal(final ImageView imageView, final int resId) {
        Bitmap bitmap = null;
        // 加载图片
        // 图片的压缩
        // 1、获得图片需要显示的大小
        ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageViewSize(imageView);
        // 2、压缩图片
        bitmap = decodeSampledBitmapFromLocal(imageView, resId, imageSize.width, imageSize.height);
        return bitmap;
    }

    /**
     * 从任务队列取出一个方法
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    /**
     * 利用签名辅助类，将字符串字节数组
     *
     * @param str
     * @return
     */
    private String md5(String str) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            digest = md.digest(str.getBytes());
            return bytes2hex02(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param bytes
     * @return
     */
    private String bytes2hex02(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bytes) {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF & b);
            // 每个字节8为，转为16进制标志，2个16进制位
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }

        return sb.toString();
    }

    private void refreashBitmap(ImageView imageView, String path, Bitmap bm) {
        Message message = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入LruCache
     *
     * @param path
     * @param bitmap
     */
    private void addBitmapToLruCache(String path, Bitmap bitmap) {
        if (getBitmapFromLruCache(path) == null) {
            if (bitmap != null) {
                mLruCache.put(path, bitmap);
            }
        }
    }

    /**
     * 根据图片需要显示的宽和高对图片进行压缩
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        // 获得图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = ImageSizeUtils.caculateInSampleSize(options, width, height);

        // 使用获得到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private Bitmap decodeSampledBitmapFromLocal(
            ImageView imageView, int resId, int width, int height) {
        // 获得图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(imageView.getContext().getResources(), resId, options);

        options.inSampleSize = ImageSizeUtils.caculateInSampleSize(options, width, height);

        // 使用获得到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(
                imageView.getContext().getResources(), resId, options);
        return bitmap;
    }

    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        // if(mPoolThreadHandler==null)wait();
        try {
            if (mPoolThreadHandler == null) {
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    /**
     * 获得缓存图片的地址
     *
     * @param context
     * @param uniqueName
     * @return
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 根据path在缓存中获取bitmap
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

}
