//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.support.v7.widget.LinearLayoutManager.AnchorInfo;
import android.support.v7.widget.LinearLayoutManager.LayoutChunkResult;
import android.support.v7.widget.LinearLayoutManager.LayoutState;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry;
import android.support.v7.widget.RecyclerView.LayoutManager.Properties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import java.util.Arrays;

public class GridLayoutManager extends LinearLayoutManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "GridLayoutManager";
    public static final int DEFAULT_SPAN_COUNT = -1;
    boolean mPendingSpanCountChange = false;
    int mSpanCount = -1;
    int[] mCachedBorders;
    View[] mSet;
    final SparseIntArray mPreLayoutSpanSizeCache = new SparseIntArray();
    final SparseIntArray mPreLayoutSpanIndexCache = new SparseIntArray();
    GridLayoutManager.SpanSizeLookup mSpanSizeLookup = new GridLayoutManager.DefaultSpanSizeLookup();
    final Rect mDecorInsets = new Rect();

    public GridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        this.setSpanCount(properties.spanCount);
    }

    public GridLayoutManager(Context context, int spanCount) {
        super(context);
        this.setSpanCount(spanCount);
    }

    public GridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.setSpanCount(spanCount);
    }

    public void setStackFromEnd(boolean stackFromEnd) {
        if (stackFromEnd) {
            throw new UnsupportedOperationException("GridLayoutManager does not support stack from end. Consider using reverse layout");
        } else {
            super.setStackFromEnd(false);
        }
    }

    public int getRowCountForAccessibility(Recycler recycler, State state) {
        if (this.mOrientation == 0) {
            return this.mSpanCount;
        } else {
            return state.getItemCount() < 1 ? 0 : this.getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
        }
    }

    public int getColumnCountForAccessibility(Recycler recycler, State state) {
        if (this.mOrientation == 1) {
            return this.mSpanCount;
        } else {
            return state.getItemCount() < 1 ? 0 : this.getSpanGroupIndex(recycler, state, state.getItemCount() - 1) + 1;
        }
    }

    public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View host, AccessibilityNodeInfoCompat info) {
        android.view.ViewGroup.LayoutParams lp = host.getLayoutParams();
        if (!(lp instanceof GridLayoutManager.LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(host, info);
        } else {
            GridLayoutManager.LayoutParams glp = (GridLayoutManager.LayoutParams)lp;
            int spanGroupIndex = this.getSpanGroupIndex(recycler, state, glp.getViewLayoutPosition());
            if (this.mOrientation == 0) {
                info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(glp.getSpanIndex(), glp.getSpanSize(), spanGroupIndex, 1, this.mSpanCount > 1 && glp.getSpanSize() == this.mSpanCount, false));
            } else {
                info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(spanGroupIndex, 1, glp.getSpanIndex(), glp.getSpanSize(), this.mSpanCount > 1 && glp.getSpanSize() == this.mSpanCount, false));
            }

        }
    }

    public void onLayoutChildren(Recycler recycler, State state) {
        if (state.isPreLayout()) {
            this.cachePreLayoutSpanMapping();
        }

        super.onLayoutChildren(recycler, state);
        this.clearPreLayoutSpanMappingCache();
    }

    public void onLayoutCompleted(State state) {
        super.onLayoutCompleted(state);
        this.mPendingSpanCountChange = false;
    }

    private void clearPreLayoutSpanMappingCache() {
        this.mPreLayoutSpanSizeCache.clear();
        this.mPreLayoutSpanIndexCache.clear();
    }

    private void cachePreLayoutSpanMapping() {
        int childCount = this.getChildCount();

        for(int i = 0; i < childCount; ++i) {
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams)this.getChildAt(i).getLayoutParams();
            int viewPosition = lp.getViewLayoutPosition();
            this.mPreLayoutSpanSizeCache.put(viewPosition, lp.getSpanSize());
            this.mPreLayoutSpanIndexCache.put(viewPosition, lp.getSpanIndex());
        }

    }

    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    public void onItemsChanged(RecyclerView recyclerView) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return this.mOrientation == 0 ? new GridLayoutManager.LayoutParams(-2, -1) : new GridLayoutManager.LayoutParams(-1, -2);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new GridLayoutManager.LayoutParams(c, attrs);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        return lp instanceof MarginLayoutParams ? new GridLayoutManager.LayoutParams((MarginLayoutParams)lp) : new GridLayoutManager.LayoutParams(lp);
    }

    public boolean checkLayoutParams(android.support.v7.widget.RecyclerView.LayoutParams lp) {
        return lp instanceof GridLayoutManager.LayoutParams;
    }

    public void setSpanSizeLookup(GridLayoutManager.SpanSizeLookup spanSizeLookup) {
        this.mSpanSizeLookup = spanSizeLookup;
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return this.mSpanSizeLookup;
    }

    private void updateMeasurements() {
        int totalSpace;
        if (this.getOrientation() == 1) {
            totalSpace = this.getWidth() - this.getPaddingRight() - this.getPaddingLeft();
        } else {
            totalSpace = this.getHeight() - this.getPaddingBottom() - this.getPaddingTop();
        }

        this.calculateItemBorders(totalSpace);
    }

    public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
        if (this.mCachedBorders == null) {
            super.setMeasuredDimension(childrenBounds, wSpec, hSpec);
        }

        int horizontalPadding = this.getPaddingLeft() + this.getPaddingRight();
        int verticalPadding = this.getPaddingTop() + this.getPaddingBottom();
        int width;
        int height;
        int usedHeight;
        if (this.mOrientation == 1) {
            usedHeight = childrenBounds.height() + verticalPadding;
            height = chooseSize(hSpec, usedHeight, this.getMinimumHeight());
            width = chooseSize(wSpec, this.mCachedBorders[this.mCachedBorders.length - 1] + horizontalPadding, this.getMinimumWidth());
        } else {
            usedHeight = childrenBounds.width() + horizontalPadding;
            width = chooseSize(wSpec, usedHeight, this.getMinimumWidth());
            height = chooseSize(hSpec, this.mCachedBorders[this.mCachedBorders.length - 1] + verticalPadding, this.getMinimumHeight());
        }

        this.setMeasuredDimension(width, height);
    }

    private void calculateItemBorders(int totalSpace) {
        this.mCachedBorders = calculateItemBorders(this.mCachedBorders, this.mSpanCount, totalSpace);
    }

    static int[] calculateItemBorders(int[] cachedBorders, int spanCount, int totalSpace) {
        if (cachedBorders == null || cachedBorders.length != spanCount + 1 || cachedBorders[cachedBorders.length - 1] != totalSpace) {
            cachedBorders = new int[spanCount + 1];
        }

        cachedBorders[0] = 0;
        int sizePerSpan = totalSpace / spanCount;
        int sizePerSpanRemainder = totalSpace % spanCount;
        int consumedPixels = 0;
        int additionalSize = 0;

        for(int i = 1; i <= spanCount; ++i) {
            int itemSize = sizePerSpan;
            additionalSize += sizePerSpanRemainder;
            if (additionalSize > 0 && spanCount - additionalSize < sizePerSpanRemainder) {
                itemSize = sizePerSpan + 1;
                additionalSize -= spanCount;
            }

            consumedPixels += itemSize;
            cachedBorders[i] = consumedPixels;
        }

        return cachedBorders;
    }

    int getSpaceForSpanRange(int startSpan, int spanSize) {
        return this.mOrientation == 1 && this.isLayoutRTL() ? this.mCachedBorders[this.mSpanCount - startSpan] - this.mCachedBorders[this.mSpanCount - startSpan - spanSize] : this.mCachedBorders[startSpan + spanSize] - this.mCachedBorders[startSpan];
    }

    void onAnchorReady(Recycler recycler, State state, AnchorInfo anchorInfo, int itemDirection) {
        super.onAnchorReady(recycler, state, anchorInfo, itemDirection);
        this.updateMeasurements();
        if (state.getItemCount() > 0 && !state.isPreLayout()) {
            this.ensureAnchorIsInCorrectSpan(recycler, state, anchorInfo, itemDirection);
        }

        this.ensureViewSet();
    }

    private void ensureViewSet() {
        if (this.mSet == null || this.mSet.length != this.mSpanCount) {
            this.mSet = new View[this.mSpanCount];
        }

    }

    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        this.updateMeasurements();
        this.ensureViewSet();
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        this.updateMeasurements();
        this.ensureViewSet();
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    private void ensureAnchorIsInCorrectSpan(Recycler recycler, State state, AnchorInfo anchorInfo, int itemDirection) {
        boolean layingOutInPrimaryDirection = itemDirection == 1;
        int span = this.getSpanIndex(recycler, state, anchorInfo.mPosition);
        if (layingOutInPrimaryDirection) {
            while(span > 0 && anchorInfo.mPosition > 0) {
                --anchorInfo.mPosition;
                span = this.getSpanIndex(recycler, state, anchorInfo.mPosition);
            }
        } else {
            int indexLimit = state.getItemCount() - 1;
            int pos = anchorInfo.mPosition;

            int next;
            for(int bestSpan = span; pos < indexLimit; bestSpan = next) {
                next = this.getSpanIndex(recycler, state, pos + 1);
                if (next <= bestSpan) {
                    break;
                }

                ++pos;
            }

            anchorInfo.mPosition = pos;
        }

    }

    View findReferenceChild(Recycler recycler, State state, int start, int end, int itemCount) {
        this.ensureLayoutState();
        View invalidMatch = null;
        View outOfBoundsMatch = null;
        int boundsStart = this.mOrientationHelper.getStartAfterPadding();
        int boundsEnd = this.mOrientationHelper.getEndAfterPadding();
        int diff = end > start ? 1 : -1;

        for(int i = start; i != end; i += diff) {
            View view = this.getChildAt(i);
            int position = this.getPosition(view);
            if (position >= 0 && position < itemCount) {
                int span = this.getSpanIndex(recycler, state, position);
                if (span == 0) {
                    if (((android.support.v7.widget.RecyclerView.LayoutParams)view.getLayoutParams()).isItemRemoved()) {
                        if (invalidMatch == null) {
                            invalidMatch = view;
                        }
                    } else {
                        if (this.mOrientationHelper.getDecoratedStart(view) < boundsEnd && this.mOrientationHelper.getDecoratedEnd(view) >= boundsStart) {
                            return view;
                        }

                        if (outOfBoundsMatch == null) {
                            outOfBoundsMatch = view;
                        }
                    }
                }
            }
        }

        return outOfBoundsMatch != null ? outOfBoundsMatch : invalidMatch;
    }

    private int getSpanGroupIndex(Recycler recycler, State state, int viewPosition) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getSpanGroupIndex(viewPosition, this.mSpanCount);
        } else {
            int adapterPosition = recycler.convertPreLayoutPositionToPostLayout(viewPosition);
            if (adapterPosition == -1) {
                Log.w("GridLayoutManager", "Cannot find span size for pre layout position. " + viewPosition);
                return 0;
            } else {
                return this.mSpanSizeLookup.getSpanGroupIndex(adapterPosition, this.mSpanCount);
            }
        }
    }

    private int getSpanIndex(Recycler recycler, State state, int pos) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getCachedSpanIndex(pos, this.mSpanCount);
        } else {
            int cached = this.mPreLayoutSpanIndexCache.get(pos, -1);
            if (cached != -1) {
                return cached;
            } else {
                int adapterPosition = recycler.convertPreLayoutPositionToPostLayout(pos);
                if (adapterPosition == -1) {
                    Log.w("GridLayoutManager", "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + pos);
                    return 0;
                } else {
                    return this.mSpanSizeLookup.getCachedSpanIndex(adapterPosition, this.mSpanCount);
                }
            }
        }
    }

    private int getSpanSize(Recycler recycler, State state, int pos) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getSpanSize(pos);
        } else {
            int cached = this.mPreLayoutSpanSizeCache.get(pos, -1);
            if (cached != -1) {
                return cached;
            } else {
                int adapterPosition = recycler.convertPreLayoutPositionToPostLayout(pos);
                if (adapterPosition == -1) {
                    Log.w("GridLayoutManager", "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + pos);
                    return 1;
                } else {
                    return this.mSpanSizeLookup.getSpanSize(adapterPosition);
                }
            }
        }
    }

    void collectPrefetchPositionsForLayoutState(State state, LayoutState layoutState, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int remainingSpan = this.mSpanCount;

        for(int count = 0; count < this.mSpanCount && layoutState.hasMore(state) && remainingSpan > 0; ++count) {
            int pos = layoutState.mCurrentPosition;
            layoutPrefetchRegistry.addPosition(pos, Math.max(0, layoutState.mScrollingOffset));
            int spanSize = this.mSpanSizeLookup.getSpanSize(pos);
            remainingSpan -= spanSize;
            layoutState.mCurrentPosition += layoutState.mItemDirection;
        }

    }

    void layoutChunk(Recycler recycler, State state, LayoutState layoutState, LayoutChunkResult result) {
        int otherDirSpecMode = this.mOrientationHelper.getModeInOther();
        boolean flexibleInOtherDir = otherDirSpecMode != 1073741824;
        int currentOtherDirSize = this.getChildCount() > 0 ? this.mCachedBorders[this.mSpanCount] : 0;
        if (flexibleInOtherDir) {
            this.updateMeasurements();
        }

        boolean layingOutInPrimaryDirection = layoutState.mItemDirection == 1;
        int count = 0;
        int consumedSpanCount = 0;
        int remainingSpan = this.mSpanCount;
        int maxSize;
        int spanSize;
        if (!layingOutInPrimaryDirection) {
            maxSize = this.getSpanIndex(recycler, state, layoutState.mCurrentPosition);
            spanSize = this.getSpanSize(recycler, state, layoutState.mCurrentPosition);
            remainingSpan = maxSize + spanSize;
        }

        while(count < this.mSpanCount && layoutState.hasMore(state) && remainingSpan > 0) {
            maxSize = layoutState.mCurrentPosition;
            spanSize = this.getSpanSize(recycler, state, maxSize);
            if (spanSize > this.mSpanCount) {
                throw new IllegalArgumentException("Item at position " + maxSize + " requires " + spanSize + " spans but GridLayoutManager has only " + this.mSpanCount + " spans.");
            }

            remainingSpan -= spanSize;
            if (remainingSpan < 0) {
                break;
            }

            View view = layoutState.next(recycler);
            if (view == null) {
                break;
            }

            consumedSpanCount += spanSize;
            this.mSet[count] = view;
            ++count;
        }

        if (count == 0) {
            result.mFinished = true;
        } else {
            maxSize = 0;
            float maxSizeInOther = 0.0F;
            this.assignSpans(recycler, state, count, consumedSpanCount, layingOutInPrimaryDirection);

            View view;
            int top;
            int left;
            for(left = 0; left < count; ++left) {
                view = this.mSet[left];
                if (layoutState.mScrapList == null) {
                    if (layingOutInPrimaryDirection) {
                        this.addView(view);
                    } else {
                        this.addView(view, 0);
                    }
                } else if (layingOutInPrimaryDirection) {
                    this.addDisappearingView(view);
                } else {
                    this.addDisappearingView(view, 0);
                }

                this.calculateItemDecorationsForChild(view, this.mDecorInsets);
                this.measureChild(view, otherDirSpecMode, false);
                top = this.mOrientationHelper.getDecoratedMeasurement(view);
                if (top > maxSize) {
                    maxSize = top;
                }

                GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams)view.getLayoutParams();
                float otherSize = 1.0F * (float)this.mOrientationHelper.getDecoratedMeasurementInOther(view) / (float)lp.mSpanSize;
                if (otherSize > maxSizeInOther) {
                    maxSizeInOther = otherSize;
                }
            }

            if (flexibleInOtherDir) {
                this.guessMeasurement(maxSizeInOther, currentOtherDirSize);
                maxSize = 0;

                for(left = 0; left < count; ++left) {
                    view = this.mSet[left];
                    this.measureChild(view, 1073741824, true);
                    top = this.mOrientationHelper.getDecoratedMeasurement(view);
                    if (top > maxSize) {
                        maxSize = top;
                    }
                }
            }

            int i;
            for(left = 0; left < count; ++left) {
                view = this.mSet[left];
                if (this.mOrientationHelper.getDecoratedMeasurement(view) != maxSize) {
                    GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams)view.getLayoutParams();
                    Rect decorInsets = lp.mDecorInsets;
                    i = decorInsets.top + decorInsets.bottom + lp.topMargin + lp.bottomMargin;
                    int horizontalInsets = decorInsets.left + decorInsets.right + lp.leftMargin + lp.rightMargin;
                    int totalSpaceInOther = this.getSpaceForSpanRange(lp.mSpanIndex, lp.mSpanSize);
                    int wSpec;
                    int hSpec;
                    if (this.mOrientation == 1) {
                        wSpec = getChildMeasureSpec(totalSpaceInOther, 1073741824, horizontalInsets, lp.width, false);
                        hSpec = MeasureSpec.makeMeasureSpec(maxSize - i, 1073741824);
                    } else {
                        wSpec = MeasureSpec.makeMeasureSpec(maxSize - horizontalInsets, 1073741824);
                        hSpec = getChildMeasureSpec(totalSpaceInOther, 1073741824, i, lp.height, false);
                    }

                    this.measureChildWithDecorationsAndMargin(view, wSpec, hSpec, true);
                }
            }

            result.mConsumed = maxSize;
            left = 0;
            int right = 0;
            top = 0;
            int bottom = 0;
            if (this.mOrientation == 1) {
                if (layoutState.mLayoutDirection == -1) {
                    bottom = layoutState.mOffset;
                    top = bottom - maxSize;
                } else {
                    top = layoutState.mOffset;
                    bottom = top + maxSize;
                }
            } else if (layoutState.mLayoutDirection == -1) {
                right = layoutState.mOffset;
                left = right - maxSize;
            } else {
                left = layoutState.mOffset;
                right = left + maxSize;
            }

            for(i = 0; i < count; ++i) {
                view = this.mSet[i];
                GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams)view.getLayoutParams();
                if (this.mOrientation == 1) {
                    if (this.isLayoutRTL()) {
                        right = this.getPaddingLeft() + this.mCachedBorders[this.mSpanCount - params.mSpanIndex];
                        left = right - this.mOrientationHelper.getDecoratedMeasurementInOther(view);
                    } else {
                        left = this.getPaddingLeft() + this.mCachedBorders[params.mSpanIndex];
                        right = left + this.mOrientationHelper.getDecoratedMeasurementInOther(view);
                    }
                } else {
                    top = this.getPaddingTop() + this.mCachedBorders[params.mSpanIndex];
                    bottom = top + this.mOrientationHelper.getDecoratedMeasurementInOther(view);
                }

                this.layoutDecoratedWithMargins(view, left, top, right, bottom);
                if (params.isItemRemoved() || params.isItemChanged()) {
                    result.mIgnoreConsumed = true;
                }

                result.mFocusable |= view.hasFocusable();
            }

            Arrays.fill(this.mSet, (Object)null);
        }
    }

    private void measureChild(View view, int otherDirParentSpecMode, boolean alreadyMeasured) {
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams)view.getLayoutParams();
        Rect decorInsets = lp.mDecorInsets;
        int verticalInsets = decorInsets.top + decorInsets.bottom + lp.topMargin + lp.bottomMargin;
        int horizontalInsets = decorInsets.left + decorInsets.right + lp.leftMargin + lp.rightMargin;
        int availableSpaceInOther = this.getSpaceForSpanRange(lp.mSpanIndex, lp.mSpanSize);
        int wSpec;
        int hSpec;
        if (this.mOrientation == 1) {
            wSpec = getChildMeasureSpec(availableSpaceInOther, otherDirParentSpecMode, horizontalInsets, lp.width, false);
            hSpec = getChildMeasureSpec(this.mOrientationHelper.getTotalSpace(), this.getHeightMode(), verticalInsets, lp.height, true);
        } else {
            hSpec = getChildMeasureSpec(availableSpaceInOther, otherDirParentSpecMode, verticalInsets, lp.height, false);
            wSpec = getChildMeasureSpec(this.mOrientationHelper.getTotalSpace(), this.getWidthMode(), horizontalInsets, lp.width, true);
        }

        this.measureChildWithDecorationsAndMargin(view, wSpec, hSpec, alreadyMeasured);
    }

    private void guessMeasurement(float maxSizeInOther, int currentOtherDirSize) {
        int contentSize = Math.round(maxSizeInOther * (float)this.mSpanCount);
        this.calculateItemBorders(Math.max(contentSize, currentOtherDirSize));
    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec, boolean alreadyMeasured) {
        android.support.v7.widget.RecyclerView.LayoutParams lp = (android.support.v7.widget.RecyclerView.LayoutParams)child.getLayoutParams();
        boolean measure;
        if (alreadyMeasured) {
            measure = this.shouldReMeasureChild(child, widthSpec, heightSpec, lp);
        } else {
            measure = this.shouldMeasureChild(child, widthSpec, heightSpec, lp);
        }

        if (measure) {
            child.measure(widthSpec, heightSpec);
        }

    }

    private void assignSpans(Recycler recycler, State state, int count, int consumedSpanCount, boolean layingOutInPrimaryDirection) {
        int start;
        int end;
        byte diff;
        if (layingOutInPrimaryDirection) {
            start = 0;
            end = count;
            diff = 1;
        } else {
            start = count - 1;
            end = -1;
            diff = -1;
        }

        int span = 0;

        for(int i = start; i != end; i += diff) {
            View view = this.mSet[i];
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams)view.getLayoutParams();
            params.mSpanSize = this.getSpanSize(recycler, state, this.getPosition(view));
            params.mSpanIndex = span;
            span += params.mSpanSize;
        }

    }

    public int getSpanCount() {
        return this.mSpanCount;
    }

    public void setSpanCount(int spanCount) {
        if (spanCount != this.mSpanCount) {
            this.mPendingSpanCountChange = true;
            if (spanCount < 1) {
                throw new IllegalArgumentException("Span count should be at least 1. Provided " + spanCount);
            } else {
                this.mSpanCount = spanCount;
                this.mSpanSizeLookup.invalidateSpanIndexCache();
                this.requestLayout();
            }
        }
    }

    public View onFocusSearchFailed(View focused, int focusDirection, Recycler recycler, State state) {
        View prevFocusedChild = this.findContainingItemView(focused);
        if (prevFocusedChild == null) {
            return null;
        } else {
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams)prevFocusedChild.getLayoutParams();
            int prevSpanStart = lp.mSpanIndex;
            int prevSpanEnd = lp.mSpanIndex + lp.mSpanSize;
            View view = super.onFocusSearchFailed(focused, focusDirection, recycler, state);
            if (view == null) {
                return null;
            } else {
                int layoutDir = this.convertFocusDirectionToLayoutDirection(focusDirection);
                boolean ascend = layoutDir == 1 != this.mShouldReverseLayout;
                int start;
                byte inc;
                int limit;
                if (ascend) {
                    start = this.getChildCount() - 1;
                    inc = -1;
                    limit = -1;
                } else {
                    start = 0;
                    inc = 1;
                    limit = this.getChildCount();
                }

                boolean preferLastSpan = this.mOrientation == 1 && this.isLayoutRTL();
                View focusableWeakCandidate = null;
                int focusableWeakCandidateSpanIndex = -1;
                int focusableWeakCandidateOverlap = 0;
                View unfocusableWeakCandidate = null;
                int unfocusableWeakCandidateSpanIndex = -1;
                int unfocusableWeakCandidateOverlap = 0;
                int focusableSpanGroupIndex = this.getSpanGroupIndex(recycler, state, start);

                for(int i = start; i != limit; i += inc) {
                    int spanGroupIndex = this.getSpanGroupIndex(recycler, state, i);
                    View candidate = this.getChildAt(i);
                    if (candidate == prevFocusedChild) {
                        break;
                    }

                    if (candidate.hasFocusable() && spanGroupIndex != focusableSpanGroupIndex) {
                        if (focusableWeakCandidate != null) {
                            break;
                        }
                    } else {
                        GridLayoutManager.LayoutParams candidateLp = (GridLayoutManager.LayoutParams)candidate.getLayoutParams();
                        int candidateStart = candidateLp.mSpanIndex;
                        int candidateEnd = candidateLp.mSpanIndex + candidateLp.mSpanSize;
                        if (candidate.hasFocusable() && candidateStart == prevSpanStart && candidateEnd == prevSpanEnd) {
                            return candidate;
                        }

                        boolean assignAsWeek = false;
                        if (candidate.hasFocusable() && focusableWeakCandidate == null || !candidate.hasFocusable() && unfocusableWeakCandidate == null) {
                            assignAsWeek = true;
                        } else {
                            int maxStart = Math.max(candidateStart, prevSpanStart);
                            int minEnd = Math.min(candidateEnd, prevSpanEnd);
                            int overlap = minEnd - maxStart;
                            if (candidate.hasFocusable()) {
                                if (overlap > focusableWeakCandidateOverlap) {
                                    assignAsWeek = true;
                                } else if (overlap == focusableWeakCandidateOverlap && preferLastSpan == candidateStart > focusableWeakCandidateSpanIndex) {
                                    assignAsWeek = true;
                                }
                            } else if (focusableWeakCandidate == null && this.isViewPartiallyVisible(candidate, false, true)) {
                                if (overlap > unfocusableWeakCandidateOverlap) {
                                    assignAsWeek = true;
                                } else if (overlap == unfocusableWeakCandidateOverlap && preferLastSpan == candidateStart > unfocusableWeakCandidateSpanIndex) {
                                    assignAsWeek = true;
                                }
                            }
                        }

                        if (assignAsWeek) {
                            if (candidate.hasFocusable()) {
                                focusableWeakCandidate = candidate;
                                focusableWeakCandidateSpanIndex = candidateLp.mSpanIndex;
                                focusableWeakCandidateOverlap = Math.min(candidateEnd, prevSpanEnd) - Math.max(candidateStart, prevSpanStart);
                            } else {
                                unfocusableWeakCandidate = candidate;
                                unfocusableWeakCandidateSpanIndex = candidateLp.mSpanIndex;
                                unfocusableWeakCandidateOverlap = Math.min(candidateEnd, prevSpanEnd) - Math.max(candidateStart, prevSpanStart);
                            }
                        }
                    }
                }

                return focusableWeakCandidate != null ? focusableWeakCandidate : unfocusableWeakCandidate;
            }
        }
    }

    public boolean supportsPredictiveItemAnimations() {
        return this.mPendingSavedState == null && !this.mPendingSpanCountChange;
    }

    public static class LayoutParams extends android.support.v7.widget.RecyclerView.LayoutParams {
        public static final int INVALID_SPAN_ID = -1;
        int mSpanIndex = -1;
        int mSpanSize = 0;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(android.support.v7.widget.RecyclerView.LayoutParams source) {
            super(source);
        }

        public int getSpanIndex() {
            return this.mSpanIndex;
        }

        public int getSpanSize() {
            return this.mSpanSize;
        }
    }

    public static final class DefaultSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        public DefaultSpanSizeLookup() {
        }

        public int getSpanSize(int position) {
            return 1;
        }

        public int getSpanIndex(int position, int spanCount) {
            return position % spanCount;
        }
    }

    public abstract static class SpanSizeLookup {
        final SparseIntArray mSpanIndexCache = new SparseIntArray();
        private boolean mCacheSpanIndices = false;

        public SpanSizeLookup() {
        }

        public abstract int getSpanSize(int var1);

        public void setSpanIndexCacheEnabled(boolean cacheSpanIndices) {
            this.mCacheSpanIndices = cacheSpanIndices;
        }

        public void invalidateSpanIndexCache() {
            this.mSpanIndexCache.clear();
        }

        public boolean isSpanIndexCacheEnabled() {
            return this.mCacheSpanIndices;
        }

        int getCachedSpanIndex(int position, int spanCount) {
            if (!this.mCacheSpanIndices) {
                return this.getSpanIndex(position, spanCount);
            } else {
                int existing = this.mSpanIndexCache.get(position, -1);
                if (existing != -1) {
                    return existing;
                } else {
                    int value = this.getSpanIndex(position, spanCount);
                    this.mSpanIndexCache.put(position, value);
                    return value;
                }
            }
        }

        public int getSpanIndex(int position, int spanCount) {
            int positionSpanSize = this.getSpanSize(position);
            if (positionSpanSize == spanCount) {
                return 0;
            } else {
                int span = 0;
                int startPos = 0;
                int i;
                if (this.mCacheSpanIndices && this.mSpanIndexCache.size() > 0) {
                    i = this.findReferenceIndexFromCache(position);
                    if (i >= 0) {
                        span = this.mSpanIndexCache.get(i) + this.getSpanSize(i);
                        startPos = i + 1;
                    }
                }

                for(i = startPos; i < position; ++i) {
                    int size = this.getSpanSize(i);
                    span += size;
                    if (span == spanCount) {
                        span = 0;
                    } else if (span > spanCount) {
                        span = size;
                    }
                }

                if (span + positionSpanSize <= spanCount) {
                    return span;
                } else {
                    return 0;
                }
            }
        }

        int findReferenceIndexFromCache(int position) {
            int lo = 0;
            int hi = this.mSpanIndexCache.size() - 1;

            int index;
            while(lo <= hi) {
                index = lo + hi >>> 1;
                int midVal = this.mSpanIndexCache.keyAt(index);
                if (midVal < position) {
                    lo = index + 1;
                } else {
                    hi = index - 1;
                }
            }

            index = lo - 1;
            if (index >= 0 && index < this.mSpanIndexCache.size()) {
                return this.mSpanIndexCache.keyAt(index);
            } else {
                return -1;
            }
        }

        public int getSpanGroupIndex(int adapterPosition, int spanCount) {
            int span = 0;
            int group = 0;
            int positionSpanSize = this.getSpanSize(adapterPosition);

            for(int i = 0; i < adapterPosition; ++i) {
                int size = this.getSpanSize(i);
                span += size;
                if (span == spanCount) {
                    span = 0;
                    ++group;
                } else if (span > spanCount) {
                    span = size;
                    ++group;
                }
            }

            if (span + positionSpanSize > spanCount) {
                ++group;
            }

            return group;
        }
    }
}
