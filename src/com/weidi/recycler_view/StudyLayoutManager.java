package com.weidi.recycler_view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.log.MLog;

/***

 */

public class StudyLayoutManager extends LayoutManager {

    private static final String TAG = StudyLayoutManager.class.getSimpleName();

    //    所有数据高度和
    private int totalHeight = 0;

    /**
     * 滑动偏移量
     * 如果是正的就是在向上滑，展现上面的view
     * 如果是负的向下
     */
    private int verticalScrollOffset = 0;

    private SparseArray<Rect> allItemframs = new SparseArray<>();

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    //摆放子布局
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() <= 0) {
            this.removeAndRecycleAllViews(recycler);
            return;
        }

        // 先把所有的View先从RecyclerView中detach掉，然后标记为"Scrap"状态，表示这些View处于可被重用状态(非显示中)。
        // 实际就是把View放到了Recycler中的一个集合中。
        detachAndScrapAttachedViews(recycler);

        //开始摆放
        int offsetY = 0;
        int offsetX = 0;
        int viewH = 0;
        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            //因为进行了detach操作，所以现在要重新添加
            addView(view);
            //通知测量itemView
            measureChildWithMargins(view, 0, 0);

            int w = getDecoratedMeasuredWidth(view);
            //获取itemVIEW的实际大小，包括measureChildWithMargins方法中设置的大小
            int h = getDecoratedMeasuredHeight(view);
            MLog.d(TAG, "onLayoutChildren() width: " + w + " height: " + h);
            viewH = h;
            Rect fram = allItemframs.get(i);
            if (fram == null) {
                fram = new Rect();
            }
            // 需要换行
            if (offsetX + w > getWidth()) {
                offsetY += h;
                offsetX = w;
                fram.set(0, offsetY, w, offsetY + h);
            } else {
                // 不需要换行
                fram.set(offsetX, offsetY, offsetX + w, offsetY + h);
                offsetX += w;
            }
            //            要 针对于当前View   生成对应的Rect  然后放到allItemframs数组
            allItemframs.put(i, fram);
            // 为什么不需要调用了呢
            // layoutDecorated(view, fram.left, fram.top, fram.right, fram.bottom);
        }

        // 调用这句我们指定了该View的显示区域，并将View显示上去，此时所有区域都用于显示View，
        //包括ItemDecorator设置的距离。
        totalHeight = offsetY + viewH;
        recyclerViewFillView(recycler, state);
    }


    private void recyclerViewFillView(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //        清空RecyclerView的子View
        detachAndScrapAttachedViews(recycler);//将当前getchildcount数量的子View放入到scrap缓存池去
        Rect phoneFrame = new Rect(0, verticalScrollOffset, getWidth(), verticalScrollOffset +
                getHeight());//当前可见区域
        //将滑出屏幕的view进行回收
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            Rect child = allItemframs.get(i);
            if (!Rect.intersects(phoneFrame, child)) {
                removeAndRecycleView(childView, recycler);
                //recyclerview移除当前childview，并将之放入到recycler缓存池
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
        }
    }


    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State
            state) {
        //实际滑动距离  dx
        int trval = dy;

        if (verticalScrollOffset + dy < 0) {//如果滑动到最顶部
            trval = -verticalScrollOffset;
        } else if (verticalScrollOffset + dy > totalHeight - getHeight()) {
            //            如果滑动到最底部  往上滑   verticalScrollOffset   +
            trval = totalHeight - getHeight() - verticalScrollOffset;
        }

        verticalScrollOffset += trval;
        //平移容器内的item
        offsetChildrenVertical(trval);
        recyclerViewFillView(recycler, state);//回收滑出去的itemview，并从缓存中拿出进行复用

        return trval;
    }
}
