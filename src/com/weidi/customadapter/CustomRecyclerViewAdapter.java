package com.weidi.customadapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.customadapter.interfaces.CRUD;
import com.weidi.customadapter.interfaces.DefaultDiffCallback;
import com.weidi.customadapter.interfaces.IMultiItemViewType;

import java.util.List;

/***
 构造器必须要调用一下父类
 <p>
 //这里用线性显示 类似于listview
 recyclerView.setLayoutManager(new LinearLayoutManager(this));
 //这里用线性宫格显示 类似于grid view
 recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
 //这里用线性宫格显示 类似于瀑布流
 recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));
 recyclerView.setAdapter(new NormalRecyclerViewAdapter(this));

 目标: 在任何一个位置可以加入view

 @param <T> javaBean
 */
public abstract class CustomRecyclerViewAdapter<T> extends RecyclerViewAdapter<T>
        implements
        CRUD<T> {

    private static final String TAG =
            CustomRecyclerViewAdapter.class.getSimpleName();

    private LayoutInflater mLayoutInflater;

    /**
     * Constructor for single itemView type.
     */
    public CustomRecyclerViewAdapter(
            Context context,
            List<T> items,
            int layoutResId) {
        super(context, items, layoutResId);
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * Constructor for multiple itemView types.
     */
    public CustomRecyclerViewAdapter(
            Context context,
            List<T> items,
            IMultiItemViewType<T> mulItemViewType) {
        super(context, items, mulItemViewType);
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * 子类不需要再fu写这个方法了
     * 只要子类根据需要创建不同的构造器,在这里就能得到不同的布局
     * 如果是多类型的,必须实现IMultiItemViewType接口
     *
     * @param convertView
     * @param parent      Target container(ListView, GridView, RecyclerView,Spinner, etc.).
     * @param viewType    Choose the layout resource according to view type.
     * @return
     */
    @Override
    public CustomViewHolder onCreate(@Nullable View convertView, ViewGroup parent, int viewType) {
        @LayoutRes int resource;
        if (mMultiItemViewType != null) {
            // 根据不同的类型得到不同的而已
            resource = mMultiItemViewType.getLayoutId(viewType);
        } else {
            resource = mLayoutResId;
        }
        return CustomViewHolder.get(
                convertView,
                convertView == null ? mLayoutInflater.inflate(resource, parent, false) : null);
    }

    //    public abstract void onBind(
    // CustomViewHolder holder, int viewType, int layoutPosition, T item);

    //------------------------------------ CRUD ------------------------------------//

    /**
     * 不能用来加"头"或者"脚"
     *
     * @param item
     */
    @Override
    public final void add(T item) {
        mData.add(item);
        int location = mData.size() - 1;
        if (hasHeaderView()) {
            ++location;
        }
        notifyItemInserted(location);
        notifyDataSetChanged();
    }

    @Override
    public void add(int location, T item) {
        mData.add(location, item);
        if (hasHeaderView()) {
            ++location;
        }
        notifyItemInserted(location);
        notifyDataSetChanged();
    }

    @Override
    public final void addAll(List<T> items) {
        if (items == null || items.isEmpty()) {
            Log.w(TAG, "addAll: The list you passed contains no elements.");
            return;
        }
        int location = mData.size();
        mData.addAll(items);
        if (hasHeaderView()) {
            ++location;
        }
        notifyItemRangeInserted(location, items.size());
        notifyDataSetChanged();
    }

    @Override
    public void addAll(int location, List<T> items) {
        if (items == null || items.isEmpty()) {
            Log.w(TAG, "addAll: The list you passed contains no elements.");
            return;
        }
        if (location < 0 || location > mData.size()) {
            Log.w(TAG, "addAll: IndexOutOfBoundsException");
            return;
        }
        mData.addAll(location, items);
        if (hasHeaderView()) {
            ++location;
        }
        notifyItemRangeInserted(location, items.size());
        notifyDataSetChanged();
    }

    @Override
    public final void remove(T item) {
        if (contains(item)) {
            remove(mData.indexOf(item));
        }
    }

    @Override
    public final void remove(int location) {
        mData.remove(location);
        if (hasHeaderView()) {
            ++location;
        }
        notifyItemRemoved(location);
        notifyDataSetChanged();
    }

    @Override
    public void removeAll(List<T> items) {
        mData.removeAll(items);
        notifyDataSetChanged();
    }

    @Override
    public void retainAll(List<T> items) {
        mData.retainAll(items);
        notifyDataSetChanged();
    }

    @Override
    public final void set(T oldItem, T newItem) {
        set(mData.indexOf(oldItem), newItem);
    }

    @Override
    public final void set(int location, T item) {
        mData.set(location, item);
        if (hasHeaderView()) {
            ++location;
        }
        notifyItemChanged(location);
        notifyDataSetChanged();
    }

    @Override
    public final void replaceAll(List<T> items) {
        if (mData == items) {
            notifyDataSetChanged();
            return;
        }
        if (items == null || items.isEmpty()) {
            clear();
            return;
        }
        if (mData.isEmpty()) {
            addAll(items);
        } else {
            int start = hasHeaderView() ? 1 : 0;
            int originalSize = getItemCount();
            int newSize = items.size();
            mData.clear();
            mData.addAll(items);
            if (originalSize > newSize) {
                notifyItemRangeChanged(start, newSize);
                notifyItemRangeRemoved(start + newSize, originalSize - newSize);
            } else if (originalSize == newSize) {
                notifyItemRangeChanged(start, newSize);
            } else {
                notifyItemRangeChanged(start, originalSize);
                notifyItemRangeInserted(start + originalSize, newSize - originalSize);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public final boolean contains(T item) {
        return mData.contains(item);
    }

    @Override
    public boolean containsAll(List<T> items) {
        return mData.containsAll(items);
    }

    @Override
    public final void clear() {
        int count = getItemCount();
        if (count > 0) {
            mData.clear();
            notifyItemRangeRemoved(hasHeaderView() ? 1 : 0, count);
            notifyDataSetChanged();
        }
    }

    /**
     * Calculate the difference between two lists and output a list of update operations
     * that converts the first list into the second one.
     * <pre>
     *     List oldList = mAdapter.getData();
     *     DefaultDiffCallback<T> callback = new DefaultDiffCallback(oldList, newList);
     *     mAdapter.diff(callback);
     * </pre>
     * Note: This method only works on revision 24.2.0 or above.
     *
     * @param callback {@link DefaultDiffCallback}
     */
    @Override
    public void diff(final DefaultDiffCallback<T> callback) {
        if (checkDiff(callback)) {
            new AsyncTask<Void, Void, DiffUtil.DiffResult>() {
                @Override
                protected DiffUtil.DiffResult doInBackground(Void... params) {
                    return DiffUtil.calculateDiff(callback);
                }

                @Override
                protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                    setData(callback.getNewList());
                    if (diffResult != null) {
                        diffResult.dispatchUpdatesTo(CustomRecyclerViewAdapter.this);
                    }
                }
            }.execute();
        }
    }

    private boolean checkDiff(DiffUtil.Callback callback) {
        if (mRecyclerView == null) {
            throw new IllegalStateException("'diff(DefaultDiffCallback)' only works with " +
                    "RecyclerView");
        }

        if (callback == null || callback.getNewListSize() < 1) {
            Log.w(TAG, "Invalid size of the new list.");
            return false;
        }

        try {
            Class.forName("android.support.v7.util.DiffUtil");
            return true;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "This method only works on revision 24.2.0 or above.", e);
            return false;
        }
    }

}
