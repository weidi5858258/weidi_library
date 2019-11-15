package com.weidi.recycler_view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/***

 */

public class SimpleLayoutManager
        extends RecyclerView.LayoutManager {

    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int interval;
    private int middle;
    private int offset;
    private List<Integer> offsetList;

    public SimpleLayoutManager(Context context) {
        offsetList = new ArrayList<>();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            offset = 0;
            detachAndScrapAttachedViews(recycler);
            return;
        }

        //初始化的过程，还没有childView，先取出一个测绘。 认为每个item的大小是一样的
        if (getChildCount() == 0) {
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap);
            interval = 10;
            middle = (getVerticalSpace() - mDecoratedChildHeight) / 2;
            detachAndScrapView(scrap, recycler);
        }

        //回收全部attach 的 view 到 recycler 并重新排列
        int property = 0;
        for (int i = 0; i < getItemCount(); i++) {
            offsetList.add(property);
            property += mDecoratedChildHeight + interval;
        }
        detachAndScrapAttachedViews(recycler);
        layoutItems(recycler, state, 0);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy,
                                  RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        int willScroll = dy;
        offset += willScroll;
        if (offset < 0 || offset > offsetList.get(offsetList.size() - 1)) return 0;
        layoutItems(recycler, state, dy);
        return willScroll;
    }


    private void layoutItems(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int pos = getPosition(view);
            if (outOfRange(offsetList.get(pos) - offset)) {
                removeAndRecycleView(view, recycler);
            }
        }
        detachAndScrapAttachedViews(recycler);
        int left = 100;
        View selectedView = null;
        float maxScale = Float.MIN_VALUE;
        for (int i = 0; i < getItemCount(); i++) {
            int top = offsetList.get(i);
            if (outOfRange(top - offset)) continue;
            View scrap = recycler.getViewForPosition(i);
            measureChildWithMargins(scrap, 0, 0);
            if (dy >= 0)
                addView(scrap);
            else
                addView(scrap, 0);

            int deltaY = Math.abs(top - offset - middle);
            scrap.setScaleX(1);
            scrap.setScaleY(1);
            float scale = 1 + (mDecoratedChildHeight / (deltaY + 1));
            if (scale > maxScale) {
                maxScale = scale;
                selectedView = scrap;
            }

            layoutDecorated(scrap, left, top - offset, left + mDecoratedChildWidth, top - offset
                    + mDecoratedChildHeight);
        }

        if (selectedView != null) {
            maxScale = maxScale > 2 ? 2 : maxScale;
            selectedView.setScaleX(maxScale);
            selectedView.setScaleY(maxScale);
        }
    }

    private boolean outOfRange(float targetOffSet) {
        return targetOffSet > getVerticalSpace() + mDecoratedChildHeight ||
                targetOffSet < -mDecoratedChildHeight;
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

}
