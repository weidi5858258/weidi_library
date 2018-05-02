package com.weidi.webview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BBWebCoreClient extends WebViewClient {
    private String TAG = "BB_WEB_PACKER";
    public Context mContext;

    public BBWebCoreClient(Context context) {
        super();
        this.mContext = context;

    }

    // 页面中有链接，如果希望点击链接继续在当前browser中响应，
    // 而不是新开的系统browser中响应该链接，必须覆盖 webview的WebViewClient对象。
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //地址过滤
        Log.d(TAG, " getHost: " + Uri.parse(url).getHost());
        Log.d(TAG, " getScheme: " + Uri.parse(url).getScheme());
        Log.d(TAG, " getPath: " + Uri.parse(url).getPath());
        //		if (Uri.parse(url).getHost().equals("labs.mwrinfosecurity.com")){
        //			return true;
        //		}
        //		return false;
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                   SslError error) {
        handler.proceed();
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private WebResourceResponse doFilter(String url, String moduleName) {

        return null;
    }

    /**
     * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView,
     * java.lang.String, android.graphics.Bitmap)
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        com.weidi.webview.BBWebView bbWebView = (com.weidi.webview
                .BBWebView) view;
        bbWebView.hideErrorPage();
    }

    /**
     * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView,
     * java.lang.String)
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

    }

    /**
     * @see android.webkit.WebViewClient#onLoadResource(android.webkit.WebView,
     * java.lang.String)
     */
    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }

    /**
     * @see android.webkit.WebViewClient#onReceivedError(android.webkit.WebView,
     * int, java.lang.String, java.lang.String)
     */
    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        // mErrorView.setVisibility(View.VISIBLE);
        com.weidi.webview.BBWebView bbWebView = (com.weidi.webview.BBWebView) view;
        bbWebView.showErrorPage(null);

        super.onReceivedError(view, errorCode, description, failingUrl);

    }

}