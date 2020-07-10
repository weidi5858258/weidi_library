//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget;

import android.graphics.PointF;


import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.SmoothScroller.ScrollVectorProvider;
import android.view.View;

public class LinearSnapHelper extends SnapHelper {
    private static final float INVALID_DISTANCE = 1.0F;

    private OrientationHelper mVerticalHelper;

    private OrientationHelper mHorizontalHelper;

    public LinearSnapHelper() {
    }

    public int[] calculateDistanceToFinalSnap(LayoutManager layoutManager, View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = this.distanceToCenter(layoutManager, targetView, this.getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = this.distanceToCenter(layoutManager, targetView, this.getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }

        return out;
    }

    public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX, int velocityY) {
        if (!(layoutManager instanceof ScrollVectorProvider)) {
            return -1;
        } else {
            int itemCount = layoutManager.getItemCount();
            if (itemCount == 0) {
                return -1;
            } else {
                View currentView = this.findSnapView(layoutManager);
                if (currentView == null) {
                    return -1;
                } else {
                    int currentPosition = layoutManager.getPosition(currentView);
                    if (currentPosition == -1) {
                        return -1;
                    } else {
                        ScrollVectorProvider vectorProvider = (ScrollVectorProvider)layoutManager;
                        PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
                        if (vectorForEnd == null) {
                            return -1;
                        } else {
                            int hDeltaJump;
                            if (layoutManager.canScrollHorizontally()) {
                                hDeltaJump = this.estimateNextPositionDiffForFling(layoutManager, this.getHorizontalHelper(layoutManager), velocityX, 0);
                                if (vectorForEnd.x < 0.0F) {
                                    hDeltaJump = -hDeltaJump;
                                }
                            } else {
                                hDeltaJump = 0;
                            }

                            int vDeltaJump;
                            if (layoutManager.canScrollVertically()) {
                                vDeltaJump = this.estimateNextPositionDiffForFling(layoutManager, this.getVerticalHelper(layoutManager), 0, velocityY);
                                if (vectorForEnd.y < 0.0F) {
                                    vDeltaJump = -vDeltaJump;
                                }
                            } else {
                                vDeltaJump = 0;
                            }

                            int deltaJump = layoutManager.canScrollVertically() ? vDeltaJump : hDeltaJump;
                            if (deltaJump == 0) {
                                return -1;
                            } else {
                                int targetPos = currentPosition + deltaJump;
                                if (targetPos < 0) {
                                    targetPos = 0;
                                }

                                if (targetPos >= itemCount) {
                                    targetPos = itemCount - 1;
                                }

                                return targetPos;
                            }
                        }
                    }
                }
            }
        }
    }

    public View findSnapView(LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return this.findCenterView(layoutManager, this.getVerticalHelper(layoutManager));
        } else {
            return layoutManager.canScrollHorizontally() ? this.findCenterView(layoutManager, this.getHorizontalHelper(layoutManager)) : null;
        }
    }

    private int distanceToCenter(LayoutManager layoutManager, View targetView, OrientationHelper helper) {
        int childCenter = helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2;
        int containerCenter;
        if (layoutManager.getClipToPadding()) {
            containerCenter = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            containerCenter = helper.getEnd() / 2;
        }

        return childCenter - containerCenter;
    }

    private int estimateNextPositionDiffForFling(LayoutManager layoutManager, OrientationHelper helper, int velocityX, int velocityY) {
        int[] distances = this.calculateScrollDistance(velocityX, velocityY);
        float distancePerChild = this.computeDistancePerChild(layoutManager, helper);
        if (distancePerChild <= 0.0F) {
            return 0;
        } else {
            int distance = Math.abs(distances[0]) > Math.abs(distances[1]) ? distances[0] : distances[1];
            return Math.round((float)distance / distancePerChild);
        }
    }


    private View findCenterView(LayoutManager layoutManager, OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        } else {
            View closestChild = null;
            int center;
            if (layoutManager.getClipToPadding()) {
                center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
            } else {
                center = helper.getEnd() / 2;
            }

            int absClosest = 2147483647;

            for(int i = 0; i < childCount; ++i) {
                View child = layoutManager.getChildAt(i);
                int childCenter = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2;
                int absDistance = Math.abs(childCenter - center);
                if (absDistance < absClosest) {
                    absClosest = absDistance;
                    closestChild = child;
                }
            }

            return closestChild;
        }
    }

    private float computeDistancePerChild(LayoutManager layoutManager, OrientationHelper helper) {
        View minPosView = null;
        View maxPosView = null;
        int minPos = 2147483647;
        int maxPos = -2147483648;
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return 1.0F;
        } else {
            int start;
            int pos;
            for(start = 0; start < childCount; ++start) {
                View child = layoutManager.getChildAt(start);
                pos = layoutManager.getPosition(child);
                if (pos != -1) {
                    if (pos < minPos) {
                        minPos = pos;
                        minPosView = child;
                    }

                    if (pos > maxPos) {
                        maxPos = pos;
                        maxPosView = child;
                    }
                }
            }

            if (minPosView != null && maxPosView != null) {
                start = Math.min(helper.getDecoratedStart(minPosView), helper.getDecoratedStart(maxPosView));
                int end = Math.max(helper.getDecoratedEnd(minPosView), helper.getDecoratedEnd(maxPosView));
                pos = end - start;
                if (pos == 0) {
                    return 1.0F;
                } else {
                    return 1.0F * (float)pos / (float)(maxPos - minPos + 1);
                }
            } else {
                return 1.0F;
            }
        }
    }


    private OrientationHelper getVerticalHelper(LayoutManager layoutManager) {
        if (this.mVerticalHelper == null || this.mVerticalHelper.mLayoutManager != layoutManager) {
            this.mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }

        return this.mVerticalHelper;
    }


    private OrientationHelper getHorizontalHelper(LayoutManager layoutManager) {
        if (this.mHorizontalHelper == null || this.mHorizontalHelper.mLayoutManager != layoutManager) {
            this.mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }

        return this.mHorizontalHelper;
    }
}
