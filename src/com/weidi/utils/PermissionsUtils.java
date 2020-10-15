package com.weidi.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;

/***
 首先:
 BaseActivity或当前Activity写上下面代码
 @Override public void onRequestPermissionsResult(int requestCode,
 String[] permissions,
 int[] grantResults) {
 if (permissions != null) {
 for (String permission : permissions) {
 Log.i(TAG, "onRequestPermissionsResult(): " + permission);
 }
 }
 PermissionsUtils.onRequestPermissionsResult(
 this,
 permissions,
 grantResults);
 }

 使用:
 PermissionsUtils.checkAndRequestPermission(
 new PermissionsUtils.IRequestPermissionsResult() {

 @Override public Activity getRequiredActivity() {
 return MainActivity.this;
 }

 @Override public String[] getRequiredPermissions() {
 return PermissionsUtils.REQUIRED_PERMISSIONS;
 }

 @Override public void onRequestPermissionsResult() {
 MLog.i(TAG, "onCreate():onRequestPermissionsResult");
 PackageManager packageManager = getPackageManager();
 if (PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(
 Manifest.permission.SYSTEM_ALERT_WINDOW,
 mContext.getPackageName())) {
 // do something
 }
 }
 });
 */
public class PermissionsUtils {

    private static final String TAG = "PermissionsUtils";

    /***
     当一个Activity中需要有多个权限时,
     放在一起申请,然后在回调接口中判断
     有什么样的权限就进行什么样的操作.
     */
    public static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.VIBRATE,

            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_SMS,
            // 申请不了的
            // android.Manifest.permission.READ_PHONE_NUMBERS,

            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public interface IRequestPermissionsResult {
        Activity getRequiredActivity();

        String[] getRequiredPermissions();

        void onRequestPermissionsResult();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkAndRequestPermission(
            IRequestPermissionsResult requestPermissionsResult) {
        RequestPermissions.getInstance().checkAndRequestPermission(requestPermissionsResult);
    }

    /***
     系统把结果先返回给BaseActivity,
     然后BaseActivity再返回到这里进行下一步处理
     * @param activity
     * @param permissions
     * @param grantResults
     */
    public static void onRequestPermissionsResult(
            Activity activity,
            String[] permissions,
            int[] grantResults) {
        RequestPermissions.getInstance().onRequestPermissionsResult(
                activity,
                permissions,
                grantResults);
    }

    private static class RequestPermissions {

        private static RequestPermissions sRequestPermissions;
        private IRequestPermissionsResult mIRequestPermissionsResult;

        public static RequestPermissions getInstance() {
            if (sRequestPermissions == null) {
                synchronized (RequestPermissions.class) {
                    if (sRequestPermissions == null) {
                        sRequestPermissions = new RequestPermissions();
                    }
                }
            }
            return sRequestPermissions;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void checkAndRequestPermission(
                IRequestPermissionsResult requestPermissionsResult) {
            if (requestPermissionsResult == null
                    || requestPermissionsResult.getRequiredActivity() == null
                    || !(requestPermissionsResult.getRequiredActivity() instanceof Activity)) {
                return;
            }
            mIRequestPermissionsResult = requestPermissionsResult;
            Activity activity = mIRequestPermissionsResult.getRequiredActivity();
            String[] permissions = mIRequestPermissionsResult.getRequiredPermissions();
            String[] neededPermissions = getNeedRequestedPermissions(activity, permissions);
            if (neededPermissions == null || neededPermissions.length == 0) {
                // 不需要请求权限
                onRequestPermissionsResult();
                return;
            }

            // 调用系统方法去请求权限
            activity.requestPermissions(neededPermissions, 0x0001);
        }

        /***
         系统把结果先返回给BaseActivity,
         然后BaseActivity再返回到这里进行下一步处理
         * @param activity
         * @param permissions
         * @param grantResults
         */
        public void onRequestPermissionsResult(
                Activity activity,
                String[] permissions,
                int[] grantResults) {
            if (activity == null) {
                throw new NullPointerException("onRequestPermissionsResult() activity is null!");
            }
            // 假定所有权限都同意了
            boolean isAllPermissionsGranted = true;
            if (permissions == null ||
                    grantResults == null ||
                    permissions.length == 0 ||
                    grantResults.length == 0) {
                // 没有同意所有的权限请求
                isAllPermissionsGranted = false;
            } else {
                for (int grantResult : grantResults) {
                    // 不相等时表示没有权限
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        isAllPermissionsGranted = false;
                        break;
                    }
                }
            }
            if (isAllPermissionsGranted) {
                onRequestPermissionsResult();
            } else {
                if (!activity.isDestroyed()) {
                    // 需要请求权限
                    showMissingPermissionDialog(activity);
                }
            }
        }

        private static final String dialog_title_help = "Help";
        private static final String dialog_content =
                "This application can\\'t work without required " +
                        "permissions. Please grant the permissions in Settings.";
        private static final String dialog_button_quit = "Quit";
        private static final String dialog_button_settings = "Settings";

        /***
         * 提示用户去设置打开权限
         * @param activity
         */
        private void showMissingPermissionDialog(
                final Activity activity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(dialog_title_help);
            builder.setMessage(dialog_content);
            // 取消
            builder.setNegativeButton(
                    dialog_button_quit,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!activity.isDestroyed() && dialog != null) {
                                dialog.dismiss();
                            }

                            onRequestPermissionsResult();
                        }
                    });
            // 设置
            builder.setPositiveButton(
                    dialog_button_settings,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings
                                    .ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + activity.getPackageName()));
                            activity.startActivity(intent);
                            activity.finish();
                            mIRequestPermissionsResult = null;
                            // requires android.permission.GRANT_RUNTIME_PERMISSIONS
                        /*Intent intent = new Intent();
                        ComponentName componentName = new ComponentName(
                                "com.android.packageinstaller",
                                "com.android.packageinstaller.permission.ui
                                .ManagePermissionsActivity");
                        intent.setComponent(componentName);
                        intent.setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivity(intent);
                        activity.finish();*/
                        }
                    });
            builder.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private String[] getNeedRequestedPermissions(
                Activity activity,
                String[] permissionNames) {
            if (permissionNames == null || permissionNames.length == 0) {
                return null;
            }
            List<String> needRequestPermission = new ArrayList<String>();
            for (String permissionName : permissionNames) {
                boolean isPermissionGranted = (PackageManager.PERMISSION_GRANTED ==
                        activity.checkSelfPermission(permissionName));
                if (!isPermissionGranted) {
                    // 被拒绝的权限
                    needRequestPermission.add(permissionName);
                }
            }
            if (needRequestPermission.isEmpty()) {
                return null;
            }
            // 有需要请求的权限
            String[] needRequestPermissionArray = new String[needRequestPermission.size()];
            needRequestPermission.toArray(needRequestPermissionArray);
            return needRequestPermissionArray;
        }

        private void onRequestPermissionsResult() {
            if (mIRequestPermissionsResult != null) {
                mIRequestPermissionsResult.onRequestPermissionsResult();
                mIRequestPermissionsResult = null;
            }
        }

    }

}
