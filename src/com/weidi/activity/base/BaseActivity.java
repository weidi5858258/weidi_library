package com.weidi.activity.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.weidi.activity.ScanCodeActivity;
import com.weidi.inject.InjectUtils;
import com.weidi.library.R;

public abstract class BaseActivity extends Activity {

    private Context mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        if (!(this instanceof ScanCodeActivity)) {
            InjectUtils.inject(this, null);
        }
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
        super.onDestroy();
    }

    public Context getContext() {
        if (mContext == null) {
            mContext = this.getApplicationContext();
        }
        return mContext;
    }

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