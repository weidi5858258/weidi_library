package com.weidi.recycler_view;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.log.MLog;

import java.util.ArrayList;

/***
 进化版(缩放,中间的itemView被放大)
 */

public class Horizontal2LayoutManager extends LayoutManager {

    private static final String TAG = "alexander Horizontal2LayoutManager";

    private RecyclerView mRecyclerView;
    private RecyclerView.Recycler mRecycler;
    private RecyclerView.State mState;

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

    private static final float mNormalScaleValue = 1.0f;
    private float mScaleValue1 = 1.3f;
    private float mScaleValue2 = 1.2f;

    // 存放可见View的position,有了这个集合,就能马上知道第一个和最后一个可见View的position
    private ArrayList<Integer> mItemsVisiblePositionList = new ArrayList<Integer>();

    public Horizontal2LayoutManager() {
        mItemSpace = 16;
        mOrientation = RecyclerView.HORIZONTAL;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        MLog.d(TAG, "generateDefaultLayoutParams() getWidth: " + getWidth());
        if (getWidth() == 0) {
            // 作用于itemView
            return new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            int itemWidth = (int) (getWidth() / 5);
            return new RecyclerView.LayoutParams(
                    itemWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /***
     摆放子布局
     item数量少时可能只调用两次
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 父类没干活(没有意义的代码不用让它执行)
        // super.onLayoutChildren(recycler, state);

        mRecycler = recycler;
        mState = state;

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

    // 必须要设进来
    public void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    public void setItemSpace(int itemSpace) {
        mItemSpace = itemSpace;
    }

    public int getFirstVisiblePosition() {
        if (mItemsVisiblePositionList.isEmpty()) {
            return -1;
        }
        return mItemsVisiblePositionList.get(0);
    }

    public int getLastVisiblePosition() {
        if (mItemsVisiblePositionList.isEmpty()) {
            return -1;
        }
        return mItemsVisiblePositionList.get(mItemsVisiblePositionList.size() - 1);
    }

    private View mMaxScaleView;
    private int mMaxScalePosition = -1;

    // 放大的View就是选中状态
    public boolean isSelected(int position) {
        return mMaxScalePosition == position;
    }

    public void onItemClick(int position) {
        if (mMaxScaleView == null
                || mRecyclerView == null
                || mMaxScalePosition == position
                || position < 0
                || position >= getItemCount()) {
            return;
        }

        View view = findViewByPosition(position);
        if (view == null) {
            MLog.d(TAG, "onItemClick() view == null");
            return;
        }

        MLog.d(TAG, "onItemClick() start");
        MLog.d(TAG, "onItemClick() mMaxScalePosition: " + mMaxScalePosition +
                " position: " + position);

        mMaxScaleView.setScaleX(mNormalScaleValue);
        mMaxScaleView.setScaleY(mNormalScaleValue);
        view.setScaleX(mScaleValue2);
        view.setScaleY(mScaleValue2);
        mMaxScaleView = view;
        mMaxScalePosition = position;
        MLog.d(TAG, "onItemClick() mMaxScalePosition: " + mMaxScalePosition);

        // 移动放大的View到中间位置
        Rect itemViewRect = mAllItemsRect.get(position);
        int viewMiddlePosition = itemViewRect.left + (int) (view.getWidth() / 2);
        int screenMiddlePosition = mScrollHorizontallyOffset + (int) (getWidth() / 2);
        MLog.d(TAG, "onItemClick() viewMiddlePosition: " + viewMiddlePosition +
                " screenMiddlePosition: " + screenMiddlePosition);
        if (viewMiddlePosition != screenMiddlePosition) {
            mRecyclerView.scrollBy((viewMiddlePosition - screenMiddlePosition), 0);
        }

        MLog.d(TAG, "onItemClick() end");
    }

    public void scrollToPosition(int position) {

    }

    public void smoothScrollToPosition(RecyclerView recyclerView,
                                       RecyclerView.State state,
                                       int position) {

    }

    /////////////////////////////////////////////////////////////////////

    private void onLayoutChildrenImpl(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // state.getItemCount() = getItemCount()
        MLog.d(TAG, "onLayoutChildrenImpl()" +
                " getItemCount: " + getItemCount() +
                " getChildCount: " + getChildCount());
        // RecyclerView的可见宽高
        MLog.d(TAG, "onLayoutChildrenImpl() width: " + getWidth() + " height: " + getHeight());

        if (state.getItemCount() == 0 ||
                (getChildCount() == 0 && state.isPreLayout())) {
            MLog.d(TAG, "onLayoutChildrenImpl() removeAndRecycleAllViews return");
            removeAndRecycleAllViews(recycler);
            return;
        }

        /***
         先把所有的View先从RecyclerView中detach掉,然后标记为"Scrap"状态,
         表示这些View处于可被重用状态(非显示中)
         */
        detachAndScrapAttachedViews(recycler);

        mItemsVisiblePositionList.clear();
        Rect visibleRect = new Rect(0, 450, getWidth(), 450 + getHeight());

        View indexView = null;
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

            // 为了能够滚动到第0个位置
            if (widthOffset == 0) {
                widthOffset = getWidth() / 2 - itemWidth / 2;
            }

            Rect itemViewRect = mAllItemsRect.get(i);
            if (itemViewRect == null) {
                itemViewRect = new Rect();
            }
            itemViewRect.set(widthOffset, 450, widthOffset + itemWidth, 450 + itemHeight);
            mAllItemsRect.put(i, itemViewRect);
            // itemView在可见范围内
            if (Rect.intersects(visibleRect, itemViewRect)) {
                layoutDecorated(itemView,
                        itemViewRect.left, itemViewRect.top,
                        itemViewRect.right, itemViewRect.bottom);
                mItemsVisiblePositionList.add(i);
                MLog.d(TAG, "onLayoutChildrenImpl() i: " + i);
            }

            if (itemViewRect.left < (int) (getWidth() / 2)
                    && itemViewRect.right > (int) (getWidth() / 2)) {
                indexView = itemView;
                mMaxScaleView = itemView;
                mMaxScalePosition = i;

                itemView.setScaleX(mScaleValue2);
                itemView.setScaleY(mScaleValue2);
            }

            if (itemCount > 1 && i < itemCount - 1) {
                widthOffset += itemWidth + mItemSpace;
            } else {
                widthOffset += itemWidth;
            }

            // 为了能够滚动到第itemCount - 1个位置
            if (i == itemCount - 1) {
                widthOffset += getWidth() / 2 - itemWidth / 2;
            }
        }

        if (indexView != null) {
            removeView(indexView);
            addView(indexView);
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

        /*MLog.d(TAG, "onLayoutChildrenImpl()=================================");
        for (Integer position : mItemsVisiblePositionList) {
            MLog.d(TAG, "onLayoutChildrenImpl() position: " + position);
        }
        MLog.d(TAG, "onLayoutChildrenImpl()---------------------------------");*/
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
        handleRecycle(dx, recycler, state);

        return dx;
    }

    private void handleRecycle(int dx,
                               RecyclerView.Recycler recycler,
                               RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        // 可见范围的一个坐标
        Rect visibleRect = new Rect(
                mScrollHorizontallyOffset,
                450,
                mScrollHorizontallyOffset + getWidth(),
                450 + getHeight());

        // 将滑出屏幕的view进行回收
        /*int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            // 某个坐标与可见范围的坐标如果没有交叉点,那么remove
            if (!Rect.intersects(visibleRect, mAllItemsRect.get(i))) {
                // removeAndRecycleView(getChildAt(i), recycler);
                removeAndRecycleViewAt(i, recycler);
                MLog.d(TAG, "handleRecycle() removeView i: " + i);
            }
        }*/

        mItemsVisiblePositionList.clear();
        View selectedView = null;
        // 在可见区域出现的ItemView重新进行layout
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            View scrapView = recycler.getViewForPosition(i);
            Rect itemViewRect = mAllItemsRect.get(i);
            if (!Rect.intersects(visibleRect, itemViewRect)) {
                // 不能调用,发生空指针
                // removeAndRecycleViewAt(i, recycler);
                removeAndRecycleView(scrapView, recycler);
            } else {
                // 某个坐标与可见范围的坐标如果有交叉点,那么add
                if (itemViewRect.left - mItemSpace <=
                        mScrollHorizontallyOffset + (int) (getWidth() / 2)
                        && itemViewRect.right + mItemSpace >=
                        mScrollHorizontallyOffset + (int) (getWidth() / 2)) {
                    addView(scrapView);
                    selectedView = scrapView;
                    mMaxScaleView = scrapView;
                    mMaxScalePosition = i;
                } else {
                    addView(scrapView, 0);
                }
                measureChildWithMargins(scrapView, 0, 0);

                scrapView.setScaleX(mNormalScaleValue);
                scrapView.setScaleY(mNormalScaleValue);

                layoutDecorated(scrapView,
                        itemViewRect.left - mScrollHorizontallyOffset,
                        itemViewRect.top,
                        itemViewRect.right - mScrollHorizontallyOffset,
                        itemViewRect.bottom);

                mItemsVisiblePositionList.add(i);
            }
        }

        if (selectedView != null) {
            selectedView.setScaleX(mScaleValue2);
            selectedView.setScaleY(mScaleValue2);

            // 希望selectedView在RecyclerView的正中间
            mUiHandler.removeMessages(1);
            mUiHandler.sendEmptyMessageDelayed(1, 100);
        }

        /*MLog.d(TAG, "handleRecycle()=================================");
        for (Integer position : mItemsVisiblePositionList) {
            MLog.d(TAG, "handleRecycle() position: " + position);
        }
        MLog.d(TAG, "handleRecycle()---------------------------------");*/
    }

    private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (mRecyclerView == null
                    || mMaxScaleView == null
                    || mMaxScalePosition == -1) {
                return;
            }

            Rect itemViewRect = mAllItemsRect.get(mMaxScalePosition);
            int viewMiddlePosition = itemViewRect.left + (int) (mMaxScaleView.getWidth() / 2);
            int screenMiddlePosition = mScrollHorizontallyOffset + (int) (getWidth() / 2);
            if (viewMiddlePosition != screenMiddlePosition) {
                mRecyclerView.scrollBy((viewMiddlePosition - screenMiddlePosition), 0);
            }
        }
    };

}
