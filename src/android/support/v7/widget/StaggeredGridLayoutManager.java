//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry;
import android.support.v7.widget.RecyclerView.LayoutManager.Properties;
import android.support.v7.widget.RecyclerView.SmoothScroller.ScrollVectorProvider;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class StaggeredGridLayoutManager extends LayoutManager implements ScrollVectorProvider {
    private static final String TAG = "StaggeredGridLManager";
    static final boolean DEBUG = false;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int GAP_HANDLING_NONE = 0;
    /** @deprecated */
    @Deprecated
    public static final int GAP_HANDLING_LAZY = 1;
    public static final int GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS = 2;
    static final int INVALID_OFFSET = -2147483648;
    private static final float MAX_SCROLL_FACTOR = 0.33333334F;
    private int mSpanCount = -1;
    StaggeredGridLayoutManager.Span[] mSpans;
    @NonNull
    OrientationHelper mPrimaryOrientation;
    @NonNull
    OrientationHelper mSecondaryOrientation;
    private int mOrientation;
    private int mSizePerSpan;
    @NonNull
    private final LayoutState mLayoutState;
    boolean mReverseLayout = false;
    boolean mShouldReverseLayout = false;
    private BitSet mRemainingSpans;
    int mPendingScrollPosition = -1;
    int mPendingScrollPositionOffset = -2147483648;
    StaggeredGridLayoutManager.LazySpanLookup mLazySpanLookup = new StaggeredGridLayoutManager.LazySpanLookup();
    private int mGapStrategy = 2;
    private boolean mLastLayoutFromEnd;
    private boolean mLastLayoutRTL;
    private StaggeredGridLayoutManager.SavedState mPendingSavedState;
    private int mFullSizeSpec;
    private final Rect mTmpRect = new Rect();
    private final StaggeredGridLayoutManager.AnchorInfo mAnchorInfo = new StaggeredGridLayoutManager.AnchorInfo();
    private boolean mLaidOutInvalidFullSpan = false;
    private boolean mSmoothScrollbarEnabled = true;
    private int[] mPrefetchDistances;
    private final Runnable mCheckForGapsRunnable;

    public StaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        class NamelessClass_1 implements Runnable {
            NamelessClass_1() {
            }

            public void run() {
                StaggeredGridLayoutManager.this.checkForGaps();
            }
        }

        this.mCheckForGapsRunnable = new NamelessClass_1();
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        this.setOrientation(properties.orientation);
        this.setSpanCount(properties.spanCount);
        this.setReverseLayout(properties.reverseLayout);
        this.mLayoutState = new LayoutState();
        this.createOrientationHelpers();
    }

    public StaggeredGridLayoutManager(int spanCount, int orientation) {
        class NamelessClass_1 implements Runnable {
            NamelessClass_1() {
            }

            public void run() {
                StaggeredGridLayoutManager.this.checkForGaps();
            }
        }

        this.mCheckForGapsRunnable = new NamelessClass_1();
        this.mOrientation = orientation;
        this.setSpanCount(spanCount);
        this.mLayoutState = new LayoutState();
        this.createOrientationHelpers();
    }

    public boolean isAutoMeasureEnabled() {
        return this.mGapStrategy != 0;
    }

    private void createOrientationHelpers() {
        this.mPrimaryOrientation = OrientationHelper.createOrientationHelper(this, this.mOrientation);
        this.mSecondaryOrientation = OrientationHelper.createOrientationHelper(this, 1 - this.mOrientation);
    }

    boolean checkForGaps() {
        if (this.getChildCount() != 0 && this.mGapStrategy != 0 && this.isAttachedToWindow()) {
            int minPos;
            int maxPos;
            if (this.mShouldReverseLayout) {
                minPos = this.getLastChildPosition();
                maxPos = this.getFirstChildPosition();
            } else {
                minPos = this.getFirstChildPosition();
                maxPos = this.getLastChildPosition();
            }

            if (minPos == 0) {
                View gapView = this.hasGapsToFix();
                if (gapView != null) {
                    this.mLazySpanLookup.clear();
                    this.requestSimpleAnimationsInNextLayout();
                    this.requestLayout();
                    return true;
                }
            }

            if (!this.mLaidOutInvalidFullSpan) {
                return false;
            } else {
                int invalidGapDir = this.mShouldReverseLayout ? -1 : 1;
                StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem invalidFsi = this.mLazySpanLookup.getFirstFullSpanItemInRange(minPos, maxPos + 1, invalidGapDir, true);
                if (invalidFsi == null) {
                    this.mLaidOutInvalidFullSpan = false;
                    this.mLazySpanLookup.forceInvalidateAfter(maxPos + 1);
                    return false;
                } else {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem validFsi = this.mLazySpanLookup.getFirstFullSpanItemInRange(minPos, invalidFsi.mPosition, invalidGapDir * -1, true);
                    if (validFsi == null) {
                        this.mLazySpanLookup.forceInvalidateAfter(invalidFsi.mPosition);
                    } else {
                        this.mLazySpanLookup.forceInvalidateAfter(validFsi.mPosition + 1);
                    }

                    this.requestSimpleAnimationsInNextLayout();
                    this.requestLayout();
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    public void onScrollStateChanged(int state) {
        if (state == 0) {
            this.checkForGaps();
        }

    }

    public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        this.removeCallbacks(this.mCheckForGapsRunnable);

        for(int i = 0; i < this.mSpanCount; ++i) {
            this.mSpans[i].clear();
        }

        view.requestLayout();
    }

    View hasGapsToFix() {
        int startChildIndex = 0;
        int endChildIndex = this.getChildCount() - 1;
        BitSet mSpansToCheck = new BitSet(this.mSpanCount);
        mSpansToCheck.set(0, this.mSpanCount, true);
        int preferredSpanDir = this.mOrientation == 1 && this.isLayoutRTL() ? 1 : -1;
        int firstChildIndex;
        int childLimit;
        if (this.mShouldReverseLayout) {
            firstChildIndex = endChildIndex;
            childLimit = startChildIndex - 1;
        } else {
            firstChildIndex = startChildIndex;
            childLimit = endChildIndex + 1;
        }

        int nextChildDiff = firstChildIndex < childLimit ? 1 : -1;

        for(int i = firstChildIndex; i != childLimit; i += nextChildDiff) {
            View child = this.getChildAt(i);
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)child.getLayoutParams();
            if (mSpansToCheck.get(lp.mSpan.mIndex)) {
                if (this.checkSpanForGap(lp.mSpan)) {
                    return child;
                }

                mSpansToCheck.clear(lp.mSpan.mIndex);
            }

            if (!lp.mFullSpan && i + nextChildDiff != childLimit) {
                View nextChild = this.getChildAt(i + nextChildDiff);
                boolean compareSpans = false;
                int myEnd;
                int nextEnd;
                if (this.mShouldReverseLayout) {
                    myEnd = this.mPrimaryOrientation.getDecoratedEnd(child);
                    nextEnd = this.mPrimaryOrientation.getDecoratedEnd(nextChild);
                    if (myEnd < nextEnd) {
                        return child;
                    }

                    if (myEnd == nextEnd) {
                        compareSpans = true;
                    }
                } else {
                    myEnd = this.mPrimaryOrientation.getDecoratedStart(child);
                    nextEnd = this.mPrimaryOrientation.getDecoratedStart(nextChild);
                    if (myEnd > nextEnd) {
                        return child;
                    }

                    if (myEnd == nextEnd) {
                        compareSpans = true;
                    }
                }

                if (compareSpans) {
                    StaggeredGridLayoutManager.LayoutParams nextLp = (StaggeredGridLayoutManager.LayoutParams)nextChild.getLayoutParams();
                    if (lp.mSpan.mIndex - nextLp.mSpan.mIndex < 0 != preferredSpanDir < 0) {
                        return child;
                    }
                }
            }
        }

        return null;
    }

    private boolean checkSpanForGap(StaggeredGridLayoutManager.Span span) {
        View endView;
        StaggeredGridLayoutManager.LayoutParams lp;
        if (this.mShouldReverseLayout) {
            if (span.getEndLine() < this.mPrimaryOrientation.getEndAfterPadding()) {
                endView = (View)span.mViews.get(span.mViews.size() - 1);
                lp = span.getLayoutParams(endView);
                return !lp.mFullSpan;
            }
        } else if (span.getStartLine() > this.mPrimaryOrientation.getStartAfterPadding()) {
            endView = (View)span.mViews.get(0);
            lp = span.getLayoutParams(endView);
            return !lp.mFullSpan;
        }

        return false;
    }

    public void setSpanCount(int spanCount) {
        this.assertNotInLayoutOrScroll((String)null);
        if (spanCount != this.mSpanCount) {
            this.invalidateSpanAssignments();
            this.mSpanCount = spanCount;
            this.mRemainingSpans = new BitSet(this.mSpanCount);
            this.mSpans = new StaggeredGridLayoutManager.Span[this.mSpanCount];

            for(int i = 0; i < this.mSpanCount; ++i) {
                this.mSpans[i] = new StaggeredGridLayoutManager.Span(i);
            }

            this.requestLayout();
        }

    }

    public void setOrientation(int orientation) {
        if (orientation != 0 && orientation != 1) {
            throw new IllegalArgumentException("invalid orientation.");
        } else {
            this.assertNotInLayoutOrScroll((String)null);
            if (orientation != this.mOrientation) {
                this.mOrientation = orientation;
                OrientationHelper tmp = this.mPrimaryOrientation;
                this.mPrimaryOrientation = this.mSecondaryOrientation;
                this.mSecondaryOrientation = tmp;
                this.requestLayout();
            }
        }
    }

    public void setReverseLayout(boolean reverseLayout) {
        this.assertNotInLayoutOrScroll((String)null);
        if (this.mPendingSavedState != null && this.mPendingSavedState.mReverseLayout != reverseLayout) {
            this.mPendingSavedState.mReverseLayout = reverseLayout;
        }

        this.mReverseLayout = reverseLayout;
        this.requestLayout();
    }

    public int getGapStrategy() {
        return this.mGapStrategy;
    }

    public void setGapStrategy(int gapStrategy) {
        this.assertNotInLayoutOrScroll((String)null);
        if (gapStrategy != this.mGapStrategy) {
            if (gapStrategy != 0 && gapStrategy != 2) {
                throw new IllegalArgumentException("invalid gap strategy. Must be GAP_HANDLING_NONE or GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS");
            } else {
                this.mGapStrategy = gapStrategy;
                this.requestLayout();
            }
        }
    }

    public void assertNotInLayoutOrScroll(String message) {
        if (this.mPendingSavedState == null) {
            super.assertNotInLayoutOrScroll(message);
        }

    }

    public int getSpanCount() {
        return this.mSpanCount;
    }

    public void invalidateSpanAssignments() {
        this.mLazySpanLookup.clear();
        this.requestLayout();
    }

    private void resolveShouldLayoutReverse() {
        if (this.mOrientation != 1 && this.isLayoutRTL()) {
            this.mShouldReverseLayout = !this.mReverseLayout;
        } else {
            this.mShouldReverseLayout = this.mReverseLayout;
        }

    }

    boolean isLayoutRTL() {
        return this.getLayoutDirection() == 1;
    }

    public boolean getReverseLayout() {
        return this.mReverseLayout;
    }

    public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
        int horizontalPadding = this.getPaddingLeft() + this.getPaddingRight();
        int verticalPadding = this.getPaddingTop() + this.getPaddingBottom();
        int width;
        int height;
        int usedHeight;
        if (this.mOrientation == 1) {
            usedHeight = childrenBounds.height() + verticalPadding;
            height = chooseSize(hSpec, usedHeight, this.getMinimumHeight());
            width = chooseSize(wSpec, this.mSizePerSpan * this.mSpanCount + horizontalPadding, this.getMinimumWidth());
        } else {
            usedHeight = childrenBounds.width() + horizontalPadding;
            width = chooseSize(wSpec, usedHeight, this.getMinimumWidth());
            height = chooseSize(hSpec, this.mSizePerSpan * this.mSpanCount + verticalPadding, this.getMinimumHeight());
        }

        this.setMeasuredDimension(width, height);
    }

    public void onLayoutChildren(Recycler recycler, State state) {
        this.onLayoutChildren(recycler, state, true);
    }

    private void onLayoutChildren(Recycler recycler, State state, boolean shouldCheckForGaps) {
        StaggeredGridLayoutManager.AnchorInfo anchorInfo = this.mAnchorInfo;
        if ((this.mPendingSavedState != null || this.mPendingScrollPosition != -1) && state.getItemCount() == 0) {
            this.removeAndRecycleAllViews(recycler);
            anchorInfo.reset();
        } else {
            boolean recalculateAnchor = !anchorInfo.mValid || this.mPendingScrollPosition != -1 || this.mPendingSavedState != null;
            if (recalculateAnchor) {
                anchorInfo.reset();
                if (this.mPendingSavedState != null) {
                    this.applyPendingSavedState(anchorInfo);
                } else {
                    this.resolveShouldLayoutReverse();
                    anchorInfo.mLayoutFromEnd = this.mShouldReverseLayout;
                }

                this.updateAnchorInfoForLayout(state, anchorInfo);
                anchorInfo.mValid = true;
            }

            if (this.mPendingSavedState == null && this.mPendingScrollPosition == -1 && (anchorInfo.mLayoutFromEnd != this.mLastLayoutFromEnd || this.isLayoutRTL() != this.mLastLayoutRTL)) {
                this.mLazySpanLookup.clear();
                anchorInfo.mInvalidateOffsets = true;
            }

            if (this.getChildCount() > 0 && (this.mPendingSavedState == null || this.mPendingSavedState.mSpanOffsetsSize < 1)) {
                int i;
                if (anchorInfo.mInvalidateOffsets) {
                    for(i = 0; i < this.mSpanCount; ++i) {
                        this.mSpans[i].clear();
                        if (anchorInfo.mOffset != -2147483648) {
                            this.mSpans[i].setLine(anchorInfo.mOffset);
                        }
                    }
                } else if (!recalculateAnchor && this.mAnchorInfo.mSpanReferenceLines != null) {
                    for(i = 0; i < this.mSpanCount; ++i) {
                        StaggeredGridLayoutManager.Span span = this.mSpans[i];
                        span.clear();
                        span.setLine(this.mAnchorInfo.mSpanReferenceLines[i]);
                    }
                } else {
                    for(i = 0; i < this.mSpanCount; ++i) {
                        this.mSpans[i].cacheReferenceLineAndClear(this.mShouldReverseLayout, anchorInfo.mOffset);
                    }

                    this.mAnchorInfo.saveSpanReferenceLines(this.mSpans);
                }
            }

            this.detachAndScrapAttachedViews(recycler);
            this.mLayoutState.mRecycle = false;
            this.mLaidOutInvalidFullSpan = false;
            this.updateMeasureSpecs(this.mSecondaryOrientation.getTotalSpace());
            this.updateLayoutState(anchorInfo.mPosition, state);
            if (anchorInfo.mLayoutFromEnd) {
                this.setLayoutStateDirection(-1);
                this.fill(recycler, this.mLayoutState, state);
                this.setLayoutStateDirection(1);
                this.mLayoutState.mCurrentPosition = anchorInfo.mPosition + this.mLayoutState.mItemDirection;
                this.fill(recycler, this.mLayoutState, state);
            } else {
                this.setLayoutStateDirection(1);
                this.fill(recycler, this.mLayoutState, state);
                this.setLayoutStateDirection(-1);
                this.mLayoutState.mCurrentPosition = anchorInfo.mPosition + this.mLayoutState.mItemDirection;
                this.fill(recycler, this.mLayoutState, state);
            }

            this.repositionToWrapContentIfNecessary();
            if (this.getChildCount() > 0) {
                if (this.mShouldReverseLayout) {
                    this.fixEndGap(recycler, state, true);
                    this.fixStartGap(recycler, state, false);
                } else {
                    this.fixStartGap(recycler, state, true);
                    this.fixEndGap(recycler, state, false);
                }
            }

            boolean hasGaps = false;
            if (shouldCheckForGaps && !state.isPreLayout()) {
                boolean needToCheckForGaps = this.mGapStrategy != 0 && this.getChildCount() > 0 && (this.mLaidOutInvalidFullSpan || this.hasGapsToFix() != null);
                if (needToCheckForGaps) {
                    this.removeCallbacks(this.mCheckForGapsRunnable);
                    if (this.checkForGaps()) {
                        hasGaps = true;
                    }
                }
            }

            if (state.isPreLayout()) {
                this.mAnchorInfo.reset();
            }

            this.mLastLayoutFromEnd = anchorInfo.mLayoutFromEnd;
            this.mLastLayoutRTL = this.isLayoutRTL();
            if (hasGaps) {
                this.mAnchorInfo.reset();
                this.onLayoutChildren(recycler, state, false);
            }

        }
    }

    public void onLayoutCompleted(State state) {
        super.onLayoutCompleted(state);
        this.mPendingScrollPosition = -1;
        this.mPendingScrollPositionOffset = -2147483648;
        this.mPendingSavedState = null;
        this.mAnchorInfo.reset();
    }

    private void repositionToWrapContentIfNecessary() {
        if (this.mSecondaryOrientation.getMode() != 1073741824) {
            float maxSize = 0.0F;
            int childCount = this.getChildCount();

            int before;
            for(before = 0; before < childCount; ++before) {
                View child = this.getChildAt(before);
                float size = (float)this.mSecondaryOrientation.getDecoratedMeasurement(child);
                if (size >= maxSize) {
                    StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)child.getLayoutParams();
                    if (layoutParams.isFullSpan()) {
                        size = 1.0F * size / (float)this.mSpanCount;
                    }

                    maxSize = Math.max(maxSize, size);
                }
            }

            before = this.mSizePerSpan;
            int desired = Math.round(maxSize * (float)this.mSpanCount);
            if (this.mSecondaryOrientation.getMode() == -2147483648) {
                desired = Math.min(desired, this.mSecondaryOrientation.getTotalSpace());
            }

            this.updateMeasureSpecs(desired);
            if (this.mSizePerSpan != before) {
                for(int i = 0; i < childCount; ++i) {
                    View child = this.getChildAt(i);
                    StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)child.getLayoutParams();
                    if (!lp.mFullSpan) {
                        int newOffset;
                        int prevOffset;
                        if (this.isLayoutRTL() && this.mOrientation == 1) {
                            newOffset = -(this.mSpanCount - 1 - lp.mSpan.mIndex) * this.mSizePerSpan;
                            prevOffset = -(this.mSpanCount - 1 - lp.mSpan.mIndex) * before;
                            child.offsetLeftAndRight(newOffset - prevOffset);
                        } else {
                            newOffset = lp.mSpan.mIndex * this.mSizePerSpan;
                            prevOffset = lp.mSpan.mIndex * before;
                            if (this.mOrientation == 1) {
                                child.offsetLeftAndRight(newOffset - prevOffset);
                            } else {
                                child.offsetTopAndBottom(newOffset - prevOffset);
                            }
                        }
                    }
                }

            }
        }
    }

    private void applyPendingSavedState(StaggeredGridLayoutManager.AnchorInfo anchorInfo) {
        if (this.mPendingSavedState.mSpanOffsetsSize > 0) {
            if (this.mPendingSavedState.mSpanOffsetsSize == this.mSpanCount) {
                for(int i = 0; i < this.mSpanCount; ++i) {
                    this.mSpans[i].clear();
                    int line = this.mPendingSavedState.mSpanOffsets[i];
                    if (line != -2147483648) {
                        if (this.mPendingSavedState.mAnchorLayoutFromEnd) {
                            line += this.mPrimaryOrientation.getEndAfterPadding();
                        } else {
                            line += this.mPrimaryOrientation.getStartAfterPadding();
                        }
                    }

                    this.mSpans[i].setLine(line);
                }
            } else {
                this.mPendingSavedState.invalidateSpanInfo();
                this.mPendingSavedState.mAnchorPosition = this.mPendingSavedState.mVisibleAnchorPosition;
            }
        }

        this.mLastLayoutRTL = this.mPendingSavedState.mLastLayoutRTL;
        this.setReverseLayout(this.mPendingSavedState.mReverseLayout);
        this.resolveShouldLayoutReverse();
        if (this.mPendingSavedState.mAnchorPosition != -1) {
            this.mPendingScrollPosition = this.mPendingSavedState.mAnchorPosition;
            anchorInfo.mLayoutFromEnd = this.mPendingSavedState.mAnchorLayoutFromEnd;
        } else {
            anchorInfo.mLayoutFromEnd = this.mShouldReverseLayout;
        }

        if (this.mPendingSavedState.mSpanLookupSize > 1) {
            this.mLazySpanLookup.mData = this.mPendingSavedState.mSpanLookup;
            this.mLazySpanLookup.mFullSpanItems = this.mPendingSavedState.mFullSpanItems;
        }

    }

    void updateAnchorInfoForLayout(State state, StaggeredGridLayoutManager.AnchorInfo anchorInfo) {
        if (!this.updateAnchorFromPendingData(state, anchorInfo)) {
            if (!this.updateAnchorFromChildren(state, anchorInfo)) {
                anchorInfo.assignCoordinateFromPadding();
                anchorInfo.mPosition = 0;
            }
        }
    }

    private boolean updateAnchorFromChildren(State state, StaggeredGridLayoutManager.AnchorInfo anchorInfo) {
        anchorInfo.mPosition = this.mLastLayoutFromEnd ? this.findLastReferenceChildPosition(state.getItemCount()) : this.findFirstReferenceChildPosition(state.getItemCount());
        anchorInfo.mOffset = -2147483648;
        return true;
    }

    boolean updateAnchorFromPendingData(State state, StaggeredGridLayoutManager.AnchorInfo anchorInfo) {
        if (!state.isPreLayout() && this.mPendingScrollPosition != -1) {
            if (this.mPendingScrollPosition >= 0 && this.mPendingScrollPosition < state.getItemCount()) {
                if (this.mPendingSavedState != null && this.mPendingSavedState.mAnchorPosition != -1 && this.mPendingSavedState.mSpanOffsetsSize >= 1) {
                    anchorInfo.mOffset = -2147483648;
                    anchorInfo.mPosition = this.mPendingScrollPosition;
                } else {
                    View child = this.findViewByPosition(this.mPendingScrollPosition);
                    int target;
                    if (child != null) {
                        anchorInfo.mPosition = this.mShouldReverseLayout ? this.getLastChildPosition() : this.getFirstChildPosition();
                        if (this.mPendingScrollPositionOffset != -2147483648) {
                            if (anchorInfo.mLayoutFromEnd) {
                                target = this.mPrimaryOrientation.getEndAfterPadding() - this.mPendingScrollPositionOffset;
                                anchorInfo.mOffset = target - this.mPrimaryOrientation.getDecoratedEnd(child);
                            } else {
                                target = this.mPrimaryOrientation.getStartAfterPadding() + this.mPendingScrollPositionOffset;
                                anchorInfo.mOffset = target - this.mPrimaryOrientation.getDecoratedStart(child);
                            }

                            return true;
                        }

                        target = this.mPrimaryOrientation.getDecoratedMeasurement(child);
                        if (target > this.mPrimaryOrientation.getTotalSpace()) {
                            anchorInfo.mOffset = anchorInfo.mLayoutFromEnd ? this.mPrimaryOrientation.getEndAfterPadding() : this.mPrimaryOrientation.getStartAfterPadding();
                            return true;
                        }

                        int startGap = this.mPrimaryOrientation.getDecoratedStart(child) - this.mPrimaryOrientation.getStartAfterPadding();
                        if (startGap < 0) {
                            anchorInfo.mOffset = -startGap;
                            return true;
                        }

                        int endGap = this.mPrimaryOrientation.getEndAfterPadding() - this.mPrimaryOrientation.getDecoratedEnd(child);
                        if (endGap < 0) {
                            anchorInfo.mOffset = endGap;
                            return true;
                        }

                        anchorInfo.mOffset = -2147483648;
                    } else {
                        anchorInfo.mPosition = this.mPendingScrollPosition;
                        if (this.mPendingScrollPositionOffset == -2147483648) {
                            target = this.calculateScrollDirectionForPosition(anchorInfo.mPosition);
                            anchorInfo.mLayoutFromEnd = target == 1;
                            anchorInfo.assignCoordinateFromPadding();
                        } else {
                            anchorInfo.assignCoordinateFromPadding(this.mPendingScrollPositionOffset);
                        }

                        anchorInfo.mInvalidateOffsets = true;
                    }
                }

                return true;
            } else {
                this.mPendingScrollPosition = -1;
                this.mPendingScrollPositionOffset = -2147483648;
                return false;
            }
        } else {
            return false;
        }
    }

    void updateMeasureSpecs(int totalSpace) {
        this.mSizePerSpan = totalSpace / this.mSpanCount;
        this.mFullSizeSpec = MeasureSpec.makeMeasureSpec(totalSpace, this.mSecondaryOrientation.getMode());
    }

    public boolean supportsPredictiveItemAnimations() {
        return this.mPendingSavedState == null;
    }

    public int[] findFirstVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[this.mSpanCount];
        } else if (into.length < this.mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal to span count. Expected:" + this.mSpanCount + ", array size:" + into.length);
        }

        for(int i = 0; i < this.mSpanCount; ++i) {
            into[i] = this.mSpans[i].findFirstVisibleItemPosition();
        }

        return into;
    }

    public int[] findFirstCompletelyVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[this.mSpanCount];
        } else if (into.length < this.mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal to span count. Expected:" + this.mSpanCount + ", array size:" + into.length);
        }

        for(int i = 0; i < this.mSpanCount; ++i) {
            into[i] = this.mSpans[i].findFirstCompletelyVisibleItemPosition();
        }

        return into;
    }

    public int[] findLastVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[this.mSpanCount];
        } else if (into.length < this.mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal to span count. Expected:" + this.mSpanCount + ", array size:" + into.length);
        }

        for(int i = 0; i < this.mSpanCount; ++i) {
            into[i] = this.mSpans[i].findLastVisibleItemPosition();
        }

        return into;
    }

    public int[] findLastCompletelyVisibleItemPositions(int[] into) {
        if (into == null) {
            into = new int[this.mSpanCount];
        } else if (into.length < this.mSpanCount) {
            throw new IllegalArgumentException("Provided int[]'s size must be more than or equal to span count. Expected:" + this.mSpanCount + ", array size:" + into.length);
        }

        for(int i = 0; i < this.mSpanCount; ++i) {
            into[i] = this.mSpans[i].findLastCompletelyVisibleItemPosition();
        }

        return into;
    }

    public int computeHorizontalScrollOffset(State state) {
        return this.computeScrollOffset(state);
    }

    private int computeScrollOffset(State state) {
        return this.getChildCount() == 0 ? 0 : ScrollbarHelper.computeScrollOffset(state, this.mPrimaryOrientation, this.findFirstVisibleItemClosestToStart(!this.mSmoothScrollbarEnabled), this.findFirstVisibleItemClosestToEnd(!this.mSmoothScrollbarEnabled), this, this.mSmoothScrollbarEnabled, this.mShouldReverseLayout);
    }

    public int computeVerticalScrollOffset(State state) {
        return this.computeScrollOffset(state);
    }

    public int computeHorizontalScrollExtent(State state) {
        return this.computeScrollExtent(state);
    }

    private int computeScrollExtent(State state) {
        return this.getChildCount() == 0 ? 0 : ScrollbarHelper.computeScrollExtent(state, this.mPrimaryOrientation, this.findFirstVisibleItemClosestToStart(!this.mSmoothScrollbarEnabled), this.findFirstVisibleItemClosestToEnd(!this.mSmoothScrollbarEnabled), this, this.mSmoothScrollbarEnabled);
    }

    public int computeVerticalScrollExtent(State state) {
        return this.computeScrollExtent(state);
    }

    public int computeHorizontalScrollRange(State state) {
        return this.computeScrollRange(state);
    }

    private int computeScrollRange(State state) {
        return this.getChildCount() == 0 ? 0 : ScrollbarHelper.computeScrollRange(state, this.mPrimaryOrientation, this.findFirstVisibleItemClosestToStart(!this.mSmoothScrollbarEnabled), this.findFirstVisibleItemClosestToEnd(!this.mSmoothScrollbarEnabled), this, this.mSmoothScrollbarEnabled);
    }

    public int computeVerticalScrollRange(State state) {
        return this.computeScrollRange(state);
    }

    private void measureChildWithDecorationsAndMargin(View child, StaggeredGridLayoutManager.LayoutParams lp, boolean alreadyMeasured) {
        if (lp.mFullSpan) {
            if (this.mOrientation == 1) {
                this.measureChildWithDecorationsAndMargin(child, this.mFullSizeSpec, getChildMeasureSpec(this.getHeight(), this.getHeightMode(), this.getPaddingTop() + this.getPaddingBottom(), lp.height, true), alreadyMeasured);
            } else {
                this.measureChildWithDecorationsAndMargin(child, getChildMeasureSpec(this.getWidth(), this.getWidthMode(), this.getPaddingLeft() + this.getPaddingRight(), lp.width, true), this.mFullSizeSpec, alreadyMeasured);
            }
        } else if (this.mOrientation == 1) {
            this.measureChildWithDecorationsAndMargin(child, getChildMeasureSpec(this.mSizePerSpan, this.getWidthMode(), 0, lp.width, false), getChildMeasureSpec(this.getHeight(), this.getHeightMode(), this.getPaddingTop() + this.getPaddingBottom(), lp.height, true), alreadyMeasured);
        } else {
            this.measureChildWithDecorationsAndMargin(child, getChildMeasureSpec(this.getWidth(), this.getWidthMode(), this.getPaddingLeft() + this.getPaddingRight(), lp.width, true), getChildMeasureSpec(this.mSizePerSpan, this.getHeightMode(), 0, lp.height, false), alreadyMeasured);
        }

    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec, boolean alreadyMeasured) {
        this.calculateItemDecorationsForChild(child, this.mTmpRect);
        StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)child.getLayoutParams();
        widthSpec = this.updateSpecWithExtra(widthSpec, lp.leftMargin + this.mTmpRect.left, lp.rightMargin + this.mTmpRect.right);
        heightSpec = this.updateSpecWithExtra(heightSpec, lp.topMargin + this.mTmpRect.top, lp.bottomMargin + this.mTmpRect.bottom);
        boolean measure = alreadyMeasured ? this.shouldReMeasureChild(child, widthSpec, heightSpec, lp) : this.shouldMeasureChild(child, widthSpec, heightSpec, lp);
        if (measure) {
            child.measure(widthSpec, heightSpec);
        }

    }

    private int updateSpecWithExtra(int spec, int startInset, int endInset) {
        if (startInset == 0 && endInset == 0) {
            return spec;
        } else {
            int mode = MeasureSpec.getMode(spec);
            return mode != -2147483648 && mode != 1073741824 ? spec : MeasureSpec.makeMeasureSpec(Math.max(0, MeasureSpec.getSize(spec) - startInset - endInset), mode);
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof StaggeredGridLayoutManager.SavedState) {
            this.mPendingSavedState = (StaggeredGridLayoutManager.SavedState)state;
            this.requestLayout();
        }

    }

    public Parcelable onSaveInstanceState() {
        if (this.mPendingSavedState != null) {
            return new StaggeredGridLayoutManager.SavedState(this.mPendingSavedState);
        } else {
            StaggeredGridLayoutManager.SavedState state = new StaggeredGridLayoutManager.SavedState();
            state.mReverseLayout = this.mReverseLayout;
            state.mAnchorLayoutFromEnd = this.mLastLayoutFromEnd;
            state.mLastLayoutRTL = this.mLastLayoutRTL;
            if (this.mLazySpanLookup != null && this.mLazySpanLookup.mData != null) {
                state.mSpanLookup = this.mLazySpanLookup.mData;
                state.mSpanLookupSize = state.mSpanLookup.length;
                state.mFullSpanItems = this.mLazySpanLookup.mFullSpanItems;
            } else {
                state.mSpanLookupSize = 0;
            }

            if (this.getChildCount() > 0) {
                state.mAnchorPosition = this.mLastLayoutFromEnd ? this.getLastChildPosition() : this.getFirstChildPosition();
                state.mVisibleAnchorPosition = this.findFirstVisibleItemPositionInt();
                state.mSpanOffsetsSize = this.mSpanCount;
                state.mSpanOffsets = new int[this.mSpanCount];

                for(int i = 0; i < this.mSpanCount; ++i) {
                    int line;
                    if (this.mLastLayoutFromEnd) {
                        line = this.mSpans[i].getEndLine(-2147483648);
                        if (line != -2147483648) {
                            line -= this.mPrimaryOrientation.getEndAfterPadding();
                        }
                    } else {
                        line = this.mSpans[i].getStartLine(-2147483648);
                        if (line != -2147483648) {
                            line -= this.mPrimaryOrientation.getStartAfterPadding();
                        }
                    }

                    state.mSpanOffsets[i] = line;
                }
            } else {
                state.mAnchorPosition = -1;
                state.mVisibleAnchorPosition = -1;
                state.mSpanOffsetsSize = 0;
            }

            return state;
        }
    }

    public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View host, AccessibilityNodeInfoCompat info) {
        android.view.ViewGroup.LayoutParams lp = host.getLayoutParams();
        if (!(lp instanceof StaggeredGridLayoutManager.LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(host, info);
        } else {
            StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams)lp;
            if (this.mOrientation == 0) {
                info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(sglp.getSpanIndex(), sglp.mFullSpan ? this.mSpanCount : 1, -1, -1, sglp.mFullSpan, false));
            } else {
                info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(-1, -1, sglp.getSpanIndex(), sglp.mFullSpan ? this.mSpanCount : 1, sglp.mFullSpan, false));
            }

        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (this.getChildCount() > 0) {
            View start = this.findFirstVisibleItemClosestToStart(false);
            View end = this.findFirstVisibleItemClosestToEnd(false);
            if (start == null || end == null) {
                return;
            }

            int startPos = this.getPosition(start);
            int endPos = this.getPosition(end);
            if (startPos < endPos) {
                event.setFromIndex(startPos);
                event.setToIndex(endPos);
            } else {
                event.setFromIndex(endPos);
                event.setToIndex(startPos);
            }
        }

    }

    int findFirstVisibleItemPositionInt() {
        View first = this.mShouldReverseLayout ? this.findFirstVisibleItemClosestToEnd(true) : this.findFirstVisibleItemClosestToStart(true);
        return first == null ? -1 : this.getPosition(first);
    }

    public int getRowCountForAccessibility(Recycler recycler, State state) {
        return this.mOrientation == 0 ? this.mSpanCount : super.getRowCountForAccessibility(recycler, state);
    }

    public int getColumnCountForAccessibility(Recycler recycler, State state) {
        return this.mOrientation == 1 ? this.mSpanCount : super.getColumnCountForAccessibility(recycler, state);
    }

    View findFirstVisibleItemClosestToStart(boolean fullyVisible) {
        int boundsStart = this.mPrimaryOrientation.getStartAfterPadding();
        int boundsEnd = this.mPrimaryOrientation.getEndAfterPadding();
        int limit = this.getChildCount();
        View partiallyVisible = null;

        for(int i = 0; i < limit; ++i) {
            View child = this.getChildAt(i);
            int childStart = this.mPrimaryOrientation.getDecoratedStart(child);
            int childEnd = this.mPrimaryOrientation.getDecoratedEnd(child);
            if (childEnd > boundsStart && childStart < boundsEnd) {
                if (childStart >= boundsStart || !fullyVisible) {
                    return child;
                }

                if (partiallyVisible == null) {
                    partiallyVisible = child;
                }
            }
        }

        return partiallyVisible;
    }

    View findFirstVisibleItemClosestToEnd(boolean fullyVisible) {
        int boundsStart = this.mPrimaryOrientation.getStartAfterPadding();
        int boundsEnd = this.mPrimaryOrientation.getEndAfterPadding();
        View partiallyVisible = null;

        for(int i = this.getChildCount() - 1; i >= 0; --i) {
            View child = this.getChildAt(i);
            int childStart = this.mPrimaryOrientation.getDecoratedStart(child);
            int childEnd = this.mPrimaryOrientation.getDecoratedEnd(child);
            if (childEnd > boundsStart && childStart < boundsEnd) {
                if (childEnd <= boundsEnd || !fullyVisible) {
                    return child;
                }

                if (partiallyVisible == null) {
                    partiallyVisible = child;
                }
            }
        }

        return partiallyVisible;
    }

    private void fixEndGap(Recycler recycler, State state, boolean canOffsetChildren) {
        int maxEndLine = this.getMaxEnd(-2147483648);
        if (maxEndLine != -2147483648) {
            int gap = this.mPrimaryOrientation.getEndAfterPadding() - maxEndLine;
            if (gap > 0) {
                int fixOffset = -this.scrollBy(-gap, recycler, state);
                gap -= fixOffset;
                if (canOffsetChildren && gap > 0) {
                    this.mPrimaryOrientation.offsetChildren(gap);
                }

            }
        }
    }

    private void fixStartGap(Recycler recycler, State state, boolean canOffsetChildren) {
        int minStartLine = this.getMinStart(2147483647);
        if (minStartLine != 2147483647) {
            int gap = minStartLine - this.mPrimaryOrientation.getStartAfterPadding();
            if (gap > 0) {
                int fixOffset = this.scrollBy(gap, recycler, state);
                gap -= fixOffset;
                if (canOffsetChildren && gap > 0) {
                    this.mPrimaryOrientation.offsetChildren(-gap);
                }

            }
        }
    }

    private void updateLayoutState(int anchorPosition, State state) {
        this.mLayoutState.mAvailable = 0;
        this.mLayoutState.mCurrentPosition = anchorPosition;
        int startExtra = 0;
        int endExtra = 0;
        if (this.isSmoothScrolling()) {
            int targetPos = state.getTargetScrollPosition();
            if (targetPos != -1) {
                if (this.mShouldReverseLayout == targetPos < anchorPosition) {
                    endExtra = this.mPrimaryOrientation.getTotalSpace();
                } else {
                    startExtra = this.mPrimaryOrientation.getTotalSpace();
                }
            }
        }

        boolean clipToPadding = this.getClipToPadding();
        if (clipToPadding) {
            this.mLayoutState.mStartLine = this.mPrimaryOrientation.getStartAfterPadding() - startExtra;
            this.mLayoutState.mEndLine = this.mPrimaryOrientation.getEndAfterPadding() + endExtra;
        } else {
            this.mLayoutState.mEndLine = this.mPrimaryOrientation.getEnd() + endExtra;
            this.mLayoutState.mStartLine = -startExtra;
        }

        this.mLayoutState.mStopInFocusable = false;
        this.mLayoutState.mRecycle = true;
        this.mLayoutState.mInfinite = this.mPrimaryOrientation.getMode() == 0 && this.mPrimaryOrientation.getEnd() == 0;
    }

    private void setLayoutStateDirection(int direction) {
        this.mLayoutState.mLayoutDirection = direction;
        this.mLayoutState.mItemDirection = this.mShouldReverseLayout == (direction == -1) ? 1 : -1;
    }

    public void offsetChildrenHorizontal(int dx) {
        super.offsetChildrenHorizontal(dx);

        for(int i = 0; i < this.mSpanCount; ++i) {
            this.mSpans[i].onOffset(dx);
        }

    }

    public void offsetChildrenVertical(int dy) {
        super.offsetChildrenVertical(dy);

        for(int i = 0; i < this.mSpanCount; ++i) {
            this.mSpans[i].onOffset(dy);
        }

    }

    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        this.handleUpdate(positionStart, itemCount, 2);
    }

    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        this.handleUpdate(positionStart, itemCount, 1);
    }

    public void onItemsChanged(RecyclerView recyclerView) {
        this.mLazySpanLookup.clear();
        this.requestLayout();
    }

    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        this.handleUpdate(from, to, 8);
    }

    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        this.handleUpdate(positionStart, itemCount, 4);
    }

    private void handleUpdate(int positionStart, int itemCountOrToPosition, int cmd) {
        int minPosition = this.mShouldReverseLayout ? this.getLastChildPosition() : this.getFirstChildPosition();
        int affectedRangeEnd;
        int affectedRangeStart;
        if (cmd == 8) {
            if (positionStart < itemCountOrToPosition) {
                affectedRangeEnd = itemCountOrToPosition + 1;
                affectedRangeStart = positionStart;
            } else {
                affectedRangeEnd = positionStart + 1;
                affectedRangeStart = itemCountOrToPosition;
            }
        } else {
            affectedRangeStart = positionStart;
            affectedRangeEnd = positionStart + itemCountOrToPosition;
        }

        this.mLazySpanLookup.invalidateAfter(affectedRangeStart);
        switch(cmd) {
            case 1:
                this.mLazySpanLookup.offsetForAddition(positionStart, itemCountOrToPosition);
                break;
            case 2:
                this.mLazySpanLookup.offsetForRemoval(positionStart, itemCountOrToPosition);
                break;
            case 8:
                this.mLazySpanLookup.offsetForRemoval(positionStart, 1);
                this.mLazySpanLookup.offsetForAddition(itemCountOrToPosition, 1);
        }

        if (affectedRangeEnd > minPosition) {
            int maxPosition = this.mShouldReverseLayout ? this.getFirstChildPosition() : this.getLastChildPosition();
            if (affectedRangeStart <= maxPosition) {
                this.requestLayout();
            }

        }
    }

    private int fill(Recycler recycler, LayoutState layoutState, State state) {
        this.mRemainingSpans.set(0, this.mSpanCount, true);
        int targetLine;
        if (this.mLayoutState.mInfinite) {
            if (layoutState.mLayoutDirection == 1) {
                targetLine = 2147483647;
            } else {
                targetLine = -2147483648;
            }
        } else if (layoutState.mLayoutDirection == 1) {
            targetLine = layoutState.mEndLine + layoutState.mAvailable;
        } else {
            targetLine = layoutState.mStartLine - layoutState.mAvailable;
        }

        this.updateAllRemainingSpans(layoutState.mLayoutDirection, targetLine);
        int defaultNewViewLine = this.mShouldReverseLayout ? this.mPrimaryOrientation.getEndAfterPadding() : this.mPrimaryOrientation.getStartAfterPadding();

        boolean added;
        for(added = false; layoutState.hasMore(state) && (this.mLayoutState.mInfinite || !this.mRemainingSpans.isEmpty()); added = true) {
            View view = layoutState.next(recycler);
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)view.getLayoutParams();
            int position = lp.getViewLayoutPosition();
            int spanIndex = this.mLazySpanLookup.getSpan(position);
            boolean assignSpan = spanIndex == -1;
            StaggeredGridLayoutManager.Span currentSpan;
            if (assignSpan) {
                currentSpan = lp.mFullSpan ? this.mSpans[0] : this.getNextSpan(layoutState);
                this.mLazySpanLookup.setSpan(position, currentSpan);
            } else {
                currentSpan = this.mSpans[spanIndex];
            }

            lp.mSpan = currentSpan;
            if (layoutState.mLayoutDirection == 1) {
                this.addView(view);
            } else {
                this.addView(view, 0);
            }

            this.measureChildWithDecorationsAndMargin(view, lp, false);
            int start;
            int end;
            StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fullSpanItem;
            if (layoutState.mLayoutDirection == 1) {
                start = lp.mFullSpan ? this.getMaxEnd(defaultNewViewLine) : currentSpan.getEndLine(defaultNewViewLine);
                end = start + this.mPrimaryOrientation.getDecoratedMeasurement(view);
                if (assignSpan && lp.mFullSpan) {
                    fullSpanItem = this.createFullSpanItemFromEnd(start);
                    fullSpanItem.mGapDir = -1;
                    fullSpanItem.mPosition = position;
                    this.mLazySpanLookup.addFullSpanItem(fullSpanItem);
                }
            } else {
                end = lp.mFullSpan ? this.getMinStart(defaultNewViewLine) : currentSpan.getStartLine(defaultNewViewLine);
                start = end - this.mPrimaryOrientation.getDecoratedMeasurement(view);
                if (assignSpan && lp.mFullSpan) {
                    fullSpanItem = this.createFullSpanItemFromStart(end);
                    fullSpanItem.mGapDir = 1;
                    fullSpanItem.mPosition = position;
                    this.mLazySpanLookup.addFullSpanItem(fullSpanItem);
                }
            }

            if (lp.mFullSpan && layoutState.mItemDirection == -1) {
                if (assignSpan) {
                    this.mLaidOutInvalidFullSpan = true;
                } else {
                    boolean hasInvalidGap;
                    if (layoutState.mLayoutDirection == 1) {
                        hasInvalidGap = !this.areAllEndsEqual();
                    } else {
                        hasInvalidGap = !this.areAllStartsEqual();
                    }

                    if (hasInvalidGap) {
                        fullSpanItem = this.mLazySpanLookup.getFullSpanItem(position);
                        if (fullSpanItem != null) {
                            fullSpanItem.mHasUnwantedGapAfter = true;
                        }

                        this.mLaidOutInvalidFullSpan = true;
                    }
                }
            }

            this.attachViewToSpans(view, lp, layoutState);
            int otherEnd;
            int otherStart;
            if (this.isLayoutRTL() && this.mOrientation == 1) {
                otherEnd = lp.mFullSpan ? this.mSecondaryOrientation.getEndAfterPadding() : this.mSecondaryOrientation.getEndAfterPadding() - (this.mSpanCount - 1 - currentSpan.mIndex) * this.mSizePerSpan;
                otherStart = otherEnd - this.mSecondaryOrientation.getDecoratedMeasurement(view);
            } else {
                otherStart = lp.mFullSpan ? this.mSecondaryOrientation.getStartAfterPadding() : currentSpan.mIndex * this.mSizePerSpan + this.mSecondaryOrientation.getStartAfterPadding();
                otherEnd = otherStart + this.mSecondaryOrientation.getDecoratedMeasurement(view);
            }

            if (this.mOrientation == 1) {
                this.layoutDecoratedWithMargins(view, otherStart, start, otherEnd, end);
            } else {
                this.layoutDecoratedWithMargins(view, start, otherStart, end, otherEnd);
            }

            if (lp.mFullSpan) {
                this.updateAllRemainingSpans(this.mLayoutState.mLayoutDirection, targetLine);
            } else {
                this.updateRemainingSpans(currentSpan, this.mLayoutState.mLayoutDirection, targetLine);
            }

            this.recycle(recycler, this.mLayoutState);
            if (this.mLayoutState.mStopInFocusable && view.hasFocusable()) {
                if (lp.mFullSpan) {
                    this.mRemainingSpans.clear();
                } else {
                    this.mRemainingSpans.set(currentSpan.mIndex, false);
                }
            }
        }

        if (!added) {
            this.recycle(recycler, this.mLayoutState);
        }

        int diff;
        int minStart;
        if (this.mLayoutState.mLayoutDirection == -1) {
            minStart = this.getMinStart(this.mPrimaryOrientation.getStartAfterPadding());
            diff = this.mPrimaryOrientation.getStartAfterPadding() - minStart;
        } else {
            minStart = this.getMaxEnd(this.mPrimaryOrientation.getEndAfterPadding());
            diff = minStart - this.mPrimaryOrientation.getEndAfterPadding();
        }

        return diff > 0 ? Math.min(layoutState.mAvailable, diff) : 0;
    }

    private StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem createFullSpanItemFromEnd(int newItemTop) {
        StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = new StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem();
        fsi.mGapPerSpan = new int[this.mSpanCount];

        for(int i = 0; i < this.mSpanCount; ++i) {
            fsi.mGapPerSpan[i] = newItemTop - this.mSpans[i].getEndLine(newItemTop);
        }

        return fsi;
    }

    private StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem createFullSpanItemFromStart(int newItemBottom) {
        StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = new StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem();
        fsi.mGapPerSpan = new int[this.mSpanCount];

        for(int i = 0; i < this.mSpanCount; ++i) {
            fsi.mGapPerSpan[i] = this.mSpans[i].getStartLine(newItemBottom) - newItemBottom;
        }

        return fsi;
    }

    private void attachViewToSpans(View view, StaggeredGridLayoutManager.LayoutParams lp, LayoutState layoutState) {
        if (layoutState.mLayoutDirection == 1) {
            if (lp.mFullSpan) {
                this.appendViewToAllSpans(view);
            } else {
                lp.mSpan.appendToSpan(view);
            }
        } else if (lp.mFullSpan) {
            this.prependViewToAllSpans(view);
        } else {
            lp.mSpan.prependToSpan(view);
        }

    }

    private void recycle(Recycler recycler, LayoutState layoutState) {
        if (layoutState.mRecycle && !layoutState.mInfinite) {
            if (layoutState.mAvailable == 0) {
                if (layoutState.mLayoutDirection == -1) {
                    this.recycleFromEnd(recycler, layoutState.mEndLine);
                } else {
                    this.recycleFromStart(recycler, layoutState.mStartLine);
                }
            } else {
                int scrolled;
                int line;
                if (layoutState.mLayoutDirection == -1) {
                    scrolled = layoutState.mStartLine - this.getMaxStart(layoutState.mStartLine);
                    if (scrolled < 0) {
                        line = layoutState.mEndLine;
                    } else {
                        line = layoutState.mEndLine - Math.min(scrolled, layoutState.mAvailable);
                    }

                    this.recycleFromEnd(recycler, line);
                } else {
                    scrolled = this.getMinEnd(layoutState.mEndLine) - layoutState.mEndLine;
                    if (scrolled < 0) {
                        line = layoutState.mStartLine;
                    } else {
                        line = layoutState.mStartLine + Math.min(scrolled, layoutState.mAvailable);
                    }

                    this.recycleFromStart(recycler, line);
                }
            }

        }
    }

    private void appendViewToAllSpans(View view) {
        for(int i = this.mSpanCount - 1; i >= 0; --i) {
            this.mSpans[i].appendToSpan(view);
        }

    }

    private void prependViewToAllSpans(View view) {
        for(int i = this.mSpanCount - 1; i >= 0; --i) {
            this.mSpans[i].prependToSpan(view);
        }

    }

    private void updateAllRemainingSpans(int layoutDir, int targetLine) {
        for(int i = 0; i < this.mSpanCount; ++i) {
            if (!this.mSpans[i].mViews.isEmpty()) {
                this.updateRemainingSpans(this.mSpans[i], layoutDir, targetLine);
            }
        }

    }

    private void updateRemainingSpans(StaggeredGridLayoutManager.Span span, int layoutDir, int targetLine) {
        int deletedSize = span.getDeletedSize();
        int line;
        if (layoutDir == -1) {
            line = span.getStartLine();
            if (line + deletedSize <= targetLine) {
                this.mRemainingSpans.set(span.mIndex, false);
            }
        } else {
            line = span.getEndLine();
            if (line - deletedSize >= targetLine) {
                this.mRemainingSpans.set(span.mIndex, false);
            }
        }

    }

    private int getMaxStart(int def) {
        int maxStart = this.mSpans[0].getStartLine(def);

        for(int i = 1; i < this.mSpanCount; ++i) {
            int spanStart = this.mSpans[i].getStartLine(def);
            if (spanStart > maxStart) {
                maxStart = spanStart;
            }
        }

        return maxStart;
    }

    private int getMinStart(int def) {
        int minStart = this.mSpans[0].getStartLine(def);

        for(int i = 1; i < this.mSpanCount; ++i) {
            int spanStart = this.mSpans[i].getStartLine(def);
            if (spanStart < minStart) {
                minStart = spanStart;
            }
        }

        return minStart;
    }

    boolean areAllEndsEqual() {
        int end = this.mSpans[0].getEndLine(-2147483648);

        for(int i = 1; i < this.mSpanCount; ++i) {
            if (this.mSpans[i].getEndLine(-2147483648) != end) {
                return false;
            }
        }

        return true;
    }

    boolean areAllStartsEqual() {
        int start = this.mSpans[0].getStartLine(-2147483648);

        for(int i = 1; i < this.mSpanCount; ++i) {
            if (this.mSpans[i].getStartLine(-2147483648) != start) {
                return false;
            }
        }

        return true;
    }

    private int getMaxEnd(int def) {
        int maxEnd = this.mSpans[0].getEndLine(def);

        for(int i = 1; i < this.mSpanCount; ++i) {
            int spanEnd = this.mSpans[i].getEndLine(def);
            if (spanEnd > maxEnd) {
                maxEnd = spanEnd;
            }
        }

        return maxEnd;
    }

    private int getMinEnd(int def) {
        int minEnd = this.mSpans[0].getEndLine(def);

        for(int i = 1; i < this.mSpanCount; ++i) {
            int spanEnd = this.mSpans[i].getEndLine(def);
            if (spanEnd < minEnd) {
                minEnd = spanEnd;
            }
        }

        return minEnd;
    }

    private void recycleFromStart(Recycler recycler, int line) {
        View child;
        for(; this.getChildCount() > 0; this.removeAndRecycleView(child, recycler)) {
            child = this.getChildAt(0);
            if (this.mPrimaryOrientation.getDecoratedEnd(child) > line || this.mPrimaryOrientation.getTransformedEndWithDecoration(child) > line) {
                return;
            }

            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)child.getLayoutParams();
            if (lp.mFullSpan) {
                int j;
                for(j = 0; j < this.mSpanCount; ++j) {
                    if (this.mSpans[j].mViews.size() == 1) {
                        return;
                    }
                }

                for(j = 0; j < this.mSpanCount; ++j) {
                    this.mSpans[j].popStart();
                }
            } else {
                if (lp.mSpan.mViews.size() == 1) {
                    return;
                }

                lp.mSpan.popStart();
            }
        }

    }

    private void recycleFromEnd(Recycler recycler, int line) {
        int childCount = this.getChildCount();

        for(int i = childCount - 1; i >= 0; --i) {
            View child = this.getChildAt(i);
            if (this.mPrimaryOrientation.getDecoratedStart(child) < line || this.mPrimaryOrientation.getTransformedStartWithDecoration(child) < line) {
                return;
            }

            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams)child.getLayoutParams();
            if (lp.mFullSpan) {
                int j;
                for(j = 0; j < this.mSpanCount; ++j) {
                    if (this.mSpans[j].mViews.size() == 1) {
                        return;
                    }
                }

                for(j = 0; j < this.mSpanCount; ++j) {
                    this.mSpans[j].popEnd();
                }
            } else {
                if (lp.mSpan.mViews.size() == 1) {
                    return;
                }

                lp.mSpan.popEnd();
            }

            this.removeAndRecycleView(child, recycler);
        }

    }

    private boolean preferLastSpan(int layoutDir) {
        if (this.mOrientation == 0) {
            return layoutDir == -1 != this.mShouldReverseLayout;
        } else {
            return layoutDir == -1 == this.mShouldReverseLayout == this.isLayoutRTL();
        }
    }

    private StaggeredGridLayoutManager.Span getNextSpan(LayoutState layoutState) {
        boolean preferLastSpan = this.preferLastSpan(layoutState.mLayoutDirection);
        int startIndex;
        int endIndex;
        byte diff;
        if (preferLastSpan) {
            startIndex = this.mSpanCount - 1;
            endIndex = -1;
            diff = -1;
        } else {
            startIndex = 0;
            endIndex = this.mSpanCount;
            diff = 1;
        }

        StaggeredGridLayoutManager.Span max;
        int maxLine;
        int defaultLine;
        int i;
        StaggeredGridLayoutManager.Span other;
        int otherLine;
        if (layoutState.mLayoutDirection == 1) {
            max = null;
            maxLine = 2147483647;
            defaultLine = this.mPrimaryOrientation.getStartAfterPadding();

            for(i = startIndex; i != endIndex; i += diff) {
                other = this.mSpans[i];
                otherLine = other.getEndLine(defaultLine);
                if (otherLine < maxLine) {
                    max = other;
                    maxLine = otherLine;
                }
            }

            return max;
        } else {
            max = null;
            maxLine = -2147483648;
            defaultLine = this.mPrimaryOrientation.getEndAfterPadding();

            for(i = startIndex; i != endIndex; i += diff) {
                other = this.mSpans[i];
                otherLine = other.getStartLine(defaultLine);
                if (otherLine > maxLine) {
                    max = other;
                    maxLine = otherLine;
                }
            }

            return max;
        }
    }

    public boolean canScrollVertically() {
        return this.mOrientation == 1;
    }

    public boolean canScrollHorizontally() {
        return this.mOrientation == 0;
    }

    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        return this.scrollBy(dx, recycler, state);
    }

    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        return this.scrollBy(dy, recycler, state);
    }

    private int calculateScrollDirectionForPosition(int position) {
        if (this.getChildCount() == 0) {
            return this.mShouldReverseLayout ? 1 : -1;
        } else {
            int firstChildPos = this.getFirstChildPosition();
            return position < firstChildPos != this.mShouldReverseLayout ? -1 : 1;
        }
    }

    public PointF computeScrollVectorForPosition(int targetPosition) {
        int direction = this.calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        } else {
            if (this.mOrientation == 0) {
                outVector.x = (float)direction;
                outVector.y = 0.0F;
            } else {
                outVector.x = 0.0F;
                outVector.y = (float)direction;
            }

            return outVector;
        }
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext());
        scroller.setTargetPosition(position);
        this.startSmoothScroll(scroller);
    }

    public void scrollToPosition(int position) {
        if (this.mPendingSavedState != null && this.mPendingSavedState.mAnchorPosition != position) {
            this.mPendingSavedState.invalidateAnchorPositionInfo();
        }

        this.mPendingScrollPosition = position;
        this.mPendingScrollPositionOffset = -2147483648;
        this.requestLayout();
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        if (this.mPendingSavedState != null) {
            this.mPendingSavedState.invalidateAnchorPositionInfo();
        }

        this.mPendingScrollPosition = position;
        this.mPendingScrollPositionOffset = offset;
        this.requestLayout();
    }

    @RestrictTo({Scope.LIBRARY})
    public void collectAdjacentPrefetchPositions(int dx, int dy, State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int delta = this.mOrientation == 0 ? dx : dy;
        if (this.getChildCount() != 0 && delta != 0) {
            this.prepareLayoutStateForDelta(delta, state);
            if (this.mPrefetchDistances == null || this.mPrefetchDistances.length < this.mSpanCount) {
                this.mPrefetchDistances = new int[this.mSpanCount];
            }

            int itemPrefetchCount = 0;

            int i;
            for(i = 0; i < this.mSpanCount; ++i) {
                int distance = this.mLayoutState.mItemDirection == -1 ? this.mLayoutState.mStartLine - this.mSpans[i].getStartLine(this.mLayoutState.mStartLine) : this.mSpans[i].getEndLine(this.mLayoutState.mEndLine) - this.mLayoutState.mEndLine;
                if (distance >= 0) {
                    this.mPrefetchDistances[itemPrefetchCount] = distance;
                    ++itemPrefetchCount;
                }
            }

            Arrays.sort(this.mPrefetchDistances, 0, itemPrefetchCount);

            for(i = 0; i < itemPrefetchCount && this.mLayoutState.hasMore(state); ++i) {
                layoutPrefetchRegistry.addPosition(this.mLayoutState.mCurrentPosition, this.mPrefetchDistances[i]);
                LayoutState var10000 = this.mLayoutState;
                var10000.mCurrentPosition += this.mLayoutState.mItemDirection;
            }

        }
    }

    void prepareLayoutStateForDelta(int delta, State state) {
        int referenceChildPosition;
        byte layoutDir;
        if (delta > 0) {
            layoutDir = 1;
            referenceChildPosition = this.getLastChildPosition();
        } else {
            layoutDir = -1;
            referenceChildPosition = this.getFirstChildPosition();
        }

        this.mLayoutState.mRecycle = true;
        this.updateLayoutState(referenceChildPosition, state);
        this.setLayoutStateDirection(layoutDir);
        this.mLayoutState.mCurrentPosition = referenceChildPosition + this.mLayoutState.mItemDirection;
        this.mLayoutState.mAvailable = Math.abs(delta);
    }

    int scrollBy(int dt, Recycler recycler, State state) {
        if (this.getChildCount() != 0 && dt != 0) {
            this.prepareLayoutStateForDelta(dt, state);
            int consumed = this.fill(recycler, this.mLayoutState, state);
            int available = this.mLayoutState.mAvailable;
            int totalScroll;
            if (available < consumed) {
                totalScroll = dt;
            } else if (dt < 0) {
                totalScroll = -consumed;
            } else {
                totalScroll = consumed;
            }

            this.mPrimaryOrientation.offsetChildren(-totalScroll);
            this.mLastLayoutFromEnd = this.mShouldReverseLayout;
            this.mLayoutState.mAvailable = 0;
            this.recycle(recycler, this.mLayoutState);
            return totalScroll;
        } else {
            return 0;
        }
    }

    int getLastChildPosition() {
        int childCount = this.getChildCount();
        return childCount == 0 ? 0 : this.getPosition(this.getChildAt(childCount - 1));
    }

    int getFirstChildPosition() {
        int childCount = this.getChildCount();
        return childCount == 0 ? 0 : this.getPosition(this.getChildAt(0));
    }

    private int findFirstReferenceChildPosition(int itemCount) {
        int limit = this.getChildCount();

        for(int i = 0; i < limit; ++i) {
            View view = this.getChildAt(i);
            int position = this.getPosition(view);
            if (position >= 0 && position < itemCount) {
                return position;
            }
        }

        return 0;
    }

    private int findLastReferenceChildPosition(int itemCount) {
        for(int i = this.getChildCount() - 1; i >= 0; --i) {
            View view = this.getChildAt(i);
            int position = this.getPosition(view);
            if (position >= 0 && position < itemCount) {
                return position;
            }
        }

        return 0;
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return this.mOrientation == 0 ? new StaggeredGridLayoutManager.LayoutParams(-2, -1) : new StaggeredGridLayoutManager.LayoutParams(-1, -2);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new StaggeredGridLayoutManager.LayoutParams(c, attrs);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        return lp instanceof MarginLayoutParams ? new StaggeredGridLayoutManager.LayoutParams((MarginLayoutParams)lp) : new StaggeredGridLayoutManager.LayoutParams(lp);
    }

    public boolean checkLayoutParams(android.support.v7.widget.RecyclerView.LayoutParams lp) {
        return lp instanceof StaggeredGridLayoutManager.LayoutParams;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    @Nullable
    public View onFocusSearchFailed(View focused, int direction, Recycler recycler, State state) {
        if (this.getChildCount() == 0) {
            return null;
        } else {
            View directChild = this.findContainingItemView(focused);
            if (directChild == null) {
                return null;
            } else {
                this.resolveShouldLayoutReverse();
                int layoutDir = this.convertFocusDirectionToLayoutDirection(direction);
                if (layoutDir == -2147483648) {
                    return null;
                } else {
                    StaggeredGridLayoutManager.LayoutParams prevFocusLayoutParams = (StaggeredGridLayoutManager.LayoutParams)directChild.getLayoutParams();
                    boolean prevFocusFullSpan = prevFocusLayoutParams.mFullSpan;
                    StaggeredGridLayoutManager.Span prevFocusSpan = prevFocusLayoutParams.mSpan;
                    int referenceChildPosition;
                    if (layoutDir == 1) {
                        referenceChildPosition = this.getLastChildPosition();
                    } else {
                        referenceChildPosition = this.getFirstChildPosition();
                    }

                    this.updateLayoutState(referenceChildPosition, state);
                    this.setLayoutStateDirection(layoutDir);
                    this.mLayoutState.mCurrentPosition = referenceChildPosition + this.mLayoutState.mItemDirection;
                    this.mLayoutState.mAvailable = (int)(0.33333334F * (float)this.mPrimaryOrientation.getTotalSpace());
                    this.mLayoutState.mStopInFocusable = true;
                    this.mLayoutState.mRecycle = false;
                    this.fill(recycler, this.mLayoutState, state);
                    this.mLastLayoutFromEnd = this.mShouldReverseLayout;
                    if (!prevFocusFullSpan) {
                        View view = prevFocusSpan.getFocusableViewAfter(referenceChildPosition, layoutDir);
                        if (view != null && view != directChild) {
                            return view;
                        }
                    }

                    View unfocusableCandidate;
                    int i;
                    if (this.preferLastSpan(layoutDir)) {
                        for(i = this.mSpanCount - 1; i >= 0; --i) {
                            unfocusableCandidate = this.mSpans[i].getFocusableViewAfter(referenceChildPosition, layoutDir);
                            if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                                return unfocusableCandidate;
                            }
                        }
                    } else {
                        for(i = 0; i < this.mSpanCount; ++i) {
                            unfocusableCandidate = this.mSpans[i].getFocusableViewAfter(referenceChildPosition, layoutDir);
                            if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                                return unfocusableCandidate;
                            }
                        }
                    }

                    boolean shouldSearchFromStart = !this.mReverseLayout == (layoutDir == -1);
                    unfocusableCandidate = null;
                    if (!prevFocusFullSpan) {
                        unfocusableCandidate = this.findViewByPosition(shouldSearchFromStart ? prevFocusSpan.findFirstPartiallyVisibleItemPosition() : prevFocusSpan.findLastPartiallyVisibleItemPosition());
                        if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                            return unfocusableCandidate;
                        }
                    }

                    if (this.preferLastSpan(layoutDir)) {
                        for(i = this.mSpanCount - 1; i >= 0; --i) {
                            if (i != prevFocusSpan.mIndex) {
                                unfocusableCandidate = this.findViewByPosition(shouldSearchFromStart ? this.mSpans[i].findFirstPartiallyVisibleItemPosition() : this.mSpans[i].findLastPartiallyVisibleItemPosition());
                                if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                                    return unfocusableCandidate;
                                }
                            }
                        }
                    } else {
                        for(i = 0; i < this.mSpanCount; ++i) {
                            unfocusableCandidate = this.findViewByPosition(shouldSearchFromStart ? this.mSpans[i].findFirstPartiallyVisibleItemPosition() : this.mSpans[i].findLastPartiallyVisibleItemPosition());
                            if (unfocusableCandidate != null && unfocusableCandidate != directChild) {
                                return unfocusableCandidate;
                            }
                        }
                    }

                    return null;
                }
            }
        }
    }

    private int convertFocusDirectionToLayoutDirection(int focusDirection) {
        switch(focusDirection) {
            case 1:
                if (this.mOrientation == 1) {
                    return -1;
                } else {
                    if (this.isLayoutRTL()) {
                        return 1;
                    }

                    return -1;
                }
            case 2:
                if (this.mOrientation == 1) {
                    return 1;
                } else {
                    if (this.isLayoutRTL()) {
                        return -1;
                    }

                    return 1;
                }
            case 17:
                return this.mOrientation == 0 ? -1 : -2147483648;
            case 33:
                return this.mOrientation == 1 ? -1 : -2147483648;
            case 66:
                return this.mOrientation == 0 ? 1 : -2147483648;
            case 130:
                return this.mOrientation == 1 ? 1 : -2147483648;
            default:
                return -2147483648;
        }
    }

    class AnchorInfo {
        int mPosition;
        int mOffset;
        boolean mLayoutFromEnd;
        boolean mInvalidateOffsets;
        boolean mValid;
        int[] mSpanReferenceLines;

        AnchorInfo() {
            this.reset();
        }

        void reset() {
            this.mPosition = -1;
            this.mOffset = -2147483648;
            this.mLayoutFromEnd = false;
            this.mInvalidateOffsets = false;
            this.mValid = false;
            if (this.mSpanReferenceLines != null) {
                Arrays.fill(this.mSpanReferenceLines, -1);
            }

        }

        void saveSpanReferenceLines(StaggeredGridLayoutManager.Span[] spans) {
            int spanCount = spans.length;
            if (this.mSpanReferenceLines == null || this.mSpanReferenceLines.length < spanCount) {
                this.mSpanReferenceLines = new int[StaggeredGridLayoutManager.this.mSpans.length];
            }

            for(int i = 0; i < spanCount; ++i) {
                this.mSpanReferenceLines[i] = spans[i].getStartLine(-2147483648);
            }

        }

        void assignCoordinateFromPadding() {
            this.mOffset = this.mLayoutFromEnd ? StaggeredGridLayoutManager.this.mPrimaryOrientation.getEndAfterPadding() : StaggeredGridLayoutManager.this.mPrimaryOrientation.getStartAfterPadding();
        }

        void assignCoordinateFromPadding(int addedDistance) {
            if (this.mLayoutFromEnd) {
                this.mOffset = StaggeredGridLayoutManager.this.mPrimaryOrientation.getEndAfterPadding() - addedDistance;
            } else {
                this.mOffset = StaggeredGridLayoutManager.this.mPrimaryOrientation.getStartAfterPadding() + addedDistance;
            }

        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static class SavedState implements Parcelable {
        int mAnchorPosition;
        int mVisibleAnchorPosition;
        int mSpanOffsetsSize;
        int[] mSpanOffsets;
        int mSpanLookupSize;
        int[] mSpanLookup;
        List<StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem> mFullSpanItems;
        boolean mReverseLayout;
        boolean mAnchorLayoutFromEnd;
        boolean mLastLayoutRTL;
        public static final Creator<StaggeredGridLayoutManager.SavedState> CREATOR = new Creator<StaggeredGridLayoutManager.SavedState>() {
            public StaggeredGridLayoutManager.SavedState createFromParcel(Parcel in) {
                return new StaggeredGridLayoutManager.SavedState(in);
            }

            public StaggeredGridLayoutManager.SavedState[] newArray(int size) {
                return new StaggeredGridLayoutManager.SavedState[size];
            }
        };

        public SavedState() {
        }

        SavedState(Parcel in) {
            this.mAnchorPosition = in.readInt();
            this.mVisibleAnchorPosition = in.readInt();
            this.mSpanOffsetsSize = in.readInt();
            if (this.mSpanOffsetsSize > 0) {
                this.mSpanOffsets = new int[this.mSpanOffsetsSize];
                in.readIntArray(this.mSpanOffsets);
            }

            this.mSpanLookupSize = in.readInt();
            if (this.mSpanLookupSize > 0) {
                this.mSpanLookup = new int[this.mSpanLookupSize];
                in.readIntArray(this.mSpanLookup);
            }

            this.mReverseLayout = in.readInt() == 1;
            this.mAnchorLayoutFromEnd = in.readInt() == 1;
            this.mLastLayoutRTL = in.readInt() == 1;
            this.mFullSpanItems = in.readArrayList(StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem.class.getClassLoader());
        }

        public SavedState(StaggeredGridLayoutManager.SavedState other) {
            this.mSpanOffsetsSize = other.mSpanOffsetsSize;
            this.mAnchorPosition = other.mAnchorPosition;
            this.mVisibleAnchorPosition = other.mVisibleAnchorPosition;
            this.mSpanOffsets = other.mSpanOffsets;
            this.mSpanLookupSize = other.mSpanLookupSize;
            this.mSpanLookup = other.mSpanLookup;
            this.mReverseLayout = other.mReverseLayout;
            this.mAnchorLayoutFromEnd = other.mAnchorLayoutFromEnd;
            this.mLastLayoutRTL = other.mLastLayoutRTL;
            this.mFullSpanItems = other.mFullSpanItems;
        }

        void invalidateSpanInfo() {
            this.mSpanOffsets = null;
            this.mSpanOffsetsSize = 0;
            this.mSpanLookupSize = 0;
            this.mSpanLookup = null;
            this.mFullSpanItems = null;
        }

        void invalidateAnchorPositionInfo() {
            this.mSpanOffsets = null;
            this.mSpanOffsetsSize = 0;
            this.mAnchorPosition = -1;
            this.mVisibleAnchorPosition = -1;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mAnchorPosition);
            dest.writeInt(this.mVisibleAnchorPosition);
            dest.writeInt(this.mSpanOffsetsSize);
            if (this.mSpanOffsetsSize > 0) {
                dest.writeIntArray(this.mSpanOffsets);
            }

            dest.writeInt(this.mSpanLookupSize);
            if (this.mSpanLookupSize > 0) {
                dest.writeIntArray(this.mSpanLookup);
            }

            dest.writeInt(this.mReverseLayout ? 1 : 0);
            dest.writeInt(this.mAnchorLayoutFromEnd ? 1 : 0);
            dest.writeInt(this.mLastLayoutRTL ? 1 : 0);
            dest.writeList(this.mFullSpanItems);
        }
    }

    static class LazySpanLookup {
        private static final int MIN_SIZE = 10;
        int[] mData;
        List<StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem> mFullSpanItems;

        LazySpanLookup() {
        }

        int forceInvalidateAfter(int position) {
            if (this.mFullSpanItems != null) {
                for(int i = this.mFullSpanItems.size() - 1; i >= 0; --i) {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(i);
                    if (fsi.mPosition >= position) {
                        this.mFullSpanItems.remove(i);
                    }
                }
            }

            return this.invalidateAfter(position);
        }

        int invalidateAfter(int position) {
            if (this.mData == null) {
                return -1;
            } else if (position >= this.mData.length) {
                return -1;
            } else {
                int endPosition = this.invalidateFullSpansAfter(position);
                if (endPosition == -1) {
                    Arrays.fill(this.mData, position, this.mData.length, -1);
                    return this.mData.length;
                } else {
                    Arrays.fill(this.mData, position, endPosition + 1, -1);
                    return endPosition + 1;
                }
            }
        }

        int getSpan(int position) {
            return this.mData != null && position < this.mData.length ? this.mData[position] : -1;
        }

        void setSpan(int position, StaggeredGridLayoutManager.Span span) {
            this.ensureSize(position);
            this.mData[position] = span.mIndex;
        }

        int sizeForPosition(int position) {
            int len;
            for(len = this.mData.length; len <= position; len *= 2) {
            }

            return len;
        }

        void ensureSize(int position) {
            if (this.mData == null) {
                this.mData = new int[Math.max(position, 10) + 1];
                Arrays.fill(this.mData, -1);
            } else if (position >= this.mData.length) {
                int[] old = this.mData;
                this.mData = new int[this.sizeForPosition(position)];
                System.arraycopy(old, 0, this.mData, 0, old.length);
                Arrays.fill(this.mData, old.length, this.mData.length, -1);
            }

        }

        void clear() {
            if (this.mData != null) {
                Arrays.fill(this.mData, -1);
            }

            this.mFullSpanItems = null;
        }

        void offsetForRemoval(int positionStart, int itemCount) {
            if (this.mData != null && positionStart < this.mData.length) {
                this.ensureSize(positionStart + itemCount);
                System.arraycopy(this.mData, positionStart + itemCount, this.mData, positionStart, this.mData.length - positionStart - itemCount);
                Arrays.fill(this.mData, this.mData.length - itemCount, this.mData.length, -1);
                this.offsetFullSpansForRemoval(positionStart, itemCount);
            }
        }

        private void offsetFullSpansForRemoval(int positionStart, int itemCount) {
            if (this.mFullSpanItems != null) {
                int end = positionStart + itemCount;

                for(int i = this.mFullSpanItems.size() - 1; i >= 0; --i) {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(i);
                    if (fsi.mPosition >= positionStart) {
                        if (fsi.mPosition < end) {
                            this.mFullSpanItems.remove(i);
                        } else {
                            fsi.mPosition -= itemCount;
                        }
                    }
                }

            }
        }

        void offsetForAddition(int positionStart, int itemCount) {
            if (this.mData != null && positionStart < this.mData.length) {
                this.ensureSize(positionStart + itemCount);
                System.arraycopy(this.mData, positionStart, this.mData, positionStart + itemCount, this.mData.length - positionStart - itemCount);
                Arrays.fill(this.mData, positionStart, positionStart + itemCount, -1);
                this.offsetFullSpansForAddition(positionStart, itemCount);
            }
        }

        private void offsetFullSpansForAddition(int positionStart, int itemCount) {
            if (this.mFullSpanItems != null) {
                for(int i = this.mFullSpanItems.size() - 1; i >= 0; --i) {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(i);
                    if (fsi.mPosition >= positionStart) {
                        fsi.mPosition += itemCount;
                    }
                }

            }
        }

        private int invalidateFullSpansAfter(int position) {
            if (this.mFullSpanItems == null) {
                return -1;
            } else {
                StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem item = this.getFullSpanItem(position);
                if (item != null) {
                    this.mFullSpanItems.remove(item);
                }

                int nextFsiIndex = -1;
                int count = this.mFullSpanItems.size();

                for(int i = 0; i < count; ++i) {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(i);
                    if (fsi.mPosition >= position) {
                        nextFsiIndex = i;
                        break;
                    }
                }

                if (nextFsiIndex != -1) {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(nextFsiIndex);
                    this.mFullSpanItems.remove(nextFsiIndex);
                    return fsi.mPosition;
                } else {
                    return -1;
                }
            }
        }

        public void addFullSpanItem(StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fullSpanItem) {
            if (this.mFullSpanItems == null) {
                this.mFullSpanItems = new ArrayList();
            }

            int size = this.mFullSpanItems.size();

            for(int i = 0; i < size; ++i) {
                StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem other = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(i);
                if (other.mPosition == fullSpanItem.mPosition) {
                    this.mFullSpanItems.remove(i);
                }

                if (other.mPosition >= fullSpanItem.mPosition) {
                    this.mFullSpanItems.add(i, fullSpanItem);
                    return;
                }
            }

            this.mFullSpanItems.add(fullSpanItem);
        }

        public StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem getFullSpanItem(int position) {
            if (this.mFullSpanItems == null) {
                return null;
            } else {
                for(int i = this.mFullSpanItems.size() - 1; i >= 0; --i) {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(i);
                    if (fsi.mPosition == position) {
                        return fsi;
                    }
                }

                return null;
            }
        }

        public StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem getFirstFullSpanItemInRange(int minPos, int maxPos, int gapDir, boolean hasUnwantedGapAfter) {
            if (this.mFullSpanItems == null) {
                return null;
            } else {
                int limit = this.mFullSpanItems.size();

                for(int i = 0; i < limit; ++i) {
                    StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = (StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem)this.mFullSpanItems.get(i);
                    if (fsi.mPosition >= maxPos) {
                        return null;
                    }

                    if (fsi.mPosition >= minPos && (gapDir == 0 || fsi.mGapDir == gapDir || hasUnwantedGapAfter && fsi.mHasUnwantedGapAfter)) {
                        return fsi;
                    }
                }

                return null;
            }
        }

        static class FullSpanItem implements Parcelable {
            int mPosition;
            int mGapDir;
            int[] mGapPerSpan;
            boolean mHasUnwantedGapAfter;
            public static final Creator<StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem> CREATOR = new Creator<StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem>() {
                public StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem createFromParcel(Parcel in) {
                    return new StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem(in);
                }

                public StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem[] newArray(int size) {
                    return new StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem[size];
                }
            };

            FullSpanItem(Parcel in) {
                this.mPosition = in.readInt();
                this.mGapDir = in.readInt();
                this.mHasUnwantedGapAfter = in.readInt() == 1;
                int spanCount = in.readInt();
                if (spanCount > 0) {
                    this.mGapPerSpan = new int[spanCount];
                    in.readIntArray(this.mGapPerSpan);
                }

            }

            FullSpanItem() {
            }

            int getGapForSpan(int spanIndex) {
                return this.mGapPerSpan == null ? 0 : this.mGapPerSpan[spanIndex];
            }

            public int describeContents() {
                return 0;
            }

            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(this.mPosition);
                dest.writeInt(this.mGapDir);
                dest.writeInt(this.mHasUnwantedGapAfter ? 1 : 0);
                if (this.mGapPerSpan != null && this.mGapPerSpan.length > 0) {
                    dest.writeInt(this.mGapPerSpan.length);
                    dest.writeIntArray(this.mGapPerSpan);
                } else {
                    dest.writeInt(0);
                }

            }

            public String toString() {
                return "FullSpanItem{mPosition=" + this.mPosition + ", mGapDir=" + this.mGapDir + ", mHasUnwantedGapAfter=" + this.mHasUnwantedGapAfter + ", mGapPerSpan=" + Arrays.toString(this.mGapPerSpan) + '}';
            }
        }
    }

    class Span {
        static final int INVALID_LINE = -2147483648;
        ArrayList<View> mViews = new ArrayList();
        int mCachedStart = -2147483648;
        int mCachedEnd = -2147483648;
        int mDeletedSize = 0;
        final int mIndex;

        Span(int index) {
            this.mIndex = index;
        }

        int getStartLine(int def) {
            if (this.mCachedStart != -2147483648) {
                return this.mCachedStart;
            } else if (this.mViews.size() == 0) {
                return def;
            } else {
                this.calculateCachedStart();
                return this.mCachedStart;
            }
        }

        void calculateCachedStart() {
            View startView = (View)this.mViews.get(0);
            StaggeredGridLayoutManager.LayoutParams lp = this.getLayoutParams(startView);
            this.mCachedStart = StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedStart(startView);
            if (lp.mFullSpan) {
                StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = StaggeredGridLayoutManager.this.mLazySpanLookup.getFullSpanItem(lp.getViewLayoutPosition());
                if (fsi != null && fsi.mGapDir == -1) {
                    this.mCachedStart -= fsi.getGapForSpan(this.mIndex);
                }
            }

        }

        int getStartLine() {
            if (this.mCachedStart != -2147483648) {
                return this.mCachedStart;
            } else {
                this.calculateCachedStart();
                return this.mCachedStart;
            }
        }

        int getEndLine(int def) {
            if (this.mCachedEnd != -2147483648) {
                return this.mCachedEnd;
            } else {
                int size = this.mViews.size();
                if (size == 0) {
                    return def;
                } else {
                    this.calculateCachedEnd();
                    return this.mCachedEnd;
                }
            }
        }

        void calculateCachedEnd() {
            View endView = (View)this.mViews.get(this.mViews.size() - 1);
            StaggeredGridLayoutManager.LayoutParams lp = this.getLayoutParams(endView);
            this.mCachedEnd = StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedEnd(endView);
            if (lp.mFullSpan) {
                StaggeredGridLayoutManager.LazySpanLookup.FullSpanItem fsi = StaggeredGridLayoutManager.this.mLazySpanLookup.getFullSpanItem(lp.getViewLayoutPosition());
                if (fsi != null && fsi.mGapDir == 1) {
                    this.mCachedEnd += fsi.getGapForSpan(this.mIndex);
                }
            }

        }

        int getEndLine() {
            if (this.mCachedEnd != -2147483648) {
                return this.mCachedEnd;
            } else {
                this.calculateCachedEnd();
                return this.mCachedEnd;
            }
        }

        void prependToSpan(View view) {
            StaggeredGridLayoutManager.LayoutParams lp = this.getLayoutParams(view);
            lp.mSpan = this;
            this.mViews.add(0, view);
            this.mCachedStart = -2147483648;
            if (this.mViews.size() == 1) {
                this.mCachedEnd = -2147483648;
            }

            if (lp.isItemRemoved() || lp.isItemChanged()) {
                this.mDeletedSize += StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedMeasurement(view);
            }

        }

        void appendToSpan(View view) {
            StaggeredGridLayoutManager.LayoutParams lp = this.getLayoutParams(view);
            lp.mSpan = this;
            this.mViews.add(view);
            this.mCachedEnd = -2147483648;
            if (this.mViews.size() == 1) {
                this.mCachedStart = -2147483648;
            }

            if (lp.isItemRemoved() || lp.isItemChanged()) {
                this.mDeletedSize += StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedMeasurement(view);
            }

        }

        void cacheReferenceLineAndClear(boolean reverseLayout, int offset) {
            int reference;
            if (reverseLayout) {
                reference = this.getEndLine(-2147483648);
            } else {
                reference = this.getStartLine(-2147483648);
            }

            this.clear();
            if (reference != -2147483648) {
                if ((!reverseLayout || reference >= StaggeredGridLayoutManager.this.mPrimaryOrientation.getEndAfterPadding()) && (reverseLayout || reference <= StaggeredGridLayoutManager.this.mPrimaryOrientation.getStartAfterPadding())) {
                    if (offset != -2147483648) {
                        reference += offset;
                    }

                    this.mCachedStart = this.mCachedEnd = reference;
                }
            }
        }

        void clear() {
            this.mViews.clear();
            this.invalidateCache();
            this.mDeletedSize = 0;
        }

        void invalidateCache() {
            this.mCachedStart = -2147483648;
            this.mCachedEnd = -2147483648;
        }

        void setLine(int line) {
            this.mCachedEnd = this.mCachedStart = line;
        }

        void popEnd() {
            int size = this.mViews.size();
            View end = (View)this.mViews.remove(size - 1);
            StaggeredGridLayoutManager.LayoutParams lp = this.getLayoutParams(end);
            lp.mSpan = null;
            if (lp.isItemRemoved() || lp.isItemChanged()) {
                this.mDeletedSize -= StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedMeasurement(end);
            }

            if (size == 1) {
                this.mCachedStart = -2147483648;
            }

            this.mCachedEnd = -2147483648;
        }

        void popStart() {
            View start = (View)this.mViews.remove(0);
            StaggeredGridLayoutManager.LayoutParams lp = this.getLayoutParams(start);
            lp.mSpan = null;
            if (this.mViews.size() == 0) {
                this.mCachedEnd = -2147483648;
            }

            if (lp.isItemRemoved() || lp.isItemChanged()) {
                this.mDeletedSize -= StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedMeasurement(start);
            }

            this.mCachedStart = -2147483648;
        }

        public int getDeletedSize() {
            return this.mDeletedSize;
        }

        StaggeredGridLayoutManager.LayoutParams getLayoutParams(View view) {
            return (StaggeredGridLayoutManager.LayoutParams)view.getLayoutParams();
        }

        void onOffset(int dt) {
            if (this.mCachedStart != -2147483648) {
                this.mCachedStart += dt;
            }

            if (this.mCachedEnd != -2147483648) {
                this.mCachedEnd += dt;
            }

        }

        public int findFirstVisibleItemPosition() {
            return StaggeredGridLayoutManager.this.mReverseLayout ? this.findOneVisibleChild(this.mViews.size() - 1, -1, false) : this.findOneVisibleChild(0, this.mViews.size(), false);
        }

        public int findFirstPartiallyVisibleItemPosition() {
            return StaggeredGridLayoutManager.this.mReverseLayout ? this.findOnePartiallyVisibleChild(this.mViews.size() - 1, -1, true) : this.findOnePartiallyVisibleChild(0, this.mViews.size(), true);
        }

        public int findFirstCompletelyVisibleItemPosition() {
            return StaggeredGridLayoutManager.this.mReverseLayout ? this.findOneVisibleChild(this.mViews.size() - 1, -1, true) : this.findOneVisibleChild(0, this.mViews.size(), true);
        }

        public int findLastVisibleItemPosition() {
            return StaggeredGridLayoutManager.this.mReverseLayout ? this.findOneVisibleChild(0, this.mViews.size(), false) : this.findOneVisibleChild(this.mViews.size() - 1, -1, false);
        }

        public int findLastPartiallyVisibleItemPosition() {
            return StaggeredGridLayoutManager.this.mReverseLayout ? this.findOnePartiallyVisibleChild(0, this.mViews.size(), true) : this.findOnePartiallyVisibleChild(this.mViews.size() - 1, -1, true);
        }

        public int findLastCompletelyVisibleItemPosition() {
            return StaggeredGridLayoutManager.this.mReverseLayout ? this.findOneVisibleChild(0, this.mViews.size(), true) : this.findOneVisibleChild(this.mViews.size() - 1, -1, true);
        }

        int findOnePartiallyOrCompletelyVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptCompletelyVisible, boolean acceptEndPointInclusion) {
            int start = StaggeredGridLayoutManager.this.mPrimaryOrientation.getStartAfterPadding();
            int end = StaggeredGridLayoutManager.this.mPrimaryOrientation.getEndAfterPadding();
            int next = toIndex > fromIndex ? 1 : -1;

            for(int i = fromIndex; i != toIndex; i += next) {
                View child = (View)this.mViews.get(i);
                int childStart = StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedStart(child);
                int childEnd = StaggeredGridLayoutManager.this.mPrimaryOrientation.getDecoratedEnd(child);
                boolean childStartInclusion = acceptEndPointInclusion ? childStart <= end : childStart < end;
                boolean childEndInclusion = acceptEndPointInclusion ? childEnd >= start : childEnd > start;
                if (childStartInclusion && childEndInclusion) {
                    if (completelyVisible && acceptCompletelyVisible) {
                        if (childStart >= start && childEnd <= end) {
                            return StaggeredGridLayoutManager.this.getPosition(child);
                        }
                    } else {
                        if (acceptCompletelyVisible) {
                            return StaggeredGridLayoutManager.this.getPosition(child);
                        }

                        if (childStart < start || childEnd > end) {
                            return StaggeredGridLayoutManager.this.getPosition(child);
                        }
                    }
                }
            }

            return -1;
        }

        int findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible) {
            return this.findOnePartiallyOrCompletelyVisibleChild(fromIndex, toIndex, completelyVisible, true, false);
        }

        int findOnePartiallyVisibleChild(int fromIndex, int toIndex, boolean acceptEndPointInclusion) {
            return this.findOnePartiallyOrCompletelyVisibleChild(fromIndex, toIndex, false, false, acceptEndPointInclusion);
        }

        public View getFocusableViewAfter(int referenceChildPosition, int layoutDir) {
            View candidate = null;
            int limit;
            if (layoutDir == -1) {
                limit = this.mViews.size();

                for(int i = 0; i < limit; ++i) {
                    View view = (View)this.mViews.get(i);
                    if (StaggeredGridLayoutManager.this.mReverseLayout && StaggeredGridLayoutManager.this.getPosition(view) <= referenceChildPosition || !StaggeredGridLayoutManager.this.mReverseLayout && StaggeredGridLayoutManager.this.getPosition(view) >= referenceChildPosition || !view.hasFocusable()) {
                        break;
                    }

                    candidate = view;
                }
            } else {
                for(limit = this.mViews.size() - 1; limit >= 0; --limit) {
                    View viewx = (View)this.mViews.get(limit);
                    if (StaggeredGridLayoutManager.this.mReverseLayout && StaggeredGridLayoutManager.this.getPosition(viewx) >= referenceChildPosition || !StaggeredGridLayoutManager.this.mReverseLayout && StaggeredGridLayoutManager.this.getPosition(viewx) <= referenceChildPosition || !viewx.hasFocusable()) {
                        break;
                    }

                    candidate = viewx;
                }
            }

            return candidate;
        }
    }

    public static class LayoutParams extends android.support.v7.widget.RecyclerView.LayoutParams {
        public static final int INVALID_SPAN_ID = -1;
        StaggeredGridLayoutManager.Span mSpan;
        boolean mFullSpan;

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

        public void setFullSpan(boolean fullSpan) {
            this.mFullSpan = fullSpan;
        }

        public boolean isFullSpan() {
            return this.mFullSpan;
        }

        public final int getSpanIndex() {
            return this.mSpan == null ? -1 : this.mSpan.mIndex;
        }
    }
}
