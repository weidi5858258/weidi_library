package com.weidi.customadapter.interfaces;


import android.view.View;
import android.view.ViewGroup;

/**
 *
 * @param <T> javabean
 * @param <CustomViewHolder>
 */
public interface IViewBindData<T, CustomViewHolder> {

    /**
     * @param convertView Support by {@link ListSupportAdapter#getView(int, View, ViewGroup)}.
     * @param parent      Target container(ListView, GridView, RecyclerView,Spinner, etc.).
     * @param viewType    Choose the layout resource according to view type.
     * @return Created view holder.
     */
    CustomViewHolder onCreate(View convertView, ViewGroup parent, int viewType);

    /**
     * Method for binding data to view.
     *
     * @param holder         ViewHolder
     * @param viewType       {@link CustomRecyclerViewAdapter#getItemViewType(int)}
     * @param layoutPosition position
     * @param item           data
     */
    void onBind(CustomViewHolder holder, int viewType, int layoutPosition, T item);

}
