//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.widget.helper;

import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
//import android.support.v7.recyclerview.R.id;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.weidi.library.R;

class ItemTouchUIUtilImpl implements ItemTouchUIUtil {
    static final ItemTouchUIUtil INSTANCE = new ItemTouchUIUtilImpl();

    ItemTouchUIUtilImpl() {
    }

    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (VERSION.SDK_INT >= 21 && isCurrentlyActive) {
            Object originalElevation = view.getTag(R.id.item_touch_helper_previous_elevation);
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(view);
                float newElevation = 1.0F + findMaxElevation(recyclerView, view);
                ViewCompat.setElevation(view, newElevation);
                view.setTag(R.id.item_touch_helper_previous_elevation, originalElevation);
            }
        }

        view.setTranslationX(dX);
        view.setTranslationY(dY);
    }

    private static float findMaxElevation(RecyclerView recyclerView, View itemView) {
        int childCount = recyclerView.getChildCount();
        float max = 0.0F;

        for(int i = 0; i < childCount; ++i) {
            View child = recyclerView.getChildAt(i);
            if (child != itemView) {
                float elevation = ViewCompat.getElevation(child);
                if (elevation > max) {
                    max = elevation;
                }
            }
        }

        return max;
    }

    public void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    }

    public void clearView(View view) {
        if (VERSION.SDK_INT >= 21) {
            Object tag = view.getTag(R.id.item_touch_helper_previous_elevation);
            if (tag != null && tag instanceof Float) {
                ViewCompat.setElevation(view, (Float)tag);
            }

            view.setTag(R.id.item_touch_helper_previous_elevation, (Object)null);
        }

        view.setTranslationX(0.0F);
        view.setTranslationY(0.0F);
    }

    public void onSelected(View view) {
    }
}
