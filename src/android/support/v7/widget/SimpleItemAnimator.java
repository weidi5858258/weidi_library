//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.RecyclerView.ItemAnimator.ItemHolderInfo;
import android.view.View;

public abstract class SimpleItemAnimator extends ItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "SimpleItemAnimator";
    boolean mSupportsChangeAnimations = true;

    public SimpleItemAnimator() {
    }

    public boolean getSupportsChangeAnimations() {
        return this.mSupportsChangeAnimations;
    }

    public void setSupportsChangeAnimations(boolean supportsChangeAnimations) {
        this.mSupportsChangeAnimations = supportsChangeAnimations;
    }

    public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder) {
        return !this.mSupportsChangeAnimations || viewHolder.isInvalid();
    }

    public boolean animateDisappearance(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
        int oldLeft = preLayoutInfo.left;
        int oldTop = preLayoutInfo.top;
        View disappearingItemView = viewHolder.itemView;
        int newLeft = postLayoutInfo == null ? disappearingItemView.getLeft() : postLayoutInfo.left;
        int newTop = postLayoutInfo == null ? disappearingItemView.getTop() : postLayoutInfo.top;
        if (viewHolder.isRemoved() || oldLeft == newLeft && oldTop == newTop) {
            return this.animateRemove(viewHolder);
        } else {
            disappearingItemView.layout(newLeft, newTop, newLeft + disappearingItemView.getWidth(), newTop + disappearingItemView.getHeight());
            return this.animateMove(viewHolder, oldLeft, oldTop, newLeft, newTop);
        }
    }

    public boolean animateAppearance(@NonNull ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        return preLayoutInfo == null || preLayoutInfo.left == postLayoutInfo.left && preLayoutInfo.top == postLayoutInfo.top ? this.animateAdd(viewHolder) : this.animateMove(viewHolder, preLayoutInfo.left, preLayoutInfo.top, postLayoutInfo.left, postLayoutInfo.top);
    }

    public boolean animatePersistence(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        if (preInfo.left == postInfo.left && preInfo.top == postInfo.top) {
            this.dispatchMoveFinished(viewHolder);
            return false;
        } else {
            return this.animateMove(viewHolder, preInfo.left, preInfo.top, postInfo.left, postInfo.top);
        }
    }

    public boolean animateChange(@NonNull ViewHolder oldHolder, @NonNull ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        int fromLeft = preInfo.left;
        int fromTop = preInfo.top;
        int toLeft;
        int toTop;
        if (newHolder.shouldIgnore()) {
            toLeft = preInfo.left;
            toTop = preInfo.top;
        } else {
            toLeft = postInfo.left;
            toTop = postInfo.top;
        }

        return this.animateChange(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop);
    }

    public abstract boolean animateRemove(ViewHolder var1);

    public abstract boolean animateAdd(ViewHolder var1);

    public abstract boolean animateMove(ViewHolder var1, int var2, int var3, int var4, int var5);

    public abstract boolean animateChange(ViewHolder var1, ViewHolder var2, int var3, int var4, int var5, int var6);

    public final void dispatchRemoveFinished(ViewHolder item) {
        this.onRemoveFinished(item);
        this.dispatchAnimationFinished(item);
    }

    public final void dispatchMoveFinished(ViewHolder item) {
        this.onMoveFinished(item);
        this.dispatchAnimationFinished(item);
    }

    public final void dispatchAddFinished(ViewHolder item) {
        this.onAddFinished(item);
        this.dispatchAnimationFinished(item);
    }

    public final void dispatchChangeFinished(ViewHolder item, boolean oldItem) {
        this.onChangeFinished(item, oldItem);
        this.dispatchAnimationFinished(item);
    }

    public final void dispatchRemoveStarting(ViewHolder item) {
        this.onRemoveStarting(item);
    }

    public final void dispatchMoveStarting(ViewHolder item) {
        this.onMoveStarting(item);
    }

    public final void dispatchAddStarting(ViewHolder item) {
        this.onAddStarting(item);
    }

    public final void dispatchChangeStarting(ViewHolder item, boolean oldItem) {
        this.onChangeStarting(item, oldItem);
    }

    public void onRemoveStarting(ViewHolder item) {
    }

    public void onRemoveFinished(ViewHolder item) {
    }

    public void onAddStarting(ViewHolder item) {
    }

    public void onAddFinished(ViewHolder item) {
    }

    public void onMoveStarting(ViewHolder item) {
    }

    public void onMoveFinished(ViewHolder item) {
    }

    public void onChangeStarting(ViewHolder item, boolean oldItem) {
    }

    public void onChangeFinished(ViewHolder item, boolean oldItem) {
    }
}
