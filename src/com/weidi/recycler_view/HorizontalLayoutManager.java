package com.weidi.recycler_view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.log.MLog;

/***
 只支持itemView的大小都一样的情况
 然后刚开始就把所有的View都layout出来了
 */

public class HorizontalLayoutManager extends LayoutManager {

    private static final String TAG = "alexander HorizontalLayoutManager";

    // RecyclerView.HORIZONTAL = 0
    // RecyclerView.VERTICAL   = 1
    private int mOrientation;

    // 两个itemView之间的间距
    private int mItemSpace = 16;
    // 所有itemView的总宽度.如果存在mItemSpace不为0的话,再加上(getItemCount()-1)个这样的高度
    private int mAllItemsTotalWidth;
    // 这是一个累积的结果,比如不断从左往右滑动,这个过程滑动了多少距离
    private int mScrollHorizontallyOffset = 0;
    // RecyclerView的可用高度
    private int mRvUsableWidth = 0;
    // 手指允许滑动的距离.
    private int mAllowScrollHorizontallyOffset = 0;
    // 保存每个itemView四个点的坐标
    private SparseArray<Rect> mAllItemsRect = new SparseArray<Rect>();

    public HorizontalLayoutManager() {
        mItemSpace = 36;
        mOrientation = RecyclerView.HORIZONTAL;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        // 作用于itemView
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /***
     摆放子布局
     item数量少时可能只调用两次
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 父类没干活(没有意义的代码不用让它执行)
        // super.onLayoutChildren(recycler, state);

        MLog.d(TAG, "onLayoutChildren() start");
        onLayoutChildrenImpl(recycler, state);
        MLog.d(TAG, "onLayoutChildren() end");
    }

    @Override
    public boolean canScrollVertically() {
        // return super.canScrollHorizontally();
        return mOrientation == RecyclerView.VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        // return super.canScrollHorizontally();
        return mOrientation == RecyclerView.HORIZONTAL;
    }

    @Override
    public int scrollVerticallyBy(int dy,
                                  RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        return scrollVerticallyByImpl(dy, recycler, state);
    }

    @Override
    public int scrollHorizontallyBy(int dx,
                                    RecyclerView.Recycler recycler,
                                    RecyclerView.State state) {
        return scrollHorizontallyByImpl(dx, recycler, state);
    }

    public void setItemSpace(int itemSpace) {
        mItemSpace = itemSpace;
    }

    /////////////////////////////////////////////////////////////////////

    private void onLayoutChildrenImpl(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // state.getItemCount() = getItemCount()
        MLog.d(TAG, "onLayoutChildrenImpl()" +
                " getItemCount: " + getItemCount() +
                " getChildCount: " + getChildCount());
        // RecyclerView的可见宽高
        MLog.d(TAG, "onLayoutChildrenImpl() width: " + getWidth() + " height: " + getHeight());

        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            MLog.d(TAG, "onLayoutChildrenImpl() detachAndScrapAttachedViews return");
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {
            // state.isPreLayout()是支持动画的
            MLog.d(TAG, "onLayoutChildrenImpl() return");
            return;
        }

        /***
         先把所有的View先从RecyclerView中detach掉,然后标记为"Scrap"状态,
         表示这些View处于可被重用状态(非显示中)
         */
        detachAndScrapAttachedViews(recycler);

        int widthOffset = 0;
        int itemCount = getItemCount();
        // 针对每一个itemView进行布局
        for (int i = 0; i < itemCount; i++) {
            // 下面三名代码为固定步骤
            View itemView = recycler.getViewForPosition(i);
            // 因为进行了detach操作,所以现在要重新添加
            addView(itemView);
            // 通知测量itemView
            measureChildWithMargins(itemView, 0, 0);

            int itemWidth = getDecoratedMeasuredWidth(itemView);
            int itemHeight = getDecoratedMeasuredHeight(itemView);
            /*MLog.d(TAG, "onLayoutChildren() itemWidth: " + itemWidth +
                    " itemHeight: " + itemHeight);*/

            Rect itemViewRect = mAllItemsRect.get(i);
            if (itemViewRect == null) {
                itemViewRect = new Rect();
            }
            itemViewRect.set(widthOffset, 0, widthOffset + itemWidth, itemHeight);
            mAllItemsRect.put(i, itemViewRect);
            layoutDecorated(itemView,
                    itemViewRect.left, itemViewRect.top, itemViewRect.right, itemViewRect.bottom);

            if (itemCount > 1 && i < itemCount - 1) {
                widthOffset += itemWidth + mItemSpace;
            } else {
                widthOffset += itemWidth;
            }
        }

        mAllItemsTotalWidth = widthOffset;
        mRvUsableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        mAllowScrollHorizontallyOffset = mAllItemsTotalWidth - mRvUsableWidth;
        MLog.d(TAG, "onLayoutChildrenImpl() mAllItemsTotalWidth: " + mAllItemsTotalWidth +
                " mRvUsableWidth: " + mRvUsableWidth +
                " mAllowScrollHorizontallyOffset: " + mAllowScrollHorizontallyOffset +
                " getPaddingStart: " + getPaddingStart() +
                " getPaddingEnd: " + getPaddingEnd() +
                " getPaddingLeft: " + getPaddingLeft() +
                " getPaddingTop: " + getPaddingTop() +
                " getPaddingRight: " + getPaddingRight() +
                " getPaddingBottom: " + getPaddingBottom());
    }

    /***
     如果没有边界限制的话,可以进行无限滑动,也就是能够把所有的itemView移出手机屏幕十万八千里
     */
    private int scrollVerticallyByImpl(int dy,
                                       RecyclerView.Recycler recycler,
                                       RecyclerView.State state) {
        // MLog.d(TAG, "scrollVerticallyByImpl() dy: " + dy);
        // super.scrollVerticallyBy(dy, recycler, state);
        return 0;
    }

    private int scrollHorizontallyByImpl(int dx,
                                         RecyclerView.Recycler recycler,
                                         RecyclerView.State state) {
        // MLog.d(TAG, "scrollHorizontallyByImpl() dx: " + dx);
        // super.scrollHorizontallyBy(dx, recycler, state);

        if (mScrollHorizontallyOffset + dx < 0) {
            // MLog.d(TAG, "scrollHorizontallyByImpl() 抵达上边界");
            dx = -mScrollHorizontallyOffset;
        } else if (mAllItemsTotalWidth > mRvUsableWidth
                && mScrollHorizontallyOffset + dx > mAllowScrollHorizontallyOffset) {
            // MLog.d(TAG, "scrollHorizontallyByImpl() 抵达下边界");
            dx = mAllowScrollHorizontallyOffset - mScrollHorizontallyOffset;
        } else {
            // 手指从右往左滑动时,dx为正,列表内容不断显示右边部分,手指越往左滑动,列表内容越接近RecyclerView的右边界
            // 手指从左往右滑动时,dx为负,列表内容不断显示左边部分,手指越往右滑动,列表内容越接近RecyclerView的左边界
        }

        mScrollHorizontallyOffset += dx;
        /*MLog.d(TAG, "scrollHorizontallyByImpl() dx: " + dx +
                " mScrollHorizontallyOffset: " + mScrollHorizontallyOffset);*/

        // 平移容器内的itemView
        offsetChildrenHorizontal(-dx);

        // recycle view
        handleRecycle(recycler, state);

        return dx;
    }

    private void handleRecycle(RecyclerView.Recycler recycler,
                               RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        // 可见范围的一个坐标
        Rect visibleRect = new Rect(
                mScrollHorizontallyOffset,
                0,
                mScrollHorizontallyOffset + getWidth(),
                getHeight());

        // 将滑出屏幕的view进行回收
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            // 某个坐标与可见范围的坐标如果没有交叉点,那么remove
            if (!Rect.intersects(visibleRect, mAllItemsRect.get(i))) {
                // removeAndRecycleView(getChildAt(i), recycler);
                removeAndRecycleViewAt(i, recycler);
                // MLog.d(TAG, "handleRecycle() removeView i: " + i);
            }
        }

        // 在可见区域出现的ItemView重新进行layout
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            Rect itemViewRect = mAllItemsRect.get(i);
            // 某个坐标与可见范围的坐标如果有交叉点,那么add
            if (Rect.intersects(visibleRect, itemViewRect)) {
                // MLog.d(TAG, "handleRecycle() addView i: " + i);
                View scrap = recycler.getViewForPosition(i);
                addView(scrap);
                measureChildWithMargins(scrap, 0, 0);

                layoutDecorated(scrap,
                        itemViewRect.left - mScrollHorizontallyOffset,
                        itemViewRect.top,
                        itemViewRect.right - mScrollHorizontallyOffset,
                        itemViewRect.bottom);
            }
        }
    }

}
