package com.weidi.wifi;

/**
 * Created by root on 18-4-19.
 */

public class ReadMe {

    /***

     Android-WiFi开发之 WiFi广播监听

     一. 首先,我们需要监听当前是否有网络连接
     (或者是其他连接, 不仅限于有线网, 无线网, 当然还可能有手机3G, 4G, 蓝牙, AP等等),
     那么需要做的就是从ConnectivityManager中获取当前的连接信息了;
     (1) ConnectivityManager, 看到这个名字, 小猜一下, 就能大概知道了, 是 Android 中对于连接的管理类; 获取方式也比较简单, 可以通过当前的 Context 对象获取, 方式如下:

     ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

     (2) 获取到了 ConnectivityManager 对象之后, 我们就能获取一些关于连接的东东了;现将有线网和无线网用来例举吧:

     <1> 获取默认的连接方式, 调用getActiveNetworkInfo(); 会获取一份默认的连接方式, 返回NetworkInfo, 如果当前没有任何默认的网络连接方式, 会返回null, 调用此方法, 需要在AndroidManifest中配置android.permission.ACCESS_NETWORK_STATE的权限;代码具体为:

     NetworkInfo defaultNetwork = connectivityManager.getActiveNetworkInfo();

     <2> 接下来, 就是当前的连接状况了, 由于小弟只是纯手工编辑, 并未使用任何编辑器, 所以汉子及代码都是一个一个打上来的, 太磕碜了, 那么继续看一点吧:

     // 注意, defaultNetwork == null, 当前无连接;

     // 当 defaultNetwork != null 的时候, 如果 defaultNetwork.isConnected() && defaultNetwork.isAvailable() 说明当前已经连接了网络了, 并且已经可用;

     // 紧接着, 如果 defaultNetwork.getType() == ConnectivityManager.TYPE_ETHERNET, 说明连接的是以太网; 如果 defaultNetwork.getType() == ConnectivityManager.TYPE_WIFI, 说明连接的是无线网;

     <3> 到了上一步, 基本上获取默认连接部分已经能拿到了, 如果你想来点儿吐司神马的, 就可以到此结束了, 但是作为一个有追求的程序员, 咱们得继续往下做一些了解了;

     <4> CSDN和很多论坛上, 有关于 NetworkInfo 及 ConnectivityManager 的介绍, 也相当容易查, 此处就不做过多解释了;

     二. 通过第一步, 我们可以静态获取当前的网络连接了, 那么在开发过程中, 我们大多是需要动态刷新状态的, 那么就需要用到 Android 伟大的广播机制了.

      同样, 广播机制也是 Android 开发中很重要的一种机制, 对于动态刷新, 通知, 数据传递都存在非常大的意义, 需要深入了解的童鞋, 可以找一下这方面的资料, 还是很多的;

     那么, 说回来了, 既然要动态监听, 那么我们应该怎么做呢, 当然是动态监听系统发出的广播了;下面就把开发中你可能需要用到的广播做一小说明:

     (1) 先说两个类吧, 一个叫做 ConnectivityManager, 一个叫做 WifiManager, 这两个是关于连接的类; 其中, 关于监听网线插拔, 需要监听如下几条Action, 分别是:

     <1> ConnectivityManager.CONNECTIVITY_ACTION;

     <2> WifiManager.WIFI_STATE_CHANGE_ACTION;

     <3> WifiManager.NETWORK_STATE_CHANGED_ACTION;

     <4> WifiManager.NETWORK_IDS_CHANGED_ACTION;

     <5> WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;

     <6> WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION;

     <7> WifiManager.RSSI_CHANGED_ACTION;

     <8> WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;

     <9> WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE;

     <10> WifiManager.ACTION_PICK_WIFI_NETWORK;

     <11> android.net.wifi.WIFI_CREDENTIAL_CHANGED;

     <12> android.net.wifi.WIFI_AP_STATE_CHANGED;

     <13> android.net.wifi.CONFIGURED_NETWORKS_CHANGE;

     <14> android.net.wifi.BATCHED_RESULTS;

     <15> android.net.wifi.LINK_CONFIGURATION_CHANGED;

     (2) 那么, 需要监听的Action, 最多可能就这么多了, 接下来, 就是你需要搞清楚在哪种场景下需要配哪些 Action 了. 那就针对上面的十条 Action 做个逐一的小说明, 根据使用场景自行添加咯;

     <1> 网络连接发生了变化的监听; 通常是默认的连接类型已经建立连接或者已经失去连接会触发的广播; 对于连接失败, 你从 intent 中取出的 ConnectivityManager.EXTRA_NO_CONNECTIVITY 为 true; 对于连接成功, 你从 intent 中可以取出 ConnectivityManager.EXTRA_NETWORK_INFO, 返回NetWorkInfo, 通过此对象, 你会获取当前连接成功的一些更为实际的东西, 那你说, 我就想知道是有线网, 无线网, 那, 不是就更简单咯? 

     boolean resultNoConnecty = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

     NetworkInfo info = intent.getPacelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

     info.getState(), 可获取当前NetworkInfo 的 status 信息;

     info.getType(), 返回当前NetworkInfo 的 连接类型, WiFi, 蓝牙, 以太网 ... 

     <2> WiFi模块硬件状态改变的广播, 肉眼看到的直观表征有, WiFi开启, WiFi关闭; 当然了, 作为一名Android 开发程序猿, 你要知道, WiFi 模块不止这两种状态. 这个广播发出后, 你可以从intent中取出当前WiFi硬件的变化状态, 可以使用 int 值来区别; 这个key是: EXTRA_WIFI_STATE, 可能得到的值为:0, 1, 2, 3, 4; 当然除了这种获取方式, 也可以通过WiFiManager对象getWifiState() 获取这个值;

     int currentWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

     或是: 

     int currentWifiState = wifiManager.getState();

     其中, 0对应的是WiFiManager.WIFI_STATE_DISABLING, 表示 WiFi 正关闭的瞬间状态; 

     1对应的是 WifiManager.WIFI_STATE_DISABLED, 表示 WiFi 模块已经完全关闭的状态; 

     2对应的是 WifiManager.WIFI_STATE_ENABLING, 表示 WiFi 模块正在打开中瞬间的状态; 

     3对应的是 WiFiManager.WIFI_STATE_ENABLED, 表示 WiFi 模块已经完全开启的状态;

     4对应的是 WiFiManager.WIFI_STATE_UNKNOWN, 表示 WiFi 处于一种未知状态; 通常是在开启或关闭WiFi的过程中出现不可预知的错误, 与硬件厂商有关的; 一般是遇不到这种情况的, 如果开发中你遇到了, 那么恭喜你可以休息10分钟, 然后去换一台新机器了;

     此广播发出后, 通常, 如果你的 WiFi 模块可以工作, 你可以顺手从 intent 中取出另外一个值, 表示之前WiFi模块的状态, 是不是很爽? 那么, 对应的key, 就是: EXTRA_PREVIOUS_WIFI_STATE;

     int previousWifiState = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, 0);

     <3> WiFi 连接的时候, 连接改变触发的广播; 通常你可以从 NetworkInfo 中获取一个新的状态, 如果这个新的状态是CONNECTED, 那么, 你就可以拿到当前的 WiFiInfo 和 BSSID 了; 获取这三个值对应的 key 分别是: EXTRA_NETWORK_INFO, EXTRA_BSSID, EXTRA_WIFI_INFO;

     NetworkInfo networkInfo = intent.getPacelableExtra(WifiManager.EXTRA_NETWORK_INFO);

     NetworkInfo.State state = networkInfo.getState();

     state, 一个枚举类, 有6个值, 分别是: CONNECTING, CONNECTED, SUSPENED, DISCONNECTING, DISCONNECTED, UNKNOWN;

     WifiInfo wifiInfo = intent.getPacelableExtra(WifiManager.EXTRA_WIFI_INFO);

     String bSSid = intent.getStringExtra(WifiManager.EXTRA_BSSID, "android_wifi_connection");

     <4> 对于已经配置好的 NetWork 而言, 其 IDs 发生改变触发的广播; 配置好的, 一般指的是你连接WiFi时, 会形成一个类似 configuration 的东西存入; 一般连接过网络的人都是知道有忘记密码这个功能的对吧? 

     <5> 对于已经建立好连接的连接, 其状态发生改变所触发的广播; 对于你想鉴定当前的网络状态, 这个广播简直是福音了; 那么同样, 有兄弟要问了, 你怎么鉴定当前Network是什么状态呢? 别慌, 有办法;可以根据intent来取值, 取值方式如下:

     SupplicantState netNewState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

     其中, SupplicantState是一个枚举类, 里面包含的值, 相对于咱们而言, 比较关心的就两个, 一个是DISCONNECTED, 一个是COMPLETED;

     同时, 如果当前网络连接出错, 也可获取出错描述状态码;

     int netConnectErrorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, WifiManager.ERROR_AUTHENTICATING);

     <6> 对于已经发生SupplicantState改变的网络已经完全连接或者已经完全失去连接所触发的广播; 与此同时, 你可以从intent中获取一个boolean值, 这个值若为true, 表示当前已成功连接, 反之, 则是断开了连接;

     boolean connectResult = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);

     <7> 网络信号强度发生改变所触发的广播; 可以获取到最新的rssi信息;

     int currentRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -55);

     <8> 扫描到一个热点, 并且此热点达可用状态 会触发此广播; 此时, 你可以通过wifiManager.getScanResult() 来取出当前所扫描到的ScanResult; 同时, 你可以从intent中取出一个boolean值; 如果此值为true, 代表着扫描热点已完全成功; 为false, 代表此次扫描不成功, ScanResult 距离上次扫描并未得到更新; (作为程序员菜菜的我, 总会对Boolean额外钟情三分)

     List<ScanResult> results = wifiManager.getScanResult();

     boolean updateScanResult = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED);

     <9> 这个广播意义非凡啊. 相信大家有过这样的经历, 当你关闭WiFi瞬间, 可能系统会给你弹出来一个Notification, 说当前有可用网络, 没错, 就是这个广播搞得了; 究竟他有什么样的用处呢, 来, 细细品味. 官方文档是这么描述的, 当这个广播触发, 如果当前WiFi是开启的, 直接可以开始扫描网络, 如果你的WiFi是关闭的, 你也可以打开, 然后扫描网络; 那, 你的activity会在onActivityResult里面, 获取当前的resultCode, 如果当前的resultCode = -1, 那说明一切正常, 你的WiFi处于实时可扫描状态; 如果resultCode = 0, 说明当前发生了某些中断性异常, 或是你拒绝了某些操作;

     <10> 您随意拾起一个WiFi就开始连接了, 那么此广播便开始触发了;

     <11> 有WiFi证书发生改变所触发的广播; 此广播发出的时候, 你可以获取当前证书改变的 Network 的 ssid, 以及当前改变 Network 是保存还是忘记;

     String currentCredentialChangeSsid = intent.getStringExtra("ssid", "android_net_connection");

     int currentCredentialChangeEventType = intent.getIntExtra("et", 0);

     如果currentCredentialChangeEventType == 0, 说明是当前证书得到保存; 如果currentCredentialChangeEventType == 1, 说明当前证书已忘记, 失效;

     <12> WiFi - AP 发生改变所触发的广播, 基本是用不着的; 如果你用的着, 那你要找我吗? 

     <13> 当前 Network 的配置发生改变所触发的广播. 我要分解一下: 

     a. 你会拿到一个boolean值, 什么意思呢? 先卖个关子:

     boolean multipleChanges = intent.getBooleanExtra("multipleChanges", false);

     b. 你会拿到这个以这个值, 叫做WifiConfiguration;

     WifiConfiguration config = intent.getPacelableExtra("wifiConfiguration");

     那么, multipleChanges 代表的是, 如果当前有 Network 改变, multipleChanges 就会为true; 如果此时此Network有多重变化, 那么可能造成config\不准确;

     c. 与此同时, 可以从 intent 中取出另一个值, 他是 int 类型的, 代表 config 发生改变的原因;

     int configChangeReason = intent.getIntExtra("changeReason", 0);

     如果 configChangeReason == 0, 代表添加config是新的, 并且是添加的形式; 如果 configChangeReason == 1, 代表当前 config 已移除, 并且不再是 已配置好的 WiFiConfiguration 列表中的一员了, 在系统配置文件中已经除名了; 如果 configChangeReason ==2, 如果 configuration 已经有明确行为或是系统自动给出一个行为, 比如正在终止对于一个发生故障的配置的 Network 的连接.

     <14> 一批热点扫描完毕, 并且这些热点可用时, 会触发此广播. 你原可以使用wifiManager.getBatchedResult() 来取出这些值, 但是令你蛋碎的是, 这个函数被@hide了, 没关系, 你可以反射嘛!!! 但是令你蛋疼的事又来了, 你反射得到的结果是一个List<BatchScanResult>, 而 BatchScanResult 这个类都被@hide了.....

     <15> WiFi链接配置发生改变触发的广播, 这个广播, 暂时没精力验证, 猜想的是, 路由配置发生改变触发; 会有两个参数给你获取:

     LinkProperties linkProperties = intent.getParcelableExtra("linkProperties");

     NetworkCapabilities capabilities = intent.getParcelableExtra("networkCapabilities");

     一般用不到监听到这个层面;

     三. 目前广播阶段已大致更新完成, 仅供参考, 如有不解, 可进安卓官方文档查阅, 也可在下方列出质疑点, 3Q!

     作者：迷你小猪
     链接：https://www.jianshu.com/p/c0472d7b537c
     來源：简书
     著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

    */
}
