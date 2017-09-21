package com.weidi.telecommunication.client;

import android.content.Intent;
import android.os.IBinder;

import com.weidi.service.BaseService;

/**
 * Created by root on 17-4-11.
 */

public class ClientService extends BaseService {

    private static final String TAG = "ClientService";

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

    private void init() {
        Client.getInstance().connect();
    }

    @Override
    public Object onEvent(int what, Object object) {
        return null;
    }

}
