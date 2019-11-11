//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.view.View;

public abstract class OrientationHelper {
    private static final int INVALID_SIZE = -2147483648;
    protected final LayoutManager mLayoutManager;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private int mLastTotalSpace;
    final Rect mTmpRect;

    private OrientationHelper(LayoutManager layoutManager) {
        this.mLastTotalSpace = -2147483648;
        this.mTmpRect = new Rect();
        this.mLayoutManager = layoutManager;
    }

    public LayoutManager getLayoutManager() {
        return this.mLayoutManager;
    }

    public void onLayoutComplete() {
        this.mLastTotalSpace = this.getTotalSpace();
    }

    public int getTotalSpaceChange() {
        return -2147483648 == this.mLastTotalSpace ? 0 : this.getTotalSpace() - this.mLastTotalSpace;
    }

    public abstract int getDecoratedStart(View var1);

    public abstract int getDecoratedEnd(View var1);

    public abstract int getTransformedEndWithDecoration(View var1);

    public abstract int getTransformedStartWithDecoration(View var1);

    public abstract int getDecoratedMeasurement(View var1);

    public abstract int getDecoratedMeasurementInOther(View var1);

    public abstract int getStartAfterPadding();

    public abstract int getEndAfterPadding();

    public abstract int getEnd();

    public abstract void offsetChildren(int var1);

    public abstract int getTotalSpace();

    public abstract void offsetChild(View var1, int var2);

    public abstract int getEndPadding();

    public abstract int getMode();

    public abstract int getModeInOther();

    public static OrientationHelper createOrientationHelper(LayoutManager layoutManager, int orientation) {
        switch(orientation) {
            case 0:
                return createHorizontalHelper(layoutManager);
            case 1:
                return createVerticalHelper(layoutManager);
            default:
                throw new IllegalArgumentException("invalid orientation");
        }
    }

    public static OrientationHelper createHorizontalHelper(LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) {
            public int getEndAfterPadding() {
                return this.mLayoutManager.getWidth() - this.mLayoutManager.getPaddingRight();
            }

            public int getEnd() {
                return this.mLayoutManager.getWidth();
            }

            public void offsetChildren(int amount) {
                this.mLayoutManager.offsetChildrenHorizontal(amount);
            }

            public int getStartAfterPadding() {
                return this.mLayoutManager.getPaddingLeft();
            }

            public int getDecoratedMeasurement(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            public int getDecoratedMeasurementInOther(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            public int getDecoratedEnd(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedRight(view) + params.rightMargin;
            }

            public int getDecoratedStart(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedLeft(view) - params.leftMargin;
            }

            public int getTransformedEndWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.right;
            }

            public int getTransformedStartWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.left;
            }

            public int getTotalSpace() {
                return this.mLayoutManager.getWidth() - this.mLayoutManager.getPaddingLeft() - this.mLayoutManager.getPaddingRight();
            }

            public void offsetChild(View view, int offset) {
                view.offsetLeftAndRight(offset);
            }

            public int getEndPadding() {
                return this.mLayoutManager.getPaddingRight();
            }

            public int getMode() {
                return this.mLayoutManager.getWidthMode();
            }

            public int getModeInOther() {
                return this.mLayoutManager.getHeightMode();
            }
        };
    }

    public static OrientationHelper createVerticalHelper(LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) {
            public int getEndAfterPadding() {
                return this.mLayoutManager.getHeight() - this.mLayoutManager.getPaddingBottom();
            }

            public int getEnd() {
                return this.mLayoutManager.getHeight();
            }

            public void offsetChildren(int amount) {
                this.mLayoutManager.offsetChildrenVertical(amount);
            }

            public int getStartAfterPadding() {
                return this.mLayoutManager.getPaddingTop();
            }

            public int getDecoratedMeasurement(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
            }

            public int getDecoratedMeasurementInOther(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
            }

            public int getDecoratedEnd(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedBottom(view) + params.bottomMargin;
            }

            public int getDecoratedStart(View view) {
                LayoutParams params = (LayoutParams)view.getLayoutParams();
                return this.mLayoutManager.getDecoratedTop(view) - params.topMargin;
            }

            public int getTransformedEndWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.bottom;
            }

            public int getTransformedStartWithDecoration(View view) {
                this.mLayoutManager.getTransformedBoundingBox(view, true, this.mTmpRect);
                return this.mTmpRect.top;
            }

            public int getTotalSpace() {
                return this.mLayoutManager.getHeight() - this.mLayoutManager.getPaddingTop() - this.mLayoutManager.getPaddingBottom();
            }

            public void offsetChild(View view, int offset) {
                view.offsetTopAndBottom(offset);
            }

            public int getEndPadding() {
                return this.mLayoutManager.getPaddingBottom();
            }

            public int getMode() {
                return this.mLayoutManager.getHeightMode();
            }

            public int getModeInOther() {
                return this.mLayoutManager.getWidthMode();
            }
        };
    }
}
