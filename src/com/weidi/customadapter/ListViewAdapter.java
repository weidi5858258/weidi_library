package com.weidi.customadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import com.weidi.customadapter.interfaces.IHeaderFooter;
import com.weidi.customadapter.interfaces.IMultiItemViewType;
import com.weidi.customadapter.interfaces.IViewBindData;
import com.weidi.customadapter.listener.OnItemClickListener;
import com.weidi.customadapter.listener.OnItemLongClickListener;
import com.weidi.customadapter.listener.OnItemTouchListener;

import java.util.ArrayList;
import java.util.List;

public abstract class ListViewAdapter<T> extends BaseAdapter
        implements
        ListAdapter,
        SpinnerAdapter,
        IViewBindData<T, CustomViewHolder>,
        IHeaderFooter {

    private AbsListView mAbsListView;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemTouchListener mOnItemTouchListener;

    private final int TYPE_HEADER = -0x100;
    private final int TYPE_FOOTER = -0x101;
    protected View mHeaderView;
    protected View mFooterView;

    protected final Context mContext;
    protected List<T> mData;
    protected int mLayoutResId;
    protected IMultiItemViewType<T> mMultiItemViewType;

    //    private DataSetObservable mDataSetObservable = new DataSetObservable();

    public ListViewAdapter(
            Context context,
            List<T> list,
            int layoutResId) {
        if (context == null) {
            throw new NullPointerException("ListViewAdapter's context is null.");
        }
        this.mContext = context;
        this.mData = list == null ? new ArrayList<T>() : list;
        this.mLayoutResId = layoutResId;
        this.mMultiItemViewType = null;
    }

    public ListViewAdapter(
            Context context,
            List<T> list,
            IMultiItemViewType<T> mulItemViewType) {
        if (context == null) {
            throw new NullPointerException("ListViewAdapter's context is null.");
        }
        this.mContext = context;
        this.mData = list == null ? new ArrayList<T>() : list;
        this.mMultiItemViewType =
                mulItemViewType == null ? offerMultiItemViewType() : mulItemViewType;
    }

    /**
     * @see android.widget.BaseAdapter#getCount().
     */
    @Override
    public int getCount() {
        int size = mData == null ? 0 : mData.size();
        if (hasHeaderView()) {
            ++size;
        }
        if (hasFooterView()) {
            ++size;
        }
        return size;
    }

    /**
     * @see android.widget.BaseAdapter#getItem(int).
     */
    @Override
    public T getItem(int position) {
        if (position >= mData.size() || position < 0) {
            return null;
        }
        if (isHeaderView(position)) {
            return null;
        } else if (isFooterView(position)) {
            return null;
        }
        return mData.get(position);
    }

    /**
     * @see android.widget.BaseAdapter#getItemId(int).
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see android.widget.BaseAdapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (mAbsListView == null && parent instanceof AbsListView) {
            mAbsListView = (AbsListView) parent;
        }

        if (isHeaderView(position)) {
            return mHeaderView;
        } else if (isFooterView(position)) {
            return mFooterView;
        }

        final CustomViewHolder holder = onCreate(convertView, parent, getItemViewType(position));
        T item = getItem(position);
        onBind(holder, getItemViewType(position), position, item);
        return holder.itemView;
    }

    /**
     * @see android.widget.BaseAdapter#getViewTypeCount().
     */
    @Override
    public int getViewTypeCount() {
        if (mMultiItemViewType != null) {
            return mMultiItemViewType.getViewTypeCount();
        }
        return 1;
    }

    /**
     * Note that you must override this method if using <code>ListView</code> with multiple item
     * types.
     * <p>
     * 在使用ListView的多布局的情况下,你必须重写此方法,因为ListView和RV的实现机制不同。
     *
     * @see android.widget.BaseAdapter#getItemViewType(int).
     */
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    /**
     * @see android.widget.BaseAdapter#areAllItemsEnabled().
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * @see android.widget.BaseAdapter#isEnabled(int).
     */
    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    //    @Override
    //    public void registerDataSetObserver(DataSetObserver observer) {
    //        mDataSetObservable.registerObserver(observer);
    //    }

    //    @Override
    //    public void unregisterDataSetObserver(DataSetObserver observer) {
    //        mDataSetObservable.unregisterObserver(observer);
    //    }

    //    public void notifyDataSetNotifyChanged() {
    //        mDataSetObservable.notifyChanged();
    //    }

    //    public void notifyDataSetNotifyInvalidated() {
    //        mDataSetObservable.notifyInvalidated();
    //    }

    @Override
    public void addHeaderView(View header) {
        if (isListView() && header != null) {
            ((ListView) mAbsListView).addHeaderView(header);
            mHeaderView = header;
        }
    }

    @Override
    public boolean removeHeaderView() {
        boolean result = false;
        if (isListView() && hasHeaderView()) {
            result = ((ListView) mAbsListView).removeHeaderView(mHeaderView);
            mHeaderView = null;
            return result;
        }
        return result;
    }

    @Override
    public boolean hasHeaderView() {
        if (isListView()) {
            return ((ListView) mAbsListView).getHeaderViewsCount() > 0;
        }
        return false;
    }

    @Override
    public void addFooterView(View footer) {
        if (isListView() && footer != null) {
            ((ListView) mAbsListView).addFooterView(footer);
            mFooterView = footer;
        }
    }

    @Override
    public boolean removeFooterView() {
        boolean result = false;
        if (isListView() && hasFooterView()) {
            result = ((ListView) mAbsListView).removeFooterView(mFooterView);
            mFooterView = null;
            return result;
        }
        return result;
    }

    @Override
    public boolean hasFooterView() {
        if (isListView()) {
            return ((ListView) mAbsListView).getFooterViewsCount() > 0;
        }
        return false;
    }

    @Override
    public boolean isHeaderView(int position) {
        return hasHeaderView() && position == 0;
    }

    @Override
    public boolean isFooterView(int position) {
        return hasFooterView() && position == getCount() - 1;
    }

    @Override
    public View getHeaderView() {
        return mFooterView;
    }

    @Override
    public View getFooterView() {
        return mHeaderView;
    }

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

    private boolean isListView() {
        return mAbsListView != null && mAbsListView instanceof ListView;
    }

}
