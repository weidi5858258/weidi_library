package com.weidi.customadapter.interfaces;

import android.support.v7.widget.RecyclerView;

import com.weidi.customadapter.animation.BaseAnimation;

/**
 * Animation interface for adapter.
 * <p>
 * Created by Cheney on 16/6/28.
 */
public interface IAnimation {

    void enableLoadAnimation();

    void enableLoadAnimation(long duration, BaseAnimation animation);

    void cancelLoadAnimation();

    void setOnlyOnce(boolean onlyOnce);

    void addLoadAnimation(RecyclerView.ViewHolder holder);

}
