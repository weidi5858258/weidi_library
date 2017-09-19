package com.weidi.telecommunication.client;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.weidi.threadpool.ThreadPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * Created by root on 16-12-16.
 */

public class Client {

    private static final String TAG = "Client";
    public static final String SERVER_NAME = "com.sony.serverapp.Server";
    private static final String SEPARATOR = "_#####_";
    private String mUUID;

    private volatile static Client mClient;
    private static LocalSocket mClientSocket;

    private IRemoteConnection mIRemoteConnection;
    private boolean mIsReceiveMsg = true;
    private Handler msgHandler;
    private long msgSendTime = 0;
    private long msgReceiveTime = 0;

    private PrintWriter os;

    public interface IRemoteConnection {

        void onConnected(boolean isConnected);

    }

    private Client() {
        mIsReceiveMsg = true;
    }

    public static Client getInstance() {
        if (mClient == null) {
            synchronized (Client.class) {
                if (mClient == null) {
                    mClient = new Client();
                }
            }
        }
        return mClient;
    }

    public void setIRemoteConnection(IRemoteConnection iRemoteConnection) {
        mIRemoteConnection = iRemoteConnection;
    }

    public IRemoteConnection getIRemoteConnection() {
        return mIRemoteConnection;
    }

    public void setRemoteLocalSocket(LocalSocket socket) {
        mClientSocket = socket;
    }

    public LocalSocket getRemoteLocalSocket() {
        return mClientSocket;
    }

    public boolean ismIsReceiveMsg() {
        return mIsReceiveMsg;
    }

    public void setmIsReceiveMsg(boolean mIsReceiveMsg) {
        this.mIsReceiveMsg = mIsReceiveMsg;
    }

    public void setHandler(Handler handler) {
        msgHandler = handler;
    }

    public void connect() {
        try {
            mClientSocket = new LocalSocket();
            mClientSocket.connect(new LocalSocketAddress(SERVER_NAME));
            os = new PrintWriter(new OutputStreamWriter(mClientSocket.getOutputStream(), "UTF-8"));
            final BufferedReader bufferedReader =
                    new BufferedReader(
                            new InputStreamReader(mClientSocket.getInputStream(), "UTF-8"));
            ThreadPool.getFixedThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    receiveMessage(bufferedReader);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            if (TextUtils.isEmpty(msg)) {
                return;
            }
            long time = SystemClock.uptimeMillis();
            mUUID = UUID.randomUUID().toString();
            // cd22a181-9ac3-4fc0-a0c9-5e9e8da39da3
            // a22067f1-e143-432f-9241-63908a93b2ba
            // Log.d(TAG, "uuid = " + mUUID);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(time);
            stringBuilder.append(SEPARATOR);
            stringBuilder.append(mUUID);
            stringBuilder.append(SEPARATOR);
            stringBuilder.append(msg);

            os.println(stringBuilder.toString());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收文本消息
     * 服务端跟客户端建立好连接后,应该开一个线程去不断接收这个客户端发来的消息
     * 每个客户端都要有一个线程
     */
    public void receiveMessage(BufferedReader bufferedReader) {
        if (bufferedReader == null) {
            return;
        }
        try {
            Log.d(TAG, "Client==========服务端进入while循环,一直监听消息的到来");
            String receiveMsg = null;
            while (mIsReceiveMsg) {
                // 会阻塞的
                receiveMsg = bufferedReader.readLine();

                if (TextUtils.isEmpty(receiveMsg) || !receiveMsg.contains(SEPARATOR)) {
                    return;
                }
                String uuid = receiveMsg.split(SEPARATOR)[1];
                String msg = receiveMsg.split(SEPARATOR)[2];
                receiveMsg = null;
                Log.d(TAG, "Client==========receiveMsg: " + msg);

                if (uuid.equals(mUUID) || TextUtils.isEmpty(msg)) {
                    return;
                }
                // doSomething

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送文件
     */
    public void sendMessageOfFile(String filePath) {
        if (getRemoteLocalSocket() == null
                || !getRemoteLocalSocket().isConnected()
                || TextUtils.isEmpty(filePath)) {
            if (mIRemoteConnection != null) {
                Log.d(TAG, "客户端sendMessage()方法中出现条件不满足!");
                mIRemoteConnection.onConnected(false);
            }
            return;
        }
        try {
            OutputStream outputStream = getRemoteLocalSocket().getOutputStream();
            if (outputStream == null) {
                return;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            if (file.isDirectory()) {
                return;
            }
            if (!file.canRead()) {
                return;
            }
            //1.发送文件信息实体类
            outputStream.write("file".getBytes("utf-8"));
            // 将文件写入流
            FileInputStream fileInputStream = new FileInputStream(file);
            // 每次上传2M的内容
            byte[] bt = new byte[2048];
            int length = -1;
            int fileSize = 0;//实时监测上传进度
            while ((length = fileInputStream.read(bt)) != -1) {
                fileSize += length;
                // 2.把文件写入socket输出流
                outputStream.write(bt, 0, length);
                Log.i(TAG, "文件上传进度：" + (fileSize / file.length() * 100) + "%");
            }
            outputStream.flush();
            outputStream.close();
            // 关闭文件流
            fileInputStream.close();
            outputStream = null;
            fileInputStream = null;
            file = null;

            //该方法无效
            //outputStream.write("\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
