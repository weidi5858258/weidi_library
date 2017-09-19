package com.weidi.fragment.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.activity.MainActivity;
import com.weidi.activity.base.BaseActivity;
import com.weidi.inject.InjectUtils;
import com.weidi.library.R;
import com.weidi.log.Log;

/**
 *
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";
    private static final boolean DEBUG = true;
    protected MainActivity mMainActivity;
    private Context mContext;
    private BackHandlerInterface mBackHandlerInterface;
    private boolean mIsNeedToDo = true;

    public interface BackHandlerInterface {

        void setSelectedFragment(BaseFragment selectedFragment, String fragmentTag);

    }

    public BaseFragment() {
        super();
    }

    /*********************************
     * Created
     *********************************/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (DEBUG)
            Log.d(TAG, "onAttach(): activity = " + activity);
        mMainActivity = (MainActivity) activity;
        mContext = activity.getApplicationContext();
        if (!(activity instanceof BackHandlerInterface)) {
            throw new ClassCastException("Hosting Activity must implement BackHandlerInterface");
        } else {
            mBackHandlerInterface = (BackHandlerInterface) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 一旦我们设置 setRetainInstance(true)，意味着在 Activity 重绘时，
         * 我们的 BaseFragment 不会被重复绘制，也就是它会被“保留”。为了验证
         * 其作用，我们发现在设置为 true 状态时，旋转屏幕，BaseFragment 依然是
         * 之前的 BaseFragment。而如果将它设置为默认的 false，那么旋转屏幕时
         * BaseFragment 会被销毁，然后重新创建出另外一个 fragment 实例。并且
         * 如官方所说，如果 BaseFragment 不重复创建，意味着 BaseFragment 的
         * onCreate 和 onDestroy 方法不会被重复调用。所以在旋转屏
         * BaseFragment 中，我们经常会设置 setRetainInstance(true)，
         * 这样旋转时 BaseFragment 不需要重新创建。
         *
         * setRetainInstance(true)
         * 当旋转屏幕时Fragment的onCreate()和onDestroy()方法不会被调用,
         * 但是其他生命周期方法都会被调用到.
         * onCreateView()和onActivityCreated()方法中的Bundle参数一直为null.
         * 这行代码在Activity的配置发生变化时onCreate()和onDestroy()方法
         * 不执行的情况下才有用,如果执行的话处理不好反而会发生不好的情况.
         * 如果实现这句代码的话,那么初始化工作放到onAttach(Activity activity)方法中去.
         *
         */
        setRetainInstance(true);
        if (DEBUG)
            Log.d(TAG, "onCreate(): savedInstanceState = " + savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (DEBUG)
            Log.d(TAG, "onCreateView(): savedInstanceState = " + savedInstanceState);
        // 如果写成inflater.inflate(provideLayout(), container)这样的话,
        // 那么会报异常,具体异常就是已经有一个子类的parent,添加之前先要移除这个parent.
        View view = null;
        if (savedInstanceState == null) {
            view = inflater.inflate(provideLayout(), null);
            InjectUtils.inject(this, view);
            afterInitView(inflater, container, savedInstanceState);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG)
            Log.d(TAG, "onActivityCreated(): savedInstanceState = " + savedInstanceState);
    }

    /*********************************
     * Started
     *********************************/

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG)
            Log.d(TAG, "onStart()");
    }

    /*********************************
     * Resumed
     *********************************/

    @Override
    public void onResume() {
        super.onResume();
        onResume_();
    }

    /*********************************
     * Paused
     *********************************/

    @Override
    public void onPause() {
        super.onPause();
        onPause_();
    }

    /*********************************
     * Stopped
     *********************************/

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG)
            Log.d(TAG, "onStop()");
    }

    /*********************************
     * Destroyed
     *********************************/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (DEBUG)
            Log.d(TAG, "onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG)
            Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (DEBUG)
            Log.d(TAG, "onDetach()");
    }

    /**
     * Very important
     * true表示被隐藏了,false表示被显示了
     * Fragment:
     * 被show()或者hide()时才会回调这个方法,
     * 被add()或者popBackStack()时不会回调这个方法
     * 弹窗时不会被回调(是由当前的Fragment弹出的一个DialogFragment)
     * 如果是弹出一个DialogActivity窗口,则应该会被回调,
     * 因为当前Fragment所在的Activity的生命周期发生了变化,则当前Fragment的生命周期也会发生变化
     *
     * @param hidden if true that mean hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (DEBUG) Log.d(TAG, "onHiddenChanged():hidden = " + hidden);
        if (hidden) {
            onPause_();
        } else {
            onResume_();
        }
        super.onHiddenChanged(hidden);
    }

    private void onResume_() {
        if (DEBUG)
            Log.d(TAG, "onResume(): " + this);
        if (mBackHandlerInterface != null && mIsNeedToDo) {
            mBackHandlerInterface.setSelectedFragment(this, this.getClass().getName());
        }
    }

    private void onPause_() {
        if (DEBUG)
            Log.d(TAG, "onPause(): " + this);
    }

    public Activity getAttachedActivity() {
        return mMainActivity;
    }

    public Context getMyContext() {
        if (mContext == null) {
            if (getAttachedActivity() != null) {
                mContext = getAttachedActivity().getApplicationContext();
            } else if (getActivity() != null) {
                mContext = getActivity().getApplicationContext();
            }
        }
        return mContext;
    }

    public BackHandlerInterface getBackHandlerInterface() {
        return mBackHandlerInterface;
    }

    protected void setNeedToDo(boolean isNeedToDo) {
        mIsNeedToDo = isNeedToDo;
    }

    public void enterFragment() {
        if (getAttachedActivity() != null) {
            ((BaseActivity) getAttachedActivity()).enterActivity();
        }
    }

    public void exitFragment() {
        if (getAttachedActivity() != null) {
            ((BaseActivity) getAttachedActivity()).exitActivity();
        }
    }

    protected abstract int provideLayout();

    /**
     * 供子类调用，初始化组件，统一接口
     *
     * @return
     */
    //    protected abstract void initViewBefore(Bundle savedInstanceState);

    /**
     * 供子类调用，初始化数据，统一接口
     *
     * @return
     */
    protected abstract void afterInitView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState);

    /**
     * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
     * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
     * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
     * 除了像QQ那样有底部导航栏,并且是由Fragment组成的,那么这几个Fragment返回true外,其他的都返回false.
     */
    public abstract boolean onBackPressed();

    /**
     * 打开页面时，页面从右往左滑入
     * 底下的页面不需要有动画
     */
    public void startFragmentAnim() {
        try {
            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } catch (Exception e) {
        }
    }

    /**
     * 关闭页面时，页面从左往右滑出
     */
    public void finishFragmentAnim() {
        try {
            getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        } catch (Exception e) {
        }
    }

}
