package com.weidi.recycler_view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.log.MLog;

/***
 以后自定义LayoutManager时,按照这个模板写
 */

public class AlexanderLayoutManager extends LayoutManager {

    private static final String TAG = "alexander AlexanderLayoutManager";

    // 两个itemView之间的间距
    private int mItemSpace = 16;
    // 所有itemView的总高度.如果存在mItemSpace不为0的话,再加上(getItemCount()-1)个这样的高度
    private int mItemsTotalHeight;
    // 这是一个累积的结果,比如不断从下往上滑动,这个过程滑动了多少距离
    private int mScrollVerticallyOffset = 0;
    // RecyclerView的可用高度
    private int mRvUsableHeight = 0;
    // 手指能够滑动的距离.
    // 比方说mItemsTotalHeight总高度1280像素,在屏幕中已经显示了280像素,那么用户还能够滚动1000像素就到达底部了
    private int mCouldScrollVerticallyOffset = 0;

    public AlexanderLayoutManager() {
        mItemSpace = 0;
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
        return true;
    }

    @Override
    public boolean canScrollHorizontally() {
        return super.canScrollHorizontally();
        // return true;
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
        // 第一次被调用时196 196 0
        // 第二次被调用时196 196 196
        // 第三次被调用时196 196 196
        // 第四次被调用时196 196 6(界面完全呈现数据了)
        MLog.d(TAG, "onLayoutChildrenImpl() itemCount: " + state.getItemCount() +
                " " + getItemCount() +
                " " + getChildCount());
        // 第一次被调用时width: 0    height: 0
        // 第二次被调用时width: 850  height: 0
        // 第三次被调用时width: 1189 height: 582
        // 第四次被调用时width: 1189 height: 582
        MLog.d(TAG, "onLayoutChildrenImpl() width: " + getWidth() + " height: " + getHeight());

        if (getItemCount() <= 0 || state.isPreLayout()) {
            MLog.d(TAG, "onLayoutChildrenImpl() removeAndRecycleAllViews return");
            removeAndRecycleAllViews(recycler);
            return;
        }

        /***
         先把所有的View先从RecyclerView中detach掉,然后标记为"Scrap"状态,
         表示这些View处于可被重用状态(非显示中)
         */
        detachAndScrapAttachedViews(recycler);

        int offsetY = 0;
        int itemCount = getItemCount();
        // 针对每一个itemView进行布局
        for (int i = 0; i < itemCount; i++) {
            // 下面三名代码为固定步骤
            View itemView = recycler.getViewForPosition(i);
            // 因为进行了detach操作,所以现在要重新添加
            addView(itemView);
            // 通知测量itemView
            measureChildWithMargins(itemView, 0, 0);

            // 得到itemView的宽高
            int itemWidth = getDecoratedMeasuredWidth(itemView);
            // 一般在layout中会设置一定的高度
            int itemHeight = getDecoratedMeasuredHeight(itemView);
            /*MLog.d(TAG, "onLayoutChildren() itemWidth: " + itemWidth +
                    " itemHeight: " + itemHeight);*/
            /***
             每一个itemView针对RecyclerView的可用宽高进行设置
             left  : 当前itemView距离RecyclerView可用宽度的左边多少距离
             top   :
             right : 当前itemView距离RecyclerView可用宽度的右边多少距离
             bottom:
             left与right设置不好的话,itemView中某些内容可能显示不了
             */
            int left = 100;
            int top = offsetY;
            int right = itemWidth - 100;
            int bottom = offsetY + itemHeight;
            // 布局
            // layoutDecorated(itemView, left, top, right, bottom);
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            // 旋转itemView
            // itemView.setRotation(45f);
            if (itemCount > 1) {
                offsetY += itemHeight + mItemSpace;
            } else {
                // 只有一条内容时itemView下面不要有间距了,这样不好看
                offsetY += itemHeight;
            }
        }

        mItemsTotalHeight = offsetY;
        mRvUsableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        mCouldScrollVerticallyOffset = mItemsTotalHeight - mRvUsableHeight;
        MLog.d(TAG, "onLayoutChildrenImpl() getPaddingTop: " + getPaddingTop() +
                " getPaddingBottom:" + getPaddingBottom());
        MLog.d(TAG, "onLayoutChildrenImpl() mItemsTotalHeight: " + mItemsTotalHeight);
    }

    /***
     手指从下往上滑动时,dy为正,列表内容不断显示下面部分
     手指从上往下滑动时,dy为负,列表内容不断显示上面部分
     如果没有边界限制的话,可以进行无限滑动,也就是能够把所有的itemView移出手机屏幕十万八千里
     */
    private int scrollVerticallyByImpl(int dy,
                                       RecyclerView.Recycler recycler,
                                       RecyclerView.State state) {
        // MLog.d(TAG, "scrollVerticallyByImpl() dy: " + dy);
        // super.scrollVerticallyBy(dy, recycler, state);

        if (mScrollVerticallyOffset + dy < 0) {
            // MLog.d(TAG, "scrollVerticallyByImpl() 抵达上边界");
            dy = -mScrollVerticallyOffset;
        } else if (mItemsTotalHeight > mRvUsableHeight
                && mScrollVerticallyOffset + dy > mCouldScrollVerticallyOffset) {
            // MLog.d(TAG, "scrollVerticallyByImpl() 抵达下边界");
            dy = mCouldScrollVerticallyOffset - mScrollVerticallyOffset;
        } else {

        }

        mScrollVerticallyOffset += dy;

        // 平移容器内的itemView
        offsetChildrenVertical(-dy);

        // recycle view
        handleRecycle(recycler, state);

        return dy;
    }

    private int scrollHorizontallyByImpl(int dx,
                                         RecyclerView.Recycler recycler,
                                         RecyclerView.State state) {
        // super.scrollHorizontallyBy(dx, recycler, state);
        return 0;
    }

    private void handleRecycle(RecyclerView.Recycler recycler,
                               RecyclerView.State state) {
        // 清空RecyclerView的子View
        // 将当前getchildcount数量的子View放入到scrap缓存池去
        /*detachAndScrapAttachedViews(recycler);

        Rect phoneFrame = new Rect(0, mScrollVerticallyOffset, getWidth(),
                mScrollVerticallyOffset + getHeight());//当前可见区域
        //将滑出屏幕的view进行回收
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            Rect child = allItemframs.get(i);
            if (!Rect.intersects(phoneFrame, child)) {
                removeAndRecycleView(childView, recycler);
            }
        }
        //可见区域出现在屏幕上的子view
        for (int j = 0; j < getItemCount(); j++) {
            if (Rect.intersects(phoneFrame, allItemframs.get(j))) {
                //                scrap回收池里面拿的
                View scrap = recycler.getViewForPosition(j);
                measureChildWithMargins(scrap, 0, 0);
                addView(scrap);
                Rect frame = allItemframs.get(j);
                layoutDecorated(scrap, frame.left, frame.top - verticalScrollOffset,
                        frame.right, frame.bottom - verticalScrollOffset);//给每一个itemview进行布局
            }
        }*/
    }

}
