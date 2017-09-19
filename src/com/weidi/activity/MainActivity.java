package com.weidi.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.weidi.activity.base.BaseActivity;
import com.weidi.fragment.base.BaseFragment;
import com.weidi.log.Log;

public class MainActivity extends BaseActivity implements BaseFragment.BackHandlerInterface {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate():savedInstanceState = " + savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "onStart()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (DEBUG) Log.d(TAG, "onRestart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.d(TAG, "onBackPressed()");
        //        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) Log.d(TAG, "onActivityResult():requestCode = " + requestCode +
                " resultCode = " + resultCode +
                " data = " + data);
        // super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (DEBUG) Log.d(TAG, "onSaveInstanceState():outState = " + outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (DEBUG)
            Log.d(TAG, "onRestoreInstanceState():savedInstanceState = " + savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 当配置发生变化时，不会重新启动Activity。但是会回调此方法，用户自行进行对屏幕旋转后进行处理
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (DEBUG) Log.d(TAG, "onConfigurationChanged():newConfig = " + newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void setSelectedFragment(BaseFragment selectedFragment, String fragmentTag) {
        // mMainActivityController.setSelectedFragment(selectedFragment, fragmentTag);
    }

    /*public MainActivityController getMainActivityController() {
        return mMainActivityController;
    }*/

}
