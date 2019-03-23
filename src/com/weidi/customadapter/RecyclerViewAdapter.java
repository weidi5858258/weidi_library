package com.weidi.customadapter;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;

import com.weidi.customadapter.animation.AlphaInAnimation;
import com.weidi.customadapter.animation.BaseAnimation;
import com.weidi.customadapter.interfaces.IAnimation;
import com.weidi.customadapter.interfaces.IHeaderFooter;
import com.weidi.customadapter.interfaces.ILayoutManager;
import com.weidi.customadapter.interfaces.IMultiItemViewType;
import com.weidi.customadapter.interfaces.IViewBindData;
import com.weidi.customadapter.listener.OnItemClickListener;
import com.weidi.customadapter.listener.OnItemLongClickListener;
import com.weidi.customadapter.listener.OnItemTouchListener;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerViewAdapter<T> extends RecyclerView.Adapter<CustomViewHolder>
        implements
        IViewBindData<T, CustomViewHolder>,
        IAnimation,
        ILayoutManager,
        IHeaderFooter {

    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();

    protected final Context mContext;
    protected List<T> mData;
    protected int mLayoutResId;
    protected IMultiItemViewType<T> mMultiItemViewType;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemTouchListener mOnItemTouchListener;

    protected RecyclerView mRecyclerView;

    private final int TYPE_HEADER = -0x100;
    private final int TYPE_FOOTER = -0x101;
    protected View mHeaderView;
    protected View mFooterView;

    private Interpolator mInterpolator = new LinearInterpolator();
    private long mDuration = 500;
    private boolean mLoadAnimationEnabled;
    private boolean mOnlyOnce = true;
    private BaseAnimation mLoadAnimation;
    private int mLastPosition = -1;

    /**
     * Constructor for single item view type.
     *
     * @param context     Context.
     * @param list        Data list.
     * @param layoutResId {@link android.support.annotation.LayoutRes}
     */
    public RecyclerViewAdapter(
            Context context,
            List<T> list,
            int layoutResId) {
        if (context == null) {
            throw new NullPointerException("RecyclerViewAdapter's context is null.");
        }
        this.mContext = context;
        this.mData = list == null ? new ArrayList<T>() : list;
        this.mLayoutResId = layoutResId;
        this.mMultiItemViewType = null;
    }

    /**
     * Constructor for multiple item view type.
     *
     * @param context         Context.
     * @param list            Data list.
     * @param mulItemViewType If null, plz override {@link #offerMultiItemViewType()}.
     */
    public RecyclerViewAdapter(
            Context context,
            List<T> list,
            IMultiItemViewType<T> mulItemViewType) {
        if (context == null) {
            throw new NullPointerException("RecyclerViewAdapter's context is null.");
        }
        this.mContext = context;
        this.mData = list == null ? new ArrayList<T>() : list;
        this.mMultiItemViewType =
                mulItemViewType == null ? offerMultiItemViewType() : mulItemViewType;
    }

    @Override
    public int getItemCount() {
        int size = mData == null ? 0 : mData.size();
        if (hasHeaderView()) {
            ++size;
        }
        if (hasFooterView()) {
            ++size;
        }
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        if (isHeaderView(position)) {
            viewType = TYPE_HEADER;
        } else if (isFooterView(position)) {
            viewType = TYPE_FOOTER;
        } else {
            if (mMultiItemViewType != null) {
                if (hasHeaderView()) {
                    --position;
                }
                return mMultiItemViewType.getItemViewType(position, mData.get(position));
            }
            //            return 0;
        }
        return viewType;
    }

    /**
     * @param parent   RecyclerView
     * @param viewType 这里的viewType是由getItemViewType(int position)方法得到的,
     *                 由RecyclerView.Adapter在内部完成
     * @return
     */
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        final CustomViewHolder holder;
        if (viewType == TYPE_HEADER && hasHeaderView()) {
            return new CustomViewHolder(getHeaderView());
        } else if (viewType == TYPE_FOOTER && hasFooterView()) {
            return new CustomViewHolder(getFooterView());
        } else {
            holder = onCreate(null, parent, viewType);
        }

        if (!(holder.itemView instanceof AdapterView)
                && !(holder.itemView instanceof RecyclerView)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(
                                v,
                                viewType,
                                holder.getAdapterPosition());
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemLongClickListener != null) {
                        return mOnItemLongClickListener.onItemLongClick(
                                v,
                                viewType,
                                holder.getAdapterPosition());
                    }
                    return false;
                }
            });
            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mOnItemTouchListener != null) {
                        return mOnItemTouchListener.onItemTouch(
                                v,
                                viewType,
                                holder.getAdapterPosition(),
                                event);
                    }
                    return false;
                }
            });
        }
        return holder;
    }

    /**
     * 这里不绑定"头"和"脚"的数据
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(
            CustomViewHolder holder,
            int position) {
        int viewType = getItemViewType(position);
        if (viewType != TYPE_HEADER && viewType != TYPE_FOOTER) {
            // 在子类中必须fu写这个方法
            onBind(holder, viewType, position, mData.get(hasHeaderView() ? --position : position));
            addLoadAnimation(holder); // Load animation
        }
    }

    @Override
    public void onBindViewHolder(
            CustomViewHolder holder,
            int position,
            List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (mRecyclerView != null && mRecyclerView != recyclerView)
            Log.i(TAG, "Does not support multiple RecyclerViews now.");
        mRecyclerView = recyclerView;
        // Ensure a situation that add header or footer before setAdapter().
        ifGridLayoutManager();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = null;
    }

    @Override
    public void onViewAttachedToWindow(CustomViewHolder holder) {
        if (isHeaderView(holder.getLayoutPosition()) || isFooterView(holder.getLayoutPosition())) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
            }
        }
    }

    //**********************************************************************//

    public Context getContext() {
        return mContext;
    }

    /**
     * 过时的
     *
     * @return
     */
    //    @Deprecated
    //    public List<T> getList() {
    //        return mData;
    //    }

    /**
     * 使用这个
     *
     * @return
     */
    public List<T> getData() {
        return mData;
    }

    public void setData(List<T> data) {
        mData = data;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener) {
        this.mOnItemTouchListener = onItemTouchListener;
    }

    /**
     * 供子类覆写,如果mMultiItemViewType为null,那么子类需要实现这个方法
     *
     * @return Offered an {@link IMultiItemViewType} by overriding this method.
     */
    protected IMultiItemViewType<T> offerMultiItemViewType() {
        return null;
    }

    @Override
    public boolean hasLayoutManager() {
        return mRecyclerView != null && mRecyclerView.getLayoutManager() != null;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return hasLayoutManager() ? mRecyclerView.getLayoutManager() : null;
    }

    @Override
    public View getHeaderView() {
        return mHeaderView;
    }

    @Override
    public View getFooterView() {
        return mFooterView;
    }

    @Override
    public void addHeaderView(View header) {
        if (hasHeaderView()) {
            return;
            //            throw new IllegalStateException("You have already added a header view.");
        }
        mHeaderView = header;
        setLayoutParams(mHeaderView);
        ifGridLayoutManager();
        notifyItemInserted(0);
        notifyDataSetChanged();
    }

    @Override
    public void addFooterView(View footer) {
        if (hasFooterView()) {
            return;
            //            throw new IllegalStateException("You have already added a footer view.");
        }
        mFooterView = footer;
        setLayoutParams(mFooterView);
        ifGridLayoutManager();
        notifyItemInserted(getItemCount() - 1);
        notifyDataSetChanged();
    }

    @Override
    public boolean removeHeaderView() {
        if (hasHeaderView()) {
            notifyItemRemoved(0);
            mHeaderView = null;
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeFooterView() {
        if (hasFooterView()) {
            int footerPosition = getItemCount() - 1;
            notifyItemRemoved(footerPosition);
            mFooterView = null;
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean hasHeaderView() {
        return getHeaderView() != null;
    }

    @Override
    public boolean hasFooterView() {
        return getFooterView() != null;
    }

    @Override
    public boolean isHeaderView(int position) {
        return hasHeaderView() && position == 0;
    }

    @Override
    public boolean isFooterView(int position) {
        return hasFooterView() && position == getItemCount() - 1;
    }

    private void ifGridLayoutManager() {
        if (hasHeaderView() || hasFooterView()) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                final GridLayoutManager.SpanSizeLookup originalSpanSizeLookup =
                        ((GridLayoutManager) layoutManager).getSpanSizeLookup();
                ((GridLayoutManager) layoutManager).setSpanSizeLookup(
                        new GridLayoutManager.SpanSizeLookup() {
                            @Override
                            public int getSpanSize(int position) {
                                return (isHeaderView(position) || isFooterView(position)) ?
                                        ((GridLayoutManager) layoutManager).getSpanCount() :
                                        originalSpanSizeLookup.getSpanSize(position);
                            }
                        });
            }
        }
    }

    private void setLayoutParams(View view) {
        if (hasHeaderView() || hasFooterView()) {
            RecyclerView.LayoutManager layoutManager = getLayoutManager();

            if (layoutManager instanceof StaggeredGridLayoutManager) {

                view.setLayoutParams(new StaggeredGridLayoutManager.LayoutParams(
                        StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT));
            } else if (layoutManager instanceof GridLayoutManager) {

                view.setLayoutParams(new GridLayoutManager.LayoutParams(
                        GridLayoutManager.LayoutParams.MATCH_PARENT,
                        GridLayoutManager.LayoutParams.WRAP_CONTENT));
            } else {

                view.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    //------------------------------------ Load animation ------------------------------------//

    @Override
    public void enableLoadAnimation() {
        enableLoadAnimation(mDuration, new AlphaInAnimation());
    }

    @Override
    public void enableLoadAnimation(long duration, BaseAnimation animation) {
        if (duration > 0) {
            mDuration = duration;
        } else {
            Log.w(TAG, "Invalid animation duration");
        }
        mLoadAnimationEnabled = true;
        mLoadAnimation = animation;
    }

    @Override
    public void cancelLoadAnimation() {
        mLoadAnimationEnabled = false;
        mLoadAnimation = null;
    }

    @Override
    public void setOnlyOnce(boolean onlyOnce) {
        mOnlyOnce = onlyOnce;
    }

    @Override
    public void addLoadAnimation(RecyclerView.ViewHolder holder) {
        if (mLoadAnimationEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (!mOnlyOnce || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation =
                        mLoadAnimation == null ? new AlphaInAnimation() : mLoadAnimation;
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    anim.setInterpolator(mInterpolator);
                    anim.setDuration(mDuration).start();
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

}
