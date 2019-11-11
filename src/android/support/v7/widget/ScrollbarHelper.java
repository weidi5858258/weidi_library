//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget;

import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

class ScrollbarHelper {
    static int computeScrollOffset(State state, OrientationHelper orientation, View startChild, View endChild, LayoutManager lm, boolean smoothScrollbarEnabled, boolean reverseLayout) {
        if (lm.getChildCount() != 0 && state.getItemCount() != 0 && startChild != null && endChild != null) {
            int minPosition = Math.min(lm.getPosition(startChild), lm.getPosition(endChild));
            int maxPosition = Math.max(lm.getPosition(startChild), lm.getPosition(endChild));
            int itemsBefore = reverseLayout ? Math.max(0, state.getItemCount() - maxPosition - 1) : Math.max(0, minPosition);
            if (!smoothScrollbarEnabled) {
                return itemsBefore;
            } else {
                int laidOutArea = Math.abs(orientation.getDecoratedEnd(endChild) - orientation.getDecoratedStart(startChild));
                int itemRange = Math.abs(lm.getPosition(startChild) - lm.getPosition(endChild)) + 1;
                float avgSizePerRow = (float)laidOutArea / (float)itemRange;
                return Math.round((float)itemsBefore * avgSizePerRow + (float)(orientation.getStartAfterPadding() - orientation.getDecoratedStart(startChild)));
            }
        } else {
            return 0;
        }
    }

    static int computeScrollExtent(State state, OrientationHelper orientation, View startChild, View endChild, LayoutManager lm, boolean smoothScrollbarEnabled) {
        if (lm.getChildCount() != 0 && state.getItemCount() != 0 && startChild != null && endChild != null) {
            if (!smoothScrollbarEnabled) {
                return Math.abs(lm.getPosition(startChild) - lm.getPosition(endChild)) + 1;
            } else {
                int extend = orientation.getDecoratedEnd(endChild) - orientation.getDecoratedStart(startChild);
                return Math.min(orientation.getTotalSpace(), extend);
            }
        } else {
            return 0;
        }
    }

    static int computeScrollRange(State state, OrientationHelper orientation, View startChild, View endChild, LayoutManager lm, boolean smoothScrollbarEnabled) {
        if (lm.getChildCount() != 0 && state.getItemCount() != 0 && startChild != null && endChild != null) {
            if (!smoothScrollbarEnabled) {
                return state.getItemCount();
            } else {
                int laidOutArea = orientation.getDecoratedEnd(endChild) - orientation.getDecoratedStart(startChild);
                int laidOutRange = Math.abs(lm.getPosition(startChild) - lm.getPosition(endChild)) + 1;
                return (int)((float)laidOutArea / (float)laidOutRange * (float)state.getItemCount());
            }
        } else {
            return 0;
        }
    }

    private ScrollbarHelper() {
    }
}
