package com.weidi.customadapter.listener;

import android.view.View;

/**
 * OnItemLongClickListener for RecyclerView.
 * <p>
 * Created by Cheney on 16/2/24.
 */
public interface OnItemLongClickListener {

    boolean onItemLongClick(View itemView, int viewType, int position);

}
