package com.weidi.activity.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import com.weidi.activity.ScanCodeActivity;
import com.weidi.fragment.FragOperManager;
import com.weidi.fragment.base.BaseFragment;
import com.weidi.inject.InjectUtils;
import com.weidi.library.R;
import com.weidi.utils.EventBusUtils;
import com.weidi.utils.HandlerUtils;

import java.util.HashMap;
import java.util.List;


public abstract class BaseActivity
        extends Activity
        implements BaseFragment.BackHandlerInterface {

    private Context mContext = null;
    private Bundle mSavedInstanceState;
    private BaseFragment mBaseFragment;
    private String mFragmentTag;
    private static HashMap<String, Integer> sFragmentBackTypeSMap;

    static {
        // MainFragment不要加入Map中
        sFragmentBackTypeSMap = new HashMap<String, Integer>();
        sFragmentBackTypeSMap.put("AppsManagerFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("SettingsFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("BluetoothFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("AlarmClockFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("QrCodeFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("DataBackupAndRestoreFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("SmsFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("PhoneFragment", FragOperManager.POPBACKSTACK);
        sFragmentBackTypeSMap.put("TestImageFragment", FragOperManager.POPBACKSTACK);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this.getApplicationContext();
        if (savedInstanceState != null) {
            this.mSavedInstanceState = savedInstanceState;
        }
        if (!(this instanceof ScanCodeActivity)) {
            InjectUtils.inject(this, null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        /***
         做这个事的原因:
         如果有多个Fragment开启着,并且相互之间是显示和隐藏,而不是弹出,
         (如果后退时Fragment是弹出的话,不需要这样的代码的;
         如果这些Fragment是像QQ那样实现的底部导航栏形式的,
         在任何一个页面都可以退出,那么也不需要实现这样的代码的);
         那么页面在MainFragment时关闭屏幕,然后在点亮屏幕后,
         MainFragment的onResume()方法比其他Fragment的onResume()方法要先执行,
         最后执行的Fragment就得到了后退的"焦点",
         这样的话要后退时导致在MainFragment页面时就退不出去了.
         */
        if (this.mSavedInstanceState != null) {
            final String fragmentTag = this.mSavedInstanceState.getString("FragmentTag");
            List<Fragment> fragmentsList = FragOperManager.getInstance().getFragmentsList();
            if(fragmentsList == null){
                return;
            }
            FragOperManager.getInstance().enter(this, mBaseFragment, null);
            int count = fragmentsList.size();
            for (int i = 0; i < count; i++) {
                final Fragment fragment = fragmentsList.get(i);
                if (fragment != null && fragment.getClass().getSimpleName().equals(fragmentTag)) {
                    if (fragment instanceof BaseFragment) {
                        HandlerUtils.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (((BaseFragment) fragment).getBackHandlerInterface() != null) {
                                    ((BaseFragment) fragment).getBackHandlerInterface()
                                            .setSelectedFragment(
                                                    (BaseFragment) fragment,
                                                    fragmentTag);
                                }
                            }
                        }, 500);
                    }
                    break;
                }
            }
            this.mSavedInstanceState = null;
        }
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
        mSavedInstanceState = null;
        FragOperManager.getInstance().removeActivity(this);
        super.onDestroy();
        exitActivity();
    }

    /***
     * 锁屏时也会被调
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("FragmentTag", this.mFragmentTag);
        this.mSavedInstanceState = outState;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /***
     * 如果多个Fragment以底部Tab方式呈现的话,
     * 那么这些Fragment中的onBackPressed()方法最好返回true.
     * 这样就不需要在MainActivityController中处理onResume()方法了.
     * 如果不是以这种方式呈现,那么这些Fragment中的onBackPressed()方法最好返回false.
     * 然后需要在MainActivityController中处理onResume()方法了.
     */
    @Override
    public void onBackPressed() {
        if (mBaseFragment == null || mBaseFragment.onBackPressed()) {
            this.finish();
            this.exitActivity();
            return;
        }

        // 实现后退功能(把当前Fragment进行pop或者hide)
        final String fragmentName = mBaseFragment.getClass().getSimpleName();
        for (String key : sFragmentBackTypeSMap.keySet()) {
            if (key.equals(fragmentName)) {
                int type = sFragmentBackTypeSMap.get(key);
                EventBusUtils.postAsync(
                        FragOperManager.class,
                        type,
                        new Object[]{this, mBaseFragment});
                break;
            }
        }
    }

    public Context getContext() {
        if (this.mContext == null) {
            this.mContext = this.getApplicationContext();
        }
        return this.mContext;
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

    public void setSelectedFragment(BaseFragment selectedFragment, String fragmentTag) {
        mBaseFragment = selectedFragment;
        mFragmentTag = fragmentTag;
    }

    /***
     * 放到Activity的onCreate()方法中去调用
     * @param containerId
     */
    protected void setFragmentContainerId(int containerId) {
        FragOperManager.getInstance().setActivityAndContainerId(this, containerId);
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