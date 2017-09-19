package com.weidi.customadapter;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.MovementMethod;
import android.util.SparseArray;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.weidi.customadapter.interfaces.IViewSetter;

/**


 */
public class CustomViewHolder extends RecyclerView.ViewHolder
        implements IViewSetter<CustomViewHolder> {

    private SparseArray<View> childViews = new SparseArray<View>();

    public CustomViewHolder(View itemView) {
        super(itemView);
    }

    public static CustomViewHolder get(View convertView, View itemView) {
        CustomViewHolder holder = null;
        if (convertView == null) {
            holder = new CustomViewHolder(itemView);
            convertView = itemView;
            convertView.setTag(holder);
        } else {
            holder = (CustomViewHolder) convertView.getTag();
        }
        return holder;
    }

    @Deprecated
    public <T extends View> T getView(int id) {
        return findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        View childView = childViews.get(id);
        if (childView == null) {
            childView = itemView.findViewById(id);
            if (childView != null) {
                childViews.put(id, childView);
            }
        }
        return (T) childView;
    }

    //****************************IViewSetter接口****************************//

    @Override
    public CustomViewHolder setText(int viewId, CharSequence text) {
        TextView textView = findViewById(viewId);
        textView.setText(text);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setTextColor(int viewId, int textColor) {
        TextView view = findViewById(viewId);
        view.setTextColor(textColor);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setTextColor(int viewId, ColorStateList colorStateList) {
        TextView view = findViewById(viewId);
        view.setTextColor(colorStateList);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setMovementMethod(int viewId, MovementMethod method) {
        TextView textView = findViewById(viewId);
        textView.setMovementMethod(method);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setImageResource(int viewId, int imgResId) {
        ImageView view = findViewById(viewId);
        view.setImageResource(imgResId);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = findViewById(viewId);
        view.setImageDrawable(drawable);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = findViewById(viewId);
        view.setImageBitmap(bitmap);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setImageUri(int viewId, Uri imageUri) {
        ImageView view = findViewById(viewId);
        view.setImageURI(imageUri);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setScaleType(int viewId, ImageView.ScaleType type) {
        ImageView view = findViewById(viewId);
        view.setScaleType(type);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setBackgroundColor(int viewId, int bgColor) {
        View view = findViewById(viewId);
        view.setBackgroundColor(bgColor);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setBackgroundResource(int viewId, int bgRes) {
        View view = findViewById(viewId);
        view.setBackgroundResource(bgRes);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setColorFilter(int viewId, ColorFilter colorFilter) {
        ImageView view = findViewById(viewId);
        view.setColorFilter(colorFilter);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setColorFilter(int viewId, int colorFilter) {
        ImageView view = findViewById(viewId);
        view.setColorFilter(colorFilter);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setAlpha(
            int viewId,
            @FloatRange(from = 0.0, to = 1.0) float value) {
        View view = findViewById(viewId);
        ViewCompat.setAlpha(view, value);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setVisibility(int viewId, int visibility) {
        View view = findViewById(viewId);
        view.setVisibility(visibility);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setMax(int viewId, int max) {
        ProgressBar view = findViewById(viewId);
        view.setMax(max);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setProgress(int viewId, int progress) {
        ProgressBar view = findViewById(viewId);
        view.setProgress(progress);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setRating(int viewId, float rating) {
        RatingBar view = findViewById(viewId);
        view.setRating(rating);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setTag(int viewId, Object tag) {
        View view = findViewById(viewId);
        view.setTag(tag);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setTag(int viewId, int key, Object tag) {
        View view = findViewById(viewId);
        view.setTag(key, tag);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setEnabled(int viewId, boolean enabled) {
        View view = findViewById(viewId);
        view.setEnabled(enabled);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setAdapter(int viewId, Adapter adapter) {
        AdapterView<Adapter> view = findViewById(viewId);
        view.setAdapter(adapter);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setAdapter(int viewId, RecyclerView.Adapter adapter) {
        RecyclerView view = findViewById(viewId);
        view.setAdapter(adapter);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setChecked(int viewId, boolean checked) {
        Checkable view = findViewById(viewId);
        view.setChecked(checked);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        findViewById(viewId).setOnClickListener(listener);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener
            listener) {
        findViewById(viewId).setOnLongClickListener(listener);
        return CustomViewHolder.this;
    }

    @Override
    public CustomViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
        findViewById(viewId).setOnTouchListener(listener);
        return CustomViewHolder.this;
    }

}
