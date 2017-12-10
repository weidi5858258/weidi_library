package com.weidi.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.text.TextUtils;

import com.weidi.library.R;
import com.weidi.utils.EventBusUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.R.attr.fragment;

//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
/**
 * fragment在add,replace,hide,show时会调用哪些生命周期方法
 * <p>
 * class desc: Fragment操作类
 * 替换时删除id相同的fragment然后添加，只有一层，添加是多层
 * 对于fragment的使用基本有两种，
 * 一种是add方式后再进行show或者hide，这种方式切换fragment时不会让fragment重新刷新，
 * 而用replace方式会使fragment重新刷新，因为add方式是将fragment隐藏了而不是销毁再创建，
 * replace方式每次都是重新创建。
 */

/**
 * Fragment操作类
 * 1、有时候，我们需要在多个Fragment间切换，
 * 并且保存每个Fragment的状态。官方的方法是使用
 * replace()来替换Fragment，但是replace()的调用
 * 会导致Fragment的onCreteView()被调用，所以切换
 * 界面时会无法保存当前的状态。因此一般采用add()、hide()与show()配合，
 * 来达到保存Fragment的状态。
 * 2、第二个问题的出现正是因为使用了Fragment的状态保存，当系统内存不足，
 * Fragment的宿主Activity回收的时候，
 * Fragment的实例并没有随之被回收。
 * Activity被系统回收时，会主动调用onSaveInstance()
 * 方法来保存视图层（View Hierarchy），
 * 所以当Activity通过导航再次被重建时，
 * 之前被实例化过的Fragment依然会出现在Activity中，
 * 然而从上述代码中可以明显看出，再次重建了新的Fragment，
 * 综上这些因素导致了多个Fragment重叠在一起。
 * <p>
 * 在onSaveInstance()里面去remove()所有非空的Fragment，然后在onRestoreInstanceState()
 * 中去再次按照问题一的方式创建Activity。当我处于打开“不保留活动”的时候，效果非常令人满意，
 * 然而当我关闭“不保留活动”的时候，问题却出现了。当转跳到其他Activity
 * 、打开多任务窗口、使用Home回到主屏幕再返回时，发现根本没有Fragment了，一篇空白。
 * <p>
 * 于是跟踪下去，我调查了onSaveInstanceState()与onRestoreInstanceState()
 * 这两个方法。原本以为只有在系统因为内存回收Activity时才会调用的onSaveInstanceState()
 * ，居然在转跳到其他Activity、打开多任务窗口、使用Home回到主屏幕这些操作中也被调用，
 * 然而onRestoreInstanceState()
 * 并没有在再次回到Activity时被调用。而且我在onResume()发现之前的Fragment只是被移除，
 * 并不是空，所以就算你在onResume()
 * 中执行问题一中创建的Fragment的方法，同样无济于事。所以通过remove()宣告失败。
 * <p>
 * 接着通过调查资料发现Activity中的onSaveInstanceState()里面有一句
 * super.onRestoreInstanceState(savedInstanceState)
 * ，Google对于这句话的解释是“Always call the superclass so it can save the view hierarchy
 * state”，大概意思是“总是执行这句代码来调用父类去保存视图层的状态”。
 * 其实到这里大家也就明白了，就是因为这句话导致了重影的出现，于是我删除了这句话，然后onCreate()
 * 与onRestoreInstanceState()中同时使用问题一中的创建Fragment方法，
 * 然后再通过保存切换的状态，发现结果非常完美。
 * <p>
 * 只能在v4包中才能使用
 * fTransaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
 */
public class FragOperManager implements Serializable {

    private static final String TAG = "FragOperManager";
    private volatile static FragOperManager sFragOperManager;
    public static final int HIDE = 0;
    public static final int POPBACKSTACK = 1;
    public static final int POPBACKSTACKALL = 2;

    private FragOperManager() {
        EventBusUtils.register(this);
        this.mMapList = new ArrayList<Map<Activity, Integer>>();
        this.mFragmentsList = new ArrayList<Fragment>();
    }

    public static FragOperManager getInstance() {
        if (sFragOperManager == null) {
            synchronized (FragOperManager.class) {
                if (sFragOperManager == null) {
                    sFragOperManager = new FragOperManager();
                }
            }
        }
        return sFragOperManager;
    }

    /**
     * FragmentActivity 实例
     */
//    private Activity mActivity;
    private List<Map<Activity, Integer>> mMapList;
    /**
     * 该Activity所有fragment的集合
     */
    private List<Fragment> mFragmentsList;

    /**
     * BaseFragment 管理器
     */
//    private FragmentManager fManager;

    /**
     * 装Fragment的容器
     */
//    private int mContainerId;


    /**
     * @param activity
     * @param containerId
     */
    /*public FragOperManager(Activity activity, int containerId) {
        if (activity == null) {
            throw new NullPointerException("FragOperManager's mActivity is null.");
        }
        if (containerId <= 0) {
            throw new IllegalArgumentException("FragOperManager's mContainerId is Invalid.");
        }
//        this.mActivity = activity;
//        this.mContainerId = containerId;
//        this.fManager = activity.getFragmentManager();
        this.mFragmentsList = new ArrayList<Fragment>();
        EventBusUtils.register(this);
    }*/
    public void setActivityAndContainerId(Activity activity, int containerId) {
        Map<Activity, Integer> map = new HashMap<Activity, Integer>();
        map.put(activity, containerId);
        this.mMapList.add(map);
    }

    public void removeActivity(Activity activity) {
        if (activity == null
                || mMapList == null
                || mMapList.isEmpty()) {
            return;
        }
        Iterator<Map<Activity, Integer>> iterator = mMapList.iterator();
        while (iterator.hasNext()) {
            Map<Activity, Integer> map = iterator.next();
            if (map.containsKey(activity)) {
                iterator.remove();
                break;
            }
        }
    }

    public List<Fragment> getmFragmentsList() {
        return mFragmentsList;
    }

    /**
     * 不需要调用
     */
    public void onDestroy() {
        EventBusUtils.unregister(this);
    }

    /**
     * @param fragment
     * @param tag
     */
    public void enter(Activity activity, Fragment fragment, String tag) {
        if (activity == null
                || fragment == null
                || mMapList == null
                || mMapList.isEmpty()) {
            return;
        }
        Iterator<Map<Activity, Integer>> iterator = mMapList.iterator();
        int containerId = -1;
        while (iterator.hasNext()) {
            Map<Activity, Integer> map = iterator.next();
            if (map.containsKey(activity)) {
                containerId = map.get(activity);
                break;
            }
        }

        if (containerId <= 0) {
            return;
        }

        FragmentTransaction fTransaction = activity.getFragmentManager().beginTransaction();
        // 保证fragment在最后一个
        if (!mFragmentsList.contains(fragment)) {
            mFragmentsList.add(fragment);
            // 不用replace
            fTransaction.add(containerId, fragment, tag);
            fTransaction.addToBackStack(tag);
        } else {
            mFragmentsList.remove(fragment);
            mFragmentsList.add(fragment);
        }

        int count = mFragmentsList.size();
        for (int i = 0; i < count - 1; i++) {
            Fragment hideFragment = mFragmentsList.get(i);
            // fragment隐藏时的动画
            // fTransaction.setCustomAnimations(R.anim.push_right_in, R.anim.push_left_out2);
            // 先把所有的Fragment给隐藏掉.
            fTransaction.hide(hideFragment);
        }

        // fragment显示时的动画
        fTransaction.setCustomAnimations(
                R.animator.push_left_in, R.animator.push_left_out);
        fTransaction.show(fragment);
        // 旋转屏幕,然后去添加一个Fragment,出现异常
        // 旋转屏幕后
        // java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        fTransaction.commit();
    }

    /**
     * 在一个Fragment中的一个小区域再添加一个小的Fragment
     * 不过,添加之前,先要移除掉之前的Fragment
     * 就是先调用一下removeSomeOneFragment(String fragmentTag)方法
     *
     * @param fragment
     * @param tag
     * @param containerId
     */
    public void enter2(Activity activity, Fragment fragment, String tag, int containerId) {
        if (activity == null
                || fragment == null) {
            return;
        }

        FragmentTransaction fTransaction = activity.getFragmentManager().beginTransaction();
        fTransaction.add(containerId, fragment, tag);
        fTransaction.show(fragment);
        fTransaction.commit();

        /*if (!mFragmentsList.contains(fragment)) {
            mFragmentsList.add(fragment);
        }*/
    }

    /***
     * 如果退出Fragment时打算隐藏那么就传HIDE;
     * 如果退出Fragment时弹出后退栈那么就传POPBACKSTACK.
     * 如果退出时是隐藏的,那么在进入这个Fragment时它的对象不能再次new,只能new一次
     *  @param what
     * @param object
     */
    private Object onEvent(int what, Object[] object) {
        switch (what) {
            case HIDE:
                if (object != null && object.length >= 2) {
                    // 隐藏某个Fragment,而不是弹出.
                    exit((Activity) object[0], (Fragment) object[1], HIDE);
                }
                break;

            case POPBACKSTACK:
                if (object != null && object.length >= 2) {
                    // 弹出某个Fragment,而不是隐藏.
                    exit((Activity) object[0], (Fragment) object[1], POPBACKSTACK);
                }
                break;

            case POPBACKSTACKALL:
                // 在某个Fragment时出现了某种情况,应用需要退出,那么需要先把所有的Fragment给移除掉.
                popBackStackAll();
                break;

            case 10000:
                if (object != null && object.length >= 2) {
                    removeSomeOneFragment((Activity) object[0], (String) object[1]);
                }
                break;

            default:
        }
        return what;
    }

    /**
     * @param fragment
     * @param exitType
     */
    private void exit(Activity activity, Fragment fragment, int exitType) {
        if (fragment == null) {
            throw new NullPointerException("要进入的fragment不能为null.");
        }
        if (mFragmentsList == null
                || mFragmentsList.isEmpty()
                || !mFragmentsList.contains(fragment)) {
            return;
        }

        FragmentManager fManager = activity.getFragmentManager();
        FragmentTransaction fTransaction = fManager.beginTransaction();
        // 不需要先加载一个Fragment
        switch (exitType) {
            case HIDE:
                fTransaction.hide(fragment);
                int count = mFragmentsList.size();
                if (count <= 1) {
                    break;
                }
                Fragment showFragment = mFragmentsList.get(count - 1);
                fTransaction.setCustomAnimations(
                        R.animator.push_left_in, R.animator.push_left_out);
                fTransaction.show(showFragment);
                mFragmentsList.remove(fragment);
                mFragmentsList.add(0, fragment);
                break;

            case POPBACKSTACK:
                fManager.popBackStack();
                mFragmentsList.remove(fragment);
                count = mFragmentsList.size();
                if (count < 1) {
                    fTransaction.hide(fragment);
                    break;
                }
                for (int i = 0; i < count; i++) {
                    Fragment hideFragment = mFragmentsList.get(i);
                    fTransaction.hide(hideFragment);
                }
                showFragment = mFragmentsList.get(count - 1);
                // fragment显示时的动画
                fTransaction.setCustomAnimations(
                        R.animator.push_left_in, R.animator.push_left_out);
                fTransaction.show(showFragment);
                break;

            default:
        }
        fTransaction.commit();

        // 这段代码不要删除
        // 这里有个前提条件,就是进入MainActivity时首先需要加载一个MainFragment,
        // 并且这个MainFragment一直存在着.这样执行下面的代码就没有问题.
        /***
         switch (exitType) {
         case HIDE:
         fTransaction.hide(fragment);
         mFragmentsList.remove(fragment);
         Fragment showFragment = mFragmentsList.get(mFragmentsList.size() - 1);
         fTransaction.show(showFragment);
         mFragmentsList.add(0, fragment);
         break;

         case POPBACKSTACK:
         fManager.popBackStack();
         mFragmentsList.remove(fragment);
         int count = mFragmentsList.size();
         for (int i = 0; i < count; i++) {
         Fragment hideFragment = mFragmentsList.get(i);
         fTransaction.hide(hideFragment);
         }
         showFragment = mFragmentsList.get(count - 1);
         fTransaction.show(showFragment);
         break;

         default:
         }
         fTransaction.commit();
         */
    }

    private void popBackStackAll() {
        if (mFragmentsList == null || mFragmentsList.isEmpty()) {
            return;
        }
        Iterator<Map<Activity, Integer>> iterator = mMapList.iterator();
        while (iterator.hasNext()) {
            Map<Activity, Integer> map = iterator.next();
            for (Map.Entry<Activity, Integer> entry : map.entrySet()) {
                FragmentTransaction fTransaction =
                        entry.getKey().getFragmentManager().beginTransaction();
                Iterator<Fragment> iter = mFragmentsList.iterator();
                while (iter.hasNext()) {
                    Fragment fragment = iter.next();
                    fTransaction.remove(fragment);
                    iter.remove();
                }
                fTransaction.commit();
            }
        }
    }

    /***
     移除某个Fragment.什么场景下会用到这个方法呢?
     就是某个FragmentA页面中又有另一个FragmentB,那么再次添加FragmentA时,
     FragmentB会显示不了,需要把之前的FragmentA给删除掉才可以.
     @param fragmentTag
     */
    private void removeSomeOneFragment(Activity activity, String fragmentTag) {
        if (TextUtils.isEmpty(fragmentTag)
                || mFragmentsList == null
                || mFragmentsList.isEmpty()) {
            return;
        }
        Fragment fragmentTemp = null;
        FragmentTransaction fTransaction = activity.getFragmentManager().beginTransaction();
        for (Fragment fragment : mFragmentsList) {
            if (fragmentTag.equals(fragment.getClass().getSimpleName())) {
                fragmentTemp = fragment;
                fTransaction.remove(fragment);
                break;
            }
        }
        fTransaction.commit();
        if (fragmentTemp != null
                || mFragmentsList.contains(fragmentTemp)) {
            mFragmentsList.remove(fragmentTemp);
        }
    }

}
