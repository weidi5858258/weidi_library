package com.weidi.customadapter.othersetup;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.View;

/**
 * 设置item之间的间距,有特定需求时可以这样自定义
 */

public class SpaceItemDecoration extends ItemDecoration {

    private int space;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(
            Rect outRect,
            View view,
            RecyclerView parent,
            RecyclerView.State state) {
        // 不是第一个的格子都设一个左边和底部的间距
        //        int pos = parent.getChildAdapterPosition(view);
        //        outRect.left = 10;
        //        if (pos != 0) {
        //            if (pos % 2 == 0) {  //下面一行
        //                outRect.bottom = 30;
        //                outRect.top = 5;
        //            } else { //上面一行
        //                outRect.top = 30;
        //                outRect.bottom = 5;
        //            }
        //        }

        //                if (parent.getChildPosition(view) != 0) {
        //                    outRect.top = space;
        //                }

        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space;
        } else {
            outRect.top = 0;
        }

    }

}
