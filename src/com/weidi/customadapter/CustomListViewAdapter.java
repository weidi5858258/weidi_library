package com.weidi.customadapter;

import android.content.Context;
import android.os.AsyncTask;

import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weidi.customadapter.interfaces.CRUD;
import com.weidi.customadapter.interfaces.DefaultDiffCallback;
import com.weidi.customadapter.interfaces.IMultiItemViewType;

import java.util.Iterator;
import java.util.List;

import androidx.annotation.LayoutRes;

/**
 * 构造器必须要调用一下父类
 *
 * @param <T>
 */
public abstract class CustomListViewAdapter<T> extends ListViewAdapter<T> implements CRUD<T> {

    private static final String TAG = "CustomListViewAdapter";

    private LayoutInflater mLayoutInflater;

    /**
     * Constructor for single itemView type.
     */
    public CustomListViewAdapter(
            Context context,
            List<T> items,
            int layoutResId) {
        super(context, items, layoutResId);
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * Constructor for multiple itemView types.
     */
    public CustomListViewAdapter(
            Context context,
            List<T> items,
            IMultiItemViewType<T> mulItemViewType) {
        super(context, items, mulItemViewType);
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public CustomViewHolder onCreate(View convertView, ViewGroup parent, int viewType) {
        @LayoutRes int resource;
        if (mMultiItemViewType != null) {
            resource = mMultiItemViewType.getLayoutId(viewType);
        } else {
            resource = mLayoutResId;
        }
        return CustomViewHolder.get(
                convertView,
                convertView == null ? mLayoutInflater.inflate(resource, parent, false) : null);
    }

    /**
     * ------------------------------------ CRUD ------------------------------------
     */

    @Override
    public final void add(T item) {
        //        mData.add(item);
        //        int location = mData.size() - 1;
        //        if (hasHeaderView()) {
        //            ++location;
        //        }

        if (hasFooterView()) {
            View footerView = getFooterView();
            if (removeFooterView()) {
                mData.add(item);
                addFooterView(footerView);
            }
        } else {
            mData.add(item);
        }
        notifyDataSetChanged();
    }

    @Override
    public void add(int location, T item) {
        //        mData.add(location, item);
        //        if (hasHeaderView()) {
        //            ++location;
        //        }

        int count = getCount();
        if (location < 0 || location > count - 1) {
            Log.d(TAG, "");
            return;
        }

        if (!hasHeaderView() && !hasFooterView()) {
            mData.add(location, item);
        } else if (!hasHeaderView() && hasFooterView()) {
            if (location == count - 1) {
                View footerView = getFooterView();
                if (removeFooterView()) {
                    mData.add(location, item);
                    addFooterView(footerView);
                }
            } else {
                mData.add(location, item);
            }
        } else if (hasHeaderView() && !hasFooterView()) {
            if (location == 0) {
                View headerView = getHeaderView();
                if (removeHeaderView()) {
                    mData.add(location, item);
                    addHeaderView(headerView);
                }
            } else {
                mData.add(location, item);
            }
        } else if (hasHeaderView() && hasFooterView()) {
            if (location == 0) {
                View headerView = getHeaderView();
                if (removeHeaderView()) {
                    mData.add(location, item);
                    addHeaderView(headerView);
                }
            } else if (location == count - 1) {
                View footerView = getFooterView();
                if (removeFooterView()) {
                    mData.add(location, item);
                    addFooterView(footerView);
                }
            } else {
                mData.add(location, item);
            }
        }

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
        notifyDataSetChanged();
    }

    @Override
    public void addAll(int location, List<T> items) {
        if (items == null || items.isEmpty()) {
            Log.w(TAG, "addAll: The list you passed contains no elements.");
            return;
        }
        if (location < 0 || location > getCount()) {
            Log.w(TAG, "addAll: IndexOutOfBoundsException");
            return;
        }
        mData.addAll(location, items);
        if (hasHeaderView()) {
            ++location;
        }
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
        notifyDataSetChanged();
    }

    /**
     * 待测试
     *
     * @param start
     * @param end
     */
    public void remove(int start, int end) {
        if (start > end || start < 0 || end > getCount() - 1) {
            Log.d(TAG, "到CustomListViewAdapter类中查看参数是否正确1.");
            return;
        }
        int deletePosition = -1;
        if (start == end) {
            deletePosition = start;
        }

        if (start == 0 && hasHeaderView()) {
            start = 1;
        }
        if (end == getCount() - 1 && hasFooterView()) {
            end = getCount() - 2;
        }
        if (start > end) {
            Log.d(TAG, "到CustomListViewAdapter类中查看参数是否正确2.");
            return;
        }

        Iterator<T> iter = mData.iterator();
        int deleteIndex = 0;

        while (iter.hasNext()) {
            int index = deleteIndex++;
            if (index >= start && index <= end) {
                iter.next();
                iter.remove();
            } else {
                break;
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public void removeAll(List<T> items) {
        mData.removeAll(items);
        notifyDataSetChanged(); // RecyclerView
        notifyDataSetChanged(); // AdapterView
    }

    @Override
    public void retainAll(List<T> items) {
        mData.retainAll(items);
        notifyDataSetChanged(); // RecyclerView
        notifyDataSetChanged(); // AdapterView
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
        notifyDataSetChanged();
    }

    @Override
    public final void replaceAll(List<T> items) {
        if (mData == items) {
            notifyDataSetChanged();
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
            int originalSize = getCount();
            int newSize = items.size();
            mData.clear();
            mData.addAll(items);
            if (originalSize > newSize) {
                //                notifyItemRangeChanged(start, newSize);
                //                notifyItemRangeRemoved(start + newSize, originalSize - newSize);
            } else if (originalSize == newSize) {
                //                notifyItemRangeChanged(start, newSize);
            } else {
                //                notifyItemRangeChanged(start, originalSize);
                //                notifyItemRangeInserted(start + originalSize, newSize -
                // originalSize);
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
        int count = getCount();
        if (count > 0) {
            mData.clear();
            //            notifyItemRangeRemoved(hasHeaderView() ? 1 : 0, count);
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
                        //                        diffResult.dispatchUpdatesTo
                        // (CustomListViewAdapter.this);
                    }
                }
            }.execute();
        }
    }

    private boolean checkDiff(DiffUtil.Callback callback) {
        //        if (mRecyclerView == null) {
        //            throw new IllegalStateException("'diff(DefaultDiffCallback)' only works
        // with " +
        //                    "RecyclerView");
        //        }

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
