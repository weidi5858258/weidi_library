//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewPropertyAnimator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultItemAnimator extends SimpleItemAnimator {
    private static final boolean DEBUG = false;
    private static TimeInterpolator sDefaultInterpolator;
    private ArrayList<ViewHolder> mPendingRemovals = new ArrayList();
    private ArrayList<ViewHolder> mPendingAdditions = new ArrayList();
    private ArrayList<DefaultItemAnimator.MoveInfo> mPendingMoves = new ArrayList();
    private ArrayList<DefaultItemAnimator.ChangeInfo> mPendingChanges = new ArrayList();
    ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList();
    ArrayList<ArrayList<DefaultItemAnimator.MoveInfo>> mMovesList = new ArrayList();
    ArrayList<ArrayList<DefaultItemAnimator.ChangeInfo>> mChangesList = new ArrayList();
    ArrayList<ViewHolder> mAddAnimations = new ArrayList();
    ArrayList<ViewHolder> mMoveAnimations = new ArrayList();
    ArrayList<ViewHolder> mRemoveAnimations = new ArrayList();
    ArrayList<ViewHolder> mChangeAnimations = new ArrayList();

    public DefaultItemAnimator() {
    }

    public void runPendingAnimations() {
        boolean removalsPending = !this.mPendingRemovals.isEmpty();
        boolean movesPending = !this.mPendingMoves.isEmpty();
        boolean changesPending = !this.mPendingChanges.isEmpty();
        boolean additionsPending = !this.mPendingAdditions.isEmpty();
        if (removalsPending || movesPending || additionsPending || changesPending) {
            Iterator var5 = this.mPendingRemovals.iterator();

            while(var5.hasNext()) {
                ViewHolder holder = (ViewHolder)var5.next();
                this.animateRemoveImpl(holder);
            }

            this.mPendingRemovals.clear();
            Runnable adder;
            if (movesPending) {
                final ArrayList additions;
                additions = new ArrayList();
                additions.addAll(this.mPendingMoves);
                this.mMovesList.add(additions);
                this.mPendingMoves.clear();
                adder = new Runnable() {
                    public void run() {
                        Iterator var1 = additions.iterator();

                        while(var1.hasNext()) {
                            DefaultItemAnimator.MoveInfo moveInfo = (DefaultItemAnimator.MoveInfo)var1.next();
                            DefaultItemAnimator.this.animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY);
                        }

                        additions.clear();
                        DefaultItemAnimator.this.mMovesList.remove(additions);
                    }
                };
                if (removalsPending) {
                    View view = ((DefaultItemAnimator.MoveInfo)additions.get(0)).holder.itemView;
                    ViewCompat.postOnAnimationDelayed(view, adder, this.getRemoveDuration());
                } else {
                    adder.run();
                }
            }

            if (changesPending) {
                final ArrayList additions;
                additions = new ArrayList();
                additions.addAll(this.mPendingChanges);
                this.mChangesList.add(additions);
                this.mPendingChanges.clear();
                adder = new Runnable() {
                    public void run() {
                        Iterator var1 = additions.iterator();

                        while(var1.hasNext()) {
                            DefaultItemAnimator.ChangeInfo change = (DefaultItemAnimator.ChangeInfo)var1.next();
                            DefaultItemAnimator.this.animateChangeImpl(change);
                        }

                        additions.clear();
                        DefaultItemAnimator.this.mChangesList.remove(additions);
                    }
                };
                if (removalsPending) {
                    ViewHolder holder = ((DefaultItemAnimator.ChangeInfo)additions.get(0)).oldHolder;
                    ViewCompat.postOnAnimationDelayed(holder.itemView, adder, this.getRemoveDuration());
                } else {
                    adder.run();
                }
            }

            if (additionsPending) {
                final ArrayList additions;
                additions = new ArrayList();
                additions.addAll(this.mPendingAdditions);
                this.mAdditionsList.add(additions);
                this.mPendingAdditions.clear();
                adder = new Runnable() {
                    public void run() {
                        Iterator var1 = additions.iterator();

                        while(var1.hasNext()) {
                            ViewHolder holder = (ViewHolder)var1.next();
                            DefaultItemAnimator.this.animateAddImpl(holder);
                        }

                        additions.clear();
                        DefaultItemAnimator.this.mAdditionsList.remove(additions);
                    }
                };
                if (!removalsPending && !movesPending && !changesPending) {
                    adder.run();
                } else {
                    long removeDuration = removalsPending ? this.getRemoveDuration() : 0L;
                    long moveDuration = movesPending ? this.getMoveDuration() : 0L;
                    long changeDuration = changesPending ? this.getChangeDuration() : 0L;
                    long totalDelay = removeDuration + Math.max(moveDuration, changeDuration);
                    View view = ((ViewHolder)additions.get(0)).itemView;
                    ViewCompat.postOnAnimationDelayed(view, adder, totalDelay);
                }
            }

        }
    }

    public boolean animateRemove(ViewHolder holder) {
        this.resetAnimation(holder);
        this.mPendingRemovals.add(holder);
        return true;
    }

    private void animateRemoveImpl(final ViewHolder holder) {
        final View view = holder.itemView;
        final ViewPropertyAnimator animation = view.animate();
        this.mRemoveAnimations.add(holder);
        animation.setDuration(this.getRemoveDuration()).alpha(0.0F).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DefaultItemAnimator.this.dispatchRemoveStarting(holder);
            }

            public void onAnimationEnd(Animator animator) {
                animation.setListener((AnimatorListener)null);
                view.setAlpha(1.0F);
                DefaultItemAnimator.this.dispatchRemoveFinished(holder);
                DefaultItemAnimator.this.mRemoveAnimations.remove(holder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateAdd(ViewHolder holder) {
        this.resetAnimation(holder);
        holder.itemView.setAlpha(0.0F);
        this.mPendingAdditions.add(holder);
        return true;
    }

    void animateAddImpl(final ViewHolder holder) {
        final View view = holder.itemView;
        final ViewPropertyAnimator animation = view.animate();
        this.mAddAnimations.add(holder);
        animation.alpha(1.0F).setDuration(this.getAddDuration()).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DefaultItemAnimator.this.dispatchAddStarting(holder);
            }

            public void onAnimationCancel(Animator animator) {
                view.setAlpha(1.0F);
            }

            public void onAnimationEnd(Animator animator) {
                animation.setListener((AnimatorListener)null);
                DefaultItemAnimator.this.dispatchAddFinished(holder);
                DefaultItemAnimator.this.mAddAnimations.remove(holder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateMove(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        View view = holder.itemView;
        fromX += (int)holder.itemView.getTranslationX();
        fromY += (int)holder.itemView.getTranslationY();
        this.resetAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            this.dispatchMoveFinished(holder);
            return false;
        } else {
            if (deltaX != 0) {
                view.setTranslationX((float)(-deltaX));
            }

            if (deltaY != 0) {
                view.setTranslationY((float)(-deltaY));
            }

            this.mPendingMoves.add(new DefaultItemAnimator.MoveInfo(holder, fromX, fromY, toX, toY));
            return true;
        }
    }

    void animateMoveImpl(final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        if (deltaX != 0) {
            view.animate().translationX(0.0F);
        }

        if (deltaY != 0) {
            view.animate().translationY(0.0F);
        }

        final ViewPropertyAnimator animation = view.animate();
        this.mMoveAnimations.add(holder);
        animation.setDuration(this.getMoveDuration()).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DefaultItemAnimator.this.dispatchMoveStarting(holder);
            }

            public void onAnimationCancel(Animator animator) {
                if (deltaX != 0) {
                    view.setTranslationX(0.0F);
                }

                if (deltaY != 0) {
                    view.setTranslationY(0.0F);
                }

            }

            public void onAnimationEnd(Animator animator) {
                animation.setListener((AnimatorListener)null);
                DefaultItemAnimator.this.dispatchMoveFinished(holder);
                DefaultItemAnimator.this.mMoveAnimations.remove(holder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if (oldHolder == newHolder) {
            return this.animateMove(oldHolder, fromX, fromY, toX, toY);
        } else {
            float prevTranslationX = oldHolder.itemView.getTranslationX();
            float prevTranslationY = oldHolder.itemView.getTranslationY();
            float prevAlpha = oldHolder.itemView.getAlpha();
            this.resetAnimation(oldHolder);
            int deltaX = (int)((float)(toX - fromX) - prevTranslationX);
            int deltaY = (int)((float)(toY - fromY) - prevTranslationY);
            oldHolder.itemView.setTranslationX(prevTranslationX);
            oldHolder.itemView.setTranslationY(prevTranslationY);
            oldHolder.itemView.setAlpha(prevAlpha);
            if (newHolder != null) {
                this.resetAnimation(newHolder);
                newHolder.itemView.setTranslationX((float)(-deltaX));
                newHolder.itemView.setTranslationY((float)(-deltaY));
                newHolder.itemView.setAlpha(0.0F);
            }

            this.mPendingChanges.add(new DefaultItemAnimator.ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
            return true;
        }
    }

    void animateChangeImpl(final DefaultItemAnimator.ChangeInfo changeInfo) {
        ViewHolder holder = changeInfo.oldHolder;
        final View view = holder == null ? null : holder.itemView;
        ViewHolder newHolder = changeInfo.newHolder;
        final View newView = newHolder != null ? newHolder.itemView : null;
        if (view != null) {
            final ViewPropertyAnimator newViewAnimation;
            newViewAnimation = view.animate().setDuration(this.getChangeDuration());
            this.mChangeAnimations.add(changeInfo.oldHolder);
            newViewAnimation.translationX((float)(changeInfo.toX - changeInfo.fromX));
            newViewAnimation.translationY((float)(changeInfo.toY - changeInfo.fromY));
            newViewAnimation.alpha(0.0F).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    DefaultItemAnimator.this.dispatchChangeStarting(changeInfo.oldHolder, true);
                }

                public void onAnimationEnd(Animator animator) {
                    newViewAnimation.setListener((AnimatorListener)null);
                    view.setAlpha(1.0F);
                    view.setTranslationX(0.0F);
                    view.setTranslationY(0.0F);
                    DefaultItemAnimator.this.dispatchChangeFinished(changeInfo.oldHolder, true);
                    DefaultItemAnimator.this.mChangeAnimations.remove(changeInfo.oldHolder);
                    DefaultItemAnimator.this.dispatchFinishedWhenDone();
                }
            }).start();
        }

        if (newView != null) {
            final ViewPropertyAnimator newViewAnimation;
            newViewAnimation = newView.animate();
            this.mChangeAnimations.add(changeInfo.newHolder);
            newViewAnimation.translationX(0.0F).translationY(0.0F).setDuration(this.getChangeDuration()).alpha(1.0F).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    DefaultItemAnimator.this.dispatchChangeStarting(changeInfo.newHolder, false);
                }

                public void onAnimationEnd(Animator animator) {
                    newViewAnimation.setListener((AnimatorListener)null);
                    newView.setAlpha(1.0F);
                    newView.setTranslationX(0.0F);
                    newView.setTranslationY(0.0F);
                    DefaultItemAnimator.this.dispatchChangeFinished(changeInfo.newHolder, false);
                    DefaultItemAnimator.this.mChangeAnimations.remove(changeInfo.newHolder);
                    DefaultItemAnimator.this.dispatchFinishedWhenDone();
                }
            }).start();
        }

    }

    private void endChangeAnimation(List<DefaultItemAnimator.ChangeInfo> infoList, ViewHolder item) {
        for(int i = infoList.size() - 1; i >= 0; --i) {
            DefaultItemAnimator.ChangeInfo changeInfo = (DefaultItemAnimator.ChangeInfo)infoList.get(i);
            if (this.endChangeAnimationIfNecessary(changeInfo, item) && changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                infoList.remove(changeInfo);
            }
        }

    }

    private void endChangeAnimationIfNecessary(DefaultItemAnimator.ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            this.endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }

        if (changeInfo.newHolder != null) {
            this.endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }

    }

    private boolean endChangeAnimationIfNecessary(DefaultItemAnimator.ChangeInfo changeInfo, ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else {
            if (changeInfo.oldHolder != item) {
                return false;
            }

            changeInfo.oldHolder = null;
            oldItem = true;
        }

        item.itemView.setAlpha(1.0F);
        item.itemView.setTranslationX(0.0F);
        item.itemView.setTranslationY(0.0F);
        this.dispatchChangeFinished(item, oldItem);
        return true;
    }

    public void endAnimation(ViewHolder item) {
        View view = item.itemView;
        view.animate().cancel();

        int i;
        for(i = this.mPendingMoves.size() - 1; i >= 0; --i) {
            DefaultItemAnimator.MoveInfo moveInfo = (DefaultItemAnimator.MoveInfo)this.mPendingMoves.get(i);
            if (moveInfo.holder == item) {
                view.setTranslationY(0.0F);
                view.setTranslationX(0.0F);
                this.dispatchMoveFinished(item);
                this.mPendingMoves.remove(i);
            }
        }

        this.endChangeAnimation(this.mPendingChanges, item);
        if (this.mPendingRemovals.remove(item)) {
            view.setAlpha(1.0F);
            this.dispatchRemoveFinished(item);
        }

        if (this.mPendingAdditions.remove(item)) {
            view.setAlpha(1.0F);
            this.dispatchAddFinished(item);
        }

        ArrayList moves;
        for(i = this.mChangesList.size() - 1; i >= 0; --i) {
            moves = (ArrayList)this.mChangesList.get(i);
            this.endChangeAnimation(moves, item);
            if (moves.isEmpty()) {
                this.mChangesList.remove(i);
            }
        }

        for(i = this.mMovesList.size() - 1; i >= 0; --i) {
            moves = (ArrayList)this.mMovesList.get(i);

            for(int j = moves.size() - 1; j >= 0; --j) {
                DefaultItemAnimator.MoveInfo moveInfo = (DefaultItemAnimator.MoveInfo)moves.get(j);
                if (moveInfo.holder == item) {
                    view.setTranslationY(0.0F);
                    view.setTranslationX(0.0F);
                    this.dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        this.mMovesList.remove(i);
                    }
                    break;
                }
            }
        }

        for(i = this.mAdditionsList.size() - 1; i >= 0; --i) {
            moves = (ArrayList)this.mAdditionsList.get(i);
            if (moves.remove(item)) {
                view.setAlpha(1.0F);
                this.dispatchAddFinished(item);
                if (moves.isEmpty()) {
                    this.mAdditionsList.remove(i);
                }
            }
        }

        if (this.mRemoveAnimations.remove(item)) {
        }

        if (this.mAddAnimations.remove(item)) {
        }

        if (this.mChangeAnimations.remove(item)) {
        }

        if (this.mMoveAnimations.remove(item)) {
        }

        this.dispatchFinishedWhenDone();
    }

    private void resetAnimation(ViewHolder holder) {
        if (sDefaultInterpolator == null) {
            sDefaultInterpolator = (new ValueAnimator()).getInterpolator();
        }

        holder.itemView.animate().setInterpolator(sDefaultInterpolator);
        this.endAnimation(holder);
    }

    public boolean isRunning() {
        return !this.mPendingAdditions.isEmpty() || !this.mPendingChanges.isEmpty() || !this.mPendingMoves.isEmpty() || !this.mPendingRemovals.isEmpty() || !this.mMoveAnimations.isEmpty() || !this.mRemoveAnimations.isEmpty() || !this.mAddAnimations.isEmpty() || !this.mChangeAnimations.isEmpty() || !this.mMovesList.isEmpty() || !this.mAdditionsList.isEmpty() || !this.mChangesList.isEmpty();
    }

    void dispatchFinishedWhenDone() {
        if (!this.isRunning()) {
            this.dispatchAnimationsFinished();
        }

    }

    public void endAnimations() {
        int count = this.mPendingMoves.size();

        int listCount;
        for(listCount = count - 1; listCount >= 0; --listCount) {
            DefaultItemAnimator.MoveInfo item = (DefaultItemAnimator.MoveInfo)this.mPendingMoves.get(listCount);
            View view = item.holder.itemView;
            view.setTranslationY(0.0F);
            view.setTranslationX(0.0F);
            this.dispatchMoveFinished(item.holder);
            this.mPendingMoves.remove(listCount);
        }

        count = this.mPendingRemovals.size();

        ViewHolder item;
        for(listCount = count - 1; listCount >= 0; --listCount) {
            item = (ViewHolder)this.mPendingRemovals.get(listCount);
            this.dispatchRemoveFinished(item);
            this.mPendingRemovals.remove(listCount);
        }

        count = this.mPendingAdditions.size();

        for(listCount = count - 1; listCount >= 0; --listCount) {
            item = (ViewHolder)this.mPendingAdditions.get(listCount);
            item.itemView.setAlpha(1.0F);
            this.dispatchAddFinished(item);
            this.mPendingAdditions.remove(listCount);
        }

        count = this.mPendingChanges.size();

        for(listCount = count - 1; listCount >= 0; --listCount) {
            this.endChangeAnimationIfNecessary((DefaultItemAnimator.ChangeInfo)this.mPendingChanges.get(listCount));
        }

        this.mPendingChanges.clear();
        if (this.isRunning()) {
            listCount = this.mMovesList.size();

            int j;
            int i;
            ArrayList changes;
            for(i = listCount - 1; i >= 0; --i) {
                changes = (ArrayList)this.mMovesList.get(i);
                count = changes.size();

                for(j = count - 1; j >= 0; --j) {
                    DefaultItemAnimator.MoveInfo moveInfo = (DefaultItemAnimator.MoveInfo)changes.get(j);
                    item = moveInfo.holder;
                    View view = item.itemView;
                    view.setTranslationY(0.0F);
                    view.setTranslationX(0.0F);
                    this.dispatchMoveFinished(moveInfo.holder);
                    changes.remove(j);
                    if (changes.isEmpty()) {
                        this.mMovesList.remove(changes);
                    }
                }
            }

            listCount = this.mAdditionsList.size();

            for(i = listCount - 1; i >= 0; --i) {
                changes = (ArrayList)this.mAdditionsList.get(i);
                count = changes.size();

                for(j = count - 1; j >= 0; --j) {
                    item = (ViewHolder)changes.get(j);
                    View view = item.itemView;
                    view.setAlpha(1.0F);
                    this.dispatchAddFinished(item);
                    changes.remove(j);
                    if (changes.isEmpty()) {
                        this.mAdditionsList.remove(changes);
                    }
                }
            }

            listCount = this.mChangesList.size();

            for(i = listCount - 1; i >= 0; --i) {
                changes = (ArrayList)this.mChangesList.get(i);
                count = changes.size();

                for(j = count - 1; j >= 0; --j) {
                    this.endChangeAnimationIfNecessary((DefaultItemAnimator.ChangeInfo)changes.get(j));
                    if (changes.isEmpty()) {
                        this.mChangesList.remove(changes);
                    }
                }
            }

            this.cancelAll(this.mRemoveAnimations);
            this.cancelAll(this.mMoveAnimations);
            this.cancelAll(this.mAddAnimations);
            this.cancelAll(this.mChangeAnimations);
            this.dispatchAnimationsFinished();
        }
    }

    void cancelAll(List<ViewHolder> viewHolders) {
        for(int i = viewHolders.size() - 1; i >= 0; --i) {
            ((ViewHolder)viewHolders.get(i)).itemView.animate().cancel();
        }

    }

    public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder, @NonNull List<Object> payloads) {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads);
    }

    private static class ChangeInfo {
        public ViewHolder oldHolder;
        public ViewHolder newHolder;
        public int fromX;
        public int fromY;
        public int toX;
        public int toY;

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public String toString() {
            return "ChangeInfo{oldHolder=" + this.oldHolder + ", newHolder=" + this.newHolder + ", fromX=" + this.fromX + ", fromY=" + this.fromY + ", toX=" + this.toX + ", toY=" + this.toY + '}';
        }
    }

    private static class MoveInfo {
        public ViewHolder holder;
        public int fromX;
        public int fromY;
        public int toX;
        public int toY;

        MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }
}
