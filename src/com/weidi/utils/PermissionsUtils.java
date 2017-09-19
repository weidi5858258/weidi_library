package com.weidi.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.weidi.activity.PermissionsActivity;
import com.weidi.activity.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weidi on 16-5-26.
 */
public class PermissionsUtils {

    public static boolean checkAndRequestPermission(Activity activity, String[] permissions) {
        String[] neededPermissions = checkRequestedPermission(activity, permissions);
        if(neededPermissions == null || neededPermissions.length == 0){
            // 不需要请求权限
            if(activity instanceof BaseActivity){
                ((BaseActivity)activity).onActivityResult(
                        PermissionsActivity.REQUEST_CODE,
                        Activity.RESULT_OK,
                        null);
                return false;
            }
        }
        // 需要请求权限
        Intent intent = new Intent();
        intent.setClass(activity, PermissionsActivity.class);
        intent.putExtra(PermissionsActivity.REQUEST_PERMISSIONS, permissions);
        activity.startActivityForResult(intent, PermissionsActivity.REQUEST_CODE);
        return true;
    }

    private static String[] checkRequestedPermission(Activity activity, String[] permissionName) {
        if(permissionName == null || permissionName.length == 0){
            return null;
        }
        boolean isPermissionGranted = true;
        List<String> needRequestPermission = new ArrayList<String>();
        for (String tmp : permissionName) {
            isPermissionGranted = (PackageManager.PERMISSION_GRANTED ==
                    activity.checkSelfPermission(tmp));
            if (!isPermissionGranted) {
                // 被拒绝的权限
                needRequestPermission.add(tmp);
            }
        }
        if(needRequestPermission.isEmpty()){
            return null;
        }
        // 有需要请求的权限
        String[] needRequestPermissionArray = new String[needRequestPermission.size()];
        needRequestPermission.toArray(needRequestPermissionArray);
        return needRequestPermissionArray;
    }

}
