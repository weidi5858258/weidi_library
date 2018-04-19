package com.xianglin.station.webview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.xianglin.mobile.common.logging.LogCatLog;
import com.xianglin.station.util.ToastUtils;

/**
 * 自定义WebChromeClient实现类
 * @author songdiyuan
 * @version $Id: BBViewClient.java, v 1.0.0 2015-8-18 下午4:48:10 xl Exp $
 */
public class BBViewClient extends WebChromeClient {
	private ValueCallback<Uri> mUploadMessage;
	private String TAG = "BB_WEB_PACKER";
	private BBViewClientCallBack mBBViewClientCallBack;
	private Context mContext;
	private boolean mIsInjectedJS;

	public BBViewClient(Context context) {
		super();
		this.mContext = context;
	}

	/**
	 * Setter method for property <tt>mBBViewClientCallBack</tt>.
	 * 
	 * @param mBBViewClientCallBack
	 *            value to be assigned to property mBBViewClientCallBack
	 */
	public void setBBViewClientCallBack(
			BBViewClientCallBack mBBViewClientCallBack) {
		this.mBBViewClientCallBack = mBBViewClientCallBack;
	}

	// 当时数据库存储空间不够时,自动扩容为原来的2倍
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public void onExceededDatabaseQuota(String url, String databaseIdentifier,
			long currentQuota, long estimatedSize, long totalUsedQuota,
			WebStorage.QuotaUpdater quotaUpdater) {
		quotaUpdater.updateQuota(estimatedSize * 2);
	}

	/**
	 * @see android.webkit.WebChromeClient#onProgressChanged(android.webkit.WebView,
	 *      int)
	 */
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		if (mBBViewClientCallBack != null) {
			mBBViewClientCallBack.onProgressChanged(newProgress);
		}

		if (newProgress == 100 && mBBViewClientCallBack != null) {
			mBBViewClientCallBack.onProgresscomplete();
		}

		// 为什么要在这里注入JS
		// 1 OnPageStarted中注入有可能全局注入不成功，导致页面脚本上所有接口任何时候都不可用
		// 2 OnPageFinished中注入，虽然最后都会全局注入成功，但是完成时间有可能太晚，当页面在初始化调用接口函数时会等待时间过长
		// 3 在进度变化时注入，刚好可以在上面两个问题中得到一个折中处理
		// 为什么是进度大于25%才进行注入，因为从测试看来只有进度大于这个数字页面才真正得到框架刷新加载，保证100%注入成功
		if (newProgress <= 25) {
			mIsInjectedJS = false;
		} else if (!mIsInjectedJS) {
			if (view instanceof BBWebCore) {
				BBWebCore bbWebCore = (BBWebCore) view;
				bbWebCore.injectJavascriptInterfaces();
			}
			// view.loadUrl(mJsCallJava.getPreloadInterfaceJS());
			mIsInjectedJS = true;
			LogCatLog.d(TAG, " inject js interface completely on progress "
					+ newProgress);
		}

		super.onProgressChanged(view, newProgress);
	}

	/**
	 * @see android.webkit.WebChromeClient#onReceivedTitle(android.webkit.WebView,
	 *      java.lang.String)
	 */
	@Override
	public void onReceivedTitle(WebView view, String title) {
		if (mBBViewClientCallBack != null) {
			mBBViewClientCallBack.onReceivedTitle(title);
		}

		super.onReceivedTitle(view, title);
	}

	/**
	 * @see android.webkit.WebChromeClient#onJsConfirm(android.webkit.WebView,
	 *      java.lang.String, java.lang.String, android.webkit.JsResult)
	 */
	@Override
	public boolean onJsConfirm(WebView view, String url, String message,
			JsResult result) {
		return super.onJsConfirm(view, url, message, result);
	}

	/**
	 * @see android.webkit.WebChromeClient#onJsPrompt(android.webkit.WebView,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      android.webkit.JsPromptResult)
	 */
	@Override
	public boolean onJsPrompt(WebView view, String url, String message,
			String defaultValue, JsPromptResult result) {
		if (view instanceof BBWebCore) {
			BBWebCore bbWebCore = (BBWebCore) view;
			if (bbWebCore.handleJsInterface(view, url, message, defaultValue,
					result)) {
				return true;
			}
		}

		return super.onJsPrompt(view, url, message, defaultValue, result);
	}

	@Override
	public boolean onJsAlert(WebView view, String url, String message,
			JsResult result) {
		LogCatLog.i(TAG, "TXTMessage: " + message);

		ToastUtils.toastForLong(mContext, message);
		result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。

		return true;

	}

	// 当缓存空间不够时,自动扩容为原来的2倍
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
			WebStorage.QuotaUpdater quotaUpdater) {
		quotaUpdater.updateQuota(spaceNeeded * 2);
	}

	// js上传文件的<input type="file" name="fileField" id="fileField" />事件捕获
	// Android > 4.1.1 调用这个方法
	public void openFileChooser(ValueCallback<Uri> uploadMsg,
			String acceptType, String capture) {
		if (mUploadMessage != null)
			return;
		mUploadMessage = uploadMsg;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		/*
		 * context.startActivityForResult( Intent.createChooser(intent,
		 * "完成操作需要使用"), WebMainActivity.FILECHOOSER_RESULTCODE);
		 */

	}

	// 3.0 + 调用这个方法
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		mUploadMessage = uploadMsg;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		/*
		 * context.startActivityForResult( Intent.createChooser(intent,
		 * "完成操作需要使用"), WebMainActivity.FILECHOOSER_RESULTCODE);
		 */
	}

	// Android < 3.0 调用这个方法
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		mUploadMessage = uploadMsg;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		/*
		 * context.startActivityForResult( Intent.createChooser(intent,
		 * "完成操作需要使用"), WebMainActivity.FILECHOOSER_RESULTCODE);
		 */
	}

	public interface BBViewClientCallBack {

		void onProgressChanged(int progress);

		void onProgresscomplete();

		void onReceivedTitle(String title);

	}

}
