package com.weidi.activity.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.weidi.activity.ScanCodeActivity;
import com.weidi.eventbus.EventBus;
import com.weidi.inject.InjectUtils;
import com.weidi.library.R;
import com.weidi.utils.PermissionsUtils;

public abstract class BaseActivity extends Activity {

    protected Context mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mContext = this.getApplicationContext();
        if (!(this instanceof ScanCodeActivity)) {
            InjectUtils.inject(this, null);
        }

        /**
         * 权限检查也是每个Activity都需要做的事，
         * 在Fragment中就不需要再去检查了。
         */
        PermissionsUtils.checkAndRequestPermission(this, getRequiredPermissions());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public abstract Object onEvent(int what, Object object);
    /***
     * 下面两个是在子类中实现：
     * 1.需要请求的权限
     * 2.用户对请求权限进行设置后的返回结果
     */
    public abstract String[] getRequiredPermissions();
    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    /***
     * 在2秒之间连续按两次“后退”键，才能退出应用。
     * 放到IndexActivity中去
     */
    //    protected void exit() {
    //        // 因为第一次按的时候“PRESS_TIME”为“0”，所以肯定大于2000
    //        if (SystemClock.uptimeMillis() - PRESS_TIME > TIME) {
    //            Toast.makeText(this,
    //                    "再按一次 退出" + getResources().getString(R.string.app_name),
    //                    Toast.LENGTH_SHORT).show();
    //            PRESS_TIME = SystemClock.uptimeMillis();
    //        } else {
    //            // 按第二次的时候如果距离前一次的时候在2秒之内，那么就走下面的路线
    //            //			APPManager.getDefault().exit();
    //
    //        }
    //    }

    /**
     * 打开页面时，页面从右往左滑入
     * 底下的页面不需要有动画
     */
    public void enterActivity() {
        try {
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } catch (Exception e) {
        }
    }

    /**
     * 关闭页面时，页面从左往右滑出
     */
    public void exitActivity() {
        try {
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        } catch (Exception e) {
        }
    }

    //记录Fragment的位置
    //    private int position = 0;
    //    @Override
    //    protected void onCreate(Bundle savedInstanceState) {
    //        super.onCreate(savedInstanceState);
    //        setContentView(R.layout.activity_index);
    //        setTabSelection(position);
    //    }
    //    @Override
    //    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    //        position = savedInstanceState.getInt("position");
    //        setTabSelection(position);
    //        super.onRestoreInstanceState(savedInstanceState);
    //    }
    //    @Override
    //    protected void onSaveInstanceState(Bundle outState) {
    //super.onSaveInstanceState(outState);   //将这一行注释掉，阻止activity保存fragment的状态
    //        //记录当前的position
    //        outState.putInt("position", position);
    //    }

}