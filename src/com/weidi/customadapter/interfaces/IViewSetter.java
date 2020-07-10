package com.weidi.customadapter.interfaces;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.method.MovementMethod;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;

import androidx.annotation.FloatRange;

/**
 * 由CustomViewHolder对象进行调用
 * 在自定义的Adapter中的onBind()方法中调用这些方法
 */
public interface IViewSetter<VH> {

    VH setText(int viewId, CharSequence text);

    VH setTextColor(int viewId, int textColor);

    VH setTextColor(int viewId, ColorStateList colorStateList);

    VH setMovementMethod(int viewId, MovementMethod method);

    VH setImageResource(int viewId, int imgResId);

    VH setImageDrawable(int viewId, Drawable drawable);

    VH setImageBitmap(int viewId, Bitmap bitmap);

    VH setImageUri(int viewId, Uri imageUri);

    VH setScaleType(int viewId, ImageView.ScaleType type);

    VH setBackgroundColor(int viewId, int bgColor);

    VH setBackgroundResource(int viewId, int bgRes);

    VH setColorFilter(int viewId, ColorFilter colorFilter);

    VH setColorFilter(int viewId, int colorFilter);

    VH setAlpha(int viewId, @FloatRange(from = 0.0, to = 1.0) float value);

    VH setVisibility(int viewId, int visibility);

    VH setMax(int viewId, int max);

    VH setProgress(int viewId, int progress);

    VH setRating(int viewId, float rating);

    VH setTag(int viewId, Object tag);

    VH setTag(int viewId, int key, Object tag);

    VH setAdapter(int viewId, Adapter adapter);

    VH setAdapter(int viewId, RecyclerView.Adapter adapter);

    VH setEnabled(int viewId, boolean enabled);

    VH setChecked(int viewId, boolean checked);

    // 对Item里面的某个控件设置点击事件
    VH setOnClickListener(int viewId, View.OnClickListener listener);

    VH setOnLongClickListener(int viewId, View.OnLongClickListener listener);

    VH setOnTouchListener(int viewId, View.OnTouchListener listener);

}
