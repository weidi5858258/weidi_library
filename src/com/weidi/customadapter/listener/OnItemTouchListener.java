package com.weidi.customadapter.listener;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by root on 16-12-27.
 */

public interface OnItemTouchListener {

    boolean onItemTouch(View itemView, int viewType, int position, MotionEvent event);

}
