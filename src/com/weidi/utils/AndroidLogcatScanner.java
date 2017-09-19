package com.weidi.utils;

import android.content.Context;
import android.content.Intent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by root on 17-5-18.
 */

public class AndroidLogcatScanner {

    private LogcatObserver mLogcatObserver;

    interface LogcatObserver {

        void handleLog(String info);

    }

    public void catchLog() {
        String[] cmds = {"logcat", "-c"};
        String shellCmd = "logcat";
        Process process = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        String line = null;
        Runtime runtime = Runtime.getRuntime();
        try {
            if (mLogcatObserver != null) {
                mLogcatObserver.handleLog(line);
            }
            int waitValue;
            waitValue = runtime.exec(cmds).waitFor();
            if (mLogcatObserver != null) {
                mLogcatObserver.handleLog("waitValue = " + waitValue +
                        "\n Has do Clear logcat cache.");
            }
            process = runtime.exec(shellCmd);
            inputStream = process.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                if (mLogcatObserver != null) {
                    mLogcatObserver.handleLog(line);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleLog(String info) {
        // TODO Auto-generated method stub
        Context context = null;
        if (info.contains("android.intent.action.DELETE") && info.contains(context.getPackageName
                ())) {

            Intent intent = new Intent();
            // intent.setClass(AndroidLogcatScannerService.this, UninstallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}
