package com.weidi.telecommunication.server;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.weidi.threadpool.ThreadPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * 把这个Server放到系统进程中去,这样我作为客户端就可以跟系统进程单独"聊天"了.
 */
public class Server {

    private static final String TAG = "Server";

    public static final String SERVER_NAME = "com.weidi.telecommunication.server.Server";
    private volatile static Server mServer;
    private static final String SEPARATOR = "_#####_";
    private HashMap<LocalSocket, PrintWriter> mSocketMap = new HashMap<LocalSocket, PrintWriter>();
    private String mUUID;
    private boolean mIsAccept;
    private boolean mIsReceiveMsg;

    private Server() {
        mIsAccept = true;
        mIsReceiveMsg = true;
    }

    public static Server getInstance() {
        if (mServer == null) {
            synchronized (Server.class) {
                if (mServer == null) {
                    mServer = new Server();
                }
            }
        }
        return mServer;
    }

    /**
     * 等待客户端的连接,需要开启线程进行操作
     */
    public void accept() {
        Log.d(TAG, "Server==========服务端进入accept()方法");
        LocalServerSocket serverSocket = null;
        mSocketMap.clear();
        try {
            serverSocket = new LocalServerSocket(SERVER_NAME);
        } catch (IOException e) {
            serverSocket = null;
            e.printStackTrace();
        }

        if (serverSocket == null) {
            return;
        }
        while (mIsAccept) {
            Log.d(TAG, "Server==========本机已作为服务端正在等待客户端的连接...");
            try {
                // 会阻塞的
                LocalSocket clientSocket = serverSocket.accept();

                if (!mSocketMap.containsKey(clientSocket)) {
                    PrintWriter printWriter = new PrintWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                    mSocketMap.put(clientSocket, printWriter);

                    BufferedReader bufferedReader = null;
                    try {
                        bufferedReader = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 每一个连接上的客户端,都单独开启一个线程用于接收这个客户端发出的消息.
                    if(bufferedReader != null){
                        final BufferedReader bufferedReaderTemp = bufferedReader;
                        ThreadPool.getFixedThreadPool().execute(new Runnable() {

                            @Override
                            public void run() {
                                receiveMessage(bufferedReaderTemp);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            if (mSocketMap == null || mSocketMap.isEmpty() || TextUtils.isEmpty(msg)) {
                return;
            }
            Iterator iterator = mSocketMap.keySet().iterator();
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
            while (iterator.hasNext()) {
                LocalSocket socket = (LocalSocket) iterator.next();
                if (socket == null) {
                    iterator.remove();
                    Log.d(TAG, "remove socket = " + socket);
                    continue;
                }
                PrintWriter printWriter = mSocketMap.get(socket);
                printWriter.println(stringBuilder.toString());
                printWriter.flush();
            }
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
            Log.d(TAG, "Server==========服务端进入while循环,一直监听消息的到来");
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
                Log.d(TAG, "Server==========receiveMsg: " + msg);

                if (uuid.equals(mUUID) || TextUtils.isEmpty(msg)) {
                    return;
                }
                sendMessage(msg);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
