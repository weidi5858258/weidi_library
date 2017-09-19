/*
 * Copyright (c) 2016, The Linux Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.weidi.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.weidi.library.R;

public class PermissionsActivity extends Activity {

    private static final String PACKAGE_URL_SCHEME = "package:";
    public static final String REQUEST_PERMISSIONS = "request_permissions";
    public static final int REQUEST_CODE = 100;
    private String[] mRequestedPermissons;

    /**
     * 需要请求的权限
     */
    /*String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /***
         * 需要请求的权限
         */
        mRequestedPermissons = getIntent().getStringArrayExtra(REQUEST_PERMISSIONS);
        if (savedInstanceState == null &&
                mRequestedPermissons != null &&
                mRequestedPermissons.length != 0) {
            requestPermissions(mRequestedPermissons, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        // 假定所有权限都同意了
        boolean isAllPermissionsGranted = true;
        if (requestCode != REQUEST_CODE ||
                permissions == null ||
                grantResults == null ||
                permissions.length == 0 ||
                grantResults.length == 0) {
            // 没有同意所有的权限请求
            isAllPermissionsGranted = false;
        } else {
            for (int i : grantResults) {
                // 不相等时表示没有权限
                if (i != PackageManager.PERMISSION_GRANTED) {
                    isAllPermissionsGranted = false;
                    break;
                }
            }
        }
        if (isAllPermissionsGranted) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            // 需要请求权限
            showMissingPermissionDialog();
        }
    }

    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_help);
        builder.setMessage(R.string.dialog_content);
        // 取消
        builder.setNegativeButton(R.string.dialog_button_quit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
        // 设置
        builder.setPositiveButton(R.string.dialog_button_settings,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
                        startActivity(intent);
                        finish();
                    }
                });
        builder.show();
    }

}
