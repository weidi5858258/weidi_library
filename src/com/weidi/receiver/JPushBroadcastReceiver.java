package com.weidi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/***
 [MyReceiver] onReceive - cn.jpush.android.intent.NOTIFICATION_RECEIVED, extras:
 key: cn.jpush.android.ALERT, value: CentOS 镜像下载
 key: cn.jpush.android.NOTIFICATION_ID, value: 192872987
 key: cn.jpush.android.NOTIFICATION_CONTENT_TITLE, value: JPushDemo
 key: cn.jpush.android.MSG_ID, value: 2458588774
 [MyReceiver] 接收到推送下来的通知
 [MyReceiver] 接收到推送下来的通知的ID: 192872987

 [MyReceiver] onReceive - cn.jpush.android.intent.CONNECTION, extras:
 key: cn.jpush.android.APPKEY, value: 6a6c2048792e9fe542d22c25
 key: cn.jpush.android.CONNECTION_CHANGE, value: false 有时是 true

 [MyReceiver] onReceive - cn.jpush.android.intent.MESSAGE_RECEIVED, extras:
 key: cn.jpush.android.TITLE, value:
 key: cn.jpush.android.MESSAGE, value: 这是什么样的情况?
 key: cn.jpush.android.CONTENT_TYPE, value:
 key: cn.jpush.android.APPKEY, value: 6a6c2048792e9fe542d22c25
 key: cn.jpush.android.MSG_ID, value: 416088296
 [MyReceiver] 接收到推送下来的自定义消息: 这是什么样的情况?
 */
public class JPushBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "JPushBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null){
            return;
        }
        /*Bundle bundle = intent.getExtras();
        String action = intent.getAction();
        MLog.d(TAG, "onReceive(): " + action + ", extras: " + printBundle(bundle));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(action)) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            MLog.d(TAG, "接收Registration Id : " + regId);
            //send the Registration Id to your server...

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action)) {
            MLog.d(TAG, "接收到推送下来的自定义消息: " +
                    bundle.getString(JPushInterface.EXTRA_MESSAGE));
            processCustomMessage(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action)) {
            MLog.d(TAG, "接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            MLog.d(TAG, "接收到推送下来的通知的ID: " + notifactionId);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(action)) {
            MLog.d(TAG, "用户点击打开了通知");
            //
            //          //打开自定义的Activity
            //          Intent i = new Intent(context, TestActivity.class);
            //          i.putExtras(bundle);
            //          //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
            //          context.startActivity(i);

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(action)) {
            MLog.d(TAG, "用户收到到RICH PUSH CALLBACK: " +
                    bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(action)) {
            boolean connected = intent.getBooleanExtra(
                    JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            MLog.d(TAG, "[MyReceiver]" + action + " connected state change to " +
                    connected);

        } else {
            MLog.d(TAG, "Unhandled intent - " + action);

        }*/
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        return "!";
    }

    //send msg to MainActivity
    private void processCustomMessage(Context context, Bundle bundle) {

    }

}
