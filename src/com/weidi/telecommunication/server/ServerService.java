package com.weidi.telecommunication.server;

import android.content.Intent;
import android.os.IBinder;

import com.weidi.service.BaseService;
import com.weidi.threadpool.ThreadPool;

/**
 * Created by root on 17-4-11.
 */

public class ServerService extends BaseService {

    private static final String TAG = "ServerService";

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Object onEvent(int what, Object object) {
        return null;
    }

    private void init() {
        ThreadPool.getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Server.getInstance().accept();
            }
        });
    }

}
