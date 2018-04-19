package com.weidi.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import java.util.List;

public class WifiUtils {

    // first do it at Application
    public static void init(Context context) {
        InnerWifiManager.setContext(context);
        InnerWifiManager.getInstance();
    }

    /**
     * 打开wifi
     */
    public void openWifi() {
        InnerWifiManager.getInstance().openWifi();
    }

    /**
     * 关闭wifi
     */
    public void closeWifi() {
        InnerWifiManager.getInstance().closeWifi();
    }

    /**
     * 搜索wifi
     */
    public void scanWifi() {
        InnerWifiManager.getInstance().scanWifi();
    }

    /***
     * 断开指定SSID的网络
     * 断开是指连接好的wifi可以把它断开达到不使用的结果.
     *
     * @param SSID wifi账号
     */
    public void disconnectWifi(String SSID) {
        InnerWifiManager.getInstance().disconnectWifi(SSID);
    }

    /**
     * 断开指定ID的网络
     *
     * @param netId 网络id
     */
    public void disconnectWifi(int netId) {
        InnerWifiManager.getInstance().disconnectWifi(netId);
    }

    /**
     * 获取wifi连接信息
     **/
    public WifiInfo getWifiInfo() {
        return InnerWifiManager.getInstance().getWifiInfo();
    }

    /**
     * 扫描出的wifi列表(包含已经连接过的和没有连接过的)
     */
    public List<ScanResult> getScanResults() {
        return InnerWifiManager.getInstance().getScanResults();
    }

    /**
     * 获取连接过的wifi
     */
    public List<WifiConfiguration> getConfiguredNetworks() {
        return InnerWifiManager.getInstance().getConfiguredNetworks();
    }

    /**
     * 创建一个WifiLock
     **/
    public void createWifiLock() {
        InnerWifiManager.getInstance().createWifiLock();
    }

    /**
     * 锁定WifiLock，当下载大文件时需要锁定
     **/
    public void acquireWifiLock() {
        InnerWifiManager.getInstance().acquireWifiLock();
    }

    /**
     * 解锁WifiLock
     **/
    public void releaseWifilock() {
        InnerWifiManager.getInstance().releaseWifilock();
    }

    /***
     使用wifi账号和密码连接到指定的wifi
     6.0以上版本，
     先查找是否有连接过的WifiConfiguration，
     如果有则使用连接过的wifiConfiguration.
     不要去创建新的wifiConfiguration,否者会失败
     */
    public void connectWifi(String SSID, String password, int Type) {
        InnerWifiManager.getInstance().connectWifi(SSID, password, Type);
    }

    /***
     移除wifi
     使用本App输入账号和密码进行连接的网络是可以被移除的,除此之外是移除不了的

     @param SSID wifi名
     */
    public boolean removeWifi(String SSID) {
        return InnerWifiManager.getInstance().removeWifi(SSID);
    }

    /**
     * 移除wifi，因为权限，无法移除的时候，需要手动去翻wifi列表删除
     * 注意：！！！只能移除自己应用创建的wifi。
     * 删除掉app，再安装的，都不算自己应用，具体看removeNetwork源码
     *
     * @param netId wifi的id
     */
    public boolean removeWifi(int netId) {
        return InnerWifiManager.getInstance().removeWifi(netId);
    }

}

class InnerWifiManager {

    /***
     List<ScanResult>这个结果的SSID没有双引号,
     List<WifiConfiguration>(已经连接过的)这个结果的SSID有双引号
     */

    private static InnerWifiManager sInnerWifiManager;
    private static Context mContext;

    // 无密码
    public static final int WIFI_CIPHER_NPW = 0;
    // WEP加密
    public static final int WIFI_CIPHER_WEP = 1;
    // WAP加密 capabilities: [WPA-PSK-CCMP][WPA2-PSK-CCMP][ESS]
    public static final int WIFI_CIPHER_WAP = 2;

    private WifiManager mWifiManager;
    //能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    private WifiManager.WifiLock mWifiLock;

    // first do it.
    public static void setContext(Context context) {
        mContext = context;
    }

    static InnerWifiManager getInstance() {
        if (sInnerWifiManager == null) {
            synchronized (InnerWifiManager.class) {
                if (sInnerWifiManager == null) {
                    sInnerWifiManager = new InnerWifiManager();
                }
            }
        }
        return sInnerWifiManager;
    }

    private InnerWifiManager() {
        if (mContext == null) {
            return;
        }
        //获取wifiManager对象
        mWifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 打开wifi
     */
    public void openWifi() {
        if (mWifiManager == null) {
            return;
        }
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭wifi
     */
    public void closeWifi() {
        if (mWifiManager == null) {
            return;
        }
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 搜索wifi
     */
    public void scanWifi() {
        if (mWifiManager == null) {
            return;
        }
        mWifiManager.startScan();
    }

    /***
     * 断开指定SSID的网络
     * 断开是指连接好的wifi可以把它断开达到不使用的结果.
     *
     * @param SSID wifi账号
     */
    public void disconnectWifi(String SSID) {
        if (getExitsWifiConfig(SSID) != null) {
            disconnectWifi(getExitsWifiConfig(SSID).networkId);
        }
    }

    /**
     * 断开指定ID的网络
     *
     * @param netId 网络id
     */
    public void disconnectWifi(int netId) {
        if (mWifiManager == null) {
            return;
        }
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * 获取wifi连接信息
     **/
    public WifiInfo getWifiInfo() {
        if (mWifiManager == null) {
            return null;
        }
        if (mWifiManager != null) {
            return mWifiManager.getConnectionInfo();
        }
        return null;
    }

    /**
     * 扫描出的wifi列表(包含已经连接过的和没有连接过的)
     */
    public List<ScanResult> getScanResults() {
        if (mWifiManager == null) {
            return null;
        }
        return mWifiManager.getScanResults();
    }

    /**
     * 获取连接过的wifi
     */
    public List<WifiConfiguration> getConfiguredNetworks() {
        if (mWifiManager == null) {
            return null;
        }
        // 得到配置过的网络
        return mWifiManager.getConfiguredNetworks();
    }

    /**
     * 创建一个WifiLock
     **/
    public void createWifiLock() {
        if (mWifiManager == null) {
            return;
        }
        mWifiLock = this.mWifiManager.createWifiLock("NewWifiLock");
    }

    /**
     * 锁定WifiLock，当下载大文件时需要锁定
     **/
    public void acquireWifiLock() {
        if (!mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    /**
     * 解锁WifiLock
     **/
    public void releaseWifilock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    /***
     使用wifi账号和密码连接到指定的wifi
     6.0以上版本，
     先查找是否有连接过的WifiConfiguration，
     如果有则使用连接过的wifiConfiguration.
     不要去创建新的wifiConfiguration,否者会失败
     */
    public void connectWifi(String SSID, String password, int Type) {
        if (mWifiManager == null) {
            return;
        }
        int networkId = -1;
        /***
         先执行删除wifi操作，
         1.如果删除成功则说明这个wifi配置是由本APP配置出来的；
         2.这样可以避免密码错误之后，同名字的wifi配置存在，无法连接；
         3.wifi直接连接成功过，不删除也能用, netId = getExitsWifiConfig(SSID).networkId;
         */
        if (removeWifi(SSID)) {
            // 移除成功，就新建一个
            networkId = mWifiManager.addNetwork(createWifiInfo(SSID, password, Type));
        } else {
            // 删除不成功，要么这个wifi配置以前就存在过，要么是还没连接过的
            if (getExitsWifiConfig(SSID) != null) {
                // 这个wifi是连接过的，如果这个wifi在连接之后改了密码，那就只能手动去删除了
                networkId = getExitsWifiConfig(SSID).networkId;
            } else {
                // 没连接过的，新建一个wifi配置
                networkId = mWifiManager.addNetwork(createWifiInfo(SSID, password, Type));
            }
        }

        // 第一个参数是需要连接wifi网络的networkId
        // 第二个参数是指连接当前wifi网络是否需要断开其他网络
        // 无论是否连接上，都返回true
        mWifiManager.enableNetwork(networkId, true);
    }

    /***
     移除wifi
     使用本App输入账号和密码进行连接的网络是可以被移除的,除此之外是移除不了的

     @param SSID wifi名
     */
    public boolean removeWifi(String SSID) {
        if (getExitsWifiConfig(SSID) != null) {
            return removeWifi(getExitsWifiConfig(SSID).networkId);
        } else {
            return false;
        }
    }

    /**
     * 移除wifi，因为权限，无法移除的时候，需要手动去翻wifi列表删除
     * 注意：！！！只能移除自己应用创建的wifi。
     * 删除掉app，再安装的，都不算自己应用，具体看removeNetwork源码
     *
     * @param netId wifi的id
     */
    public boolean removeWifi(int netId) {
        if (mWifiManager == null) {
            return false;
        }
        return mWifiManager.removeNetwork(netId);
    }

    /**
     * 创建一个wifiConfiguration
     *
     * @param SSID     wifi名称
     * @param password wifi密码
     * @param Type     加密类型
     * @return
     */
    private WifiConfiguration createWifiInfo(String SSID, String password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        //如果有相同配置的，就先删除
        WifiConfiguration tempConfig = getExitsWifiConfig(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
            mWifiManager.saveConfiguration();
        }

        //无密码
        if (Type == WIFI_CIPHER_NPW) {
            // config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            // config.wepTxKeyIndex = 0;
        }
        //WEP加密
        else if (Type == WIFI_CIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        //WPA加密
        else if (Type == WIFI_CIPHER_WAP) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 获取配置过的wifiConfiguration
     */
    private WifiConfiguration getExitsWifiConfig(String SSID) {
        List<WifiConfiguration> wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        if (wifiConfigurationList == null) {
            return null;
        }
        final String tempSSID = "\"" + SSID + "\"";
        for (WifiConfiguration wifiConfiguration : wifiConfigurationList) {
            if (tempSSID.equals(wifiConfiguration.SSID)) {
                return wifiConfiguration;
            }
        }
        return null;
    }

}

