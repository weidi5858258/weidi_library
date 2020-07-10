//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.util;



import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

public class SortedList<T> {
    public static final int INVALID_POSITION = -1;
    private static final int MIN_CAPACITY = 10;
    private static final int CAPACITY_GROWTH = 10;
    private static final int INSERTION = 1;
    private static final int DELETION = 2;
    private static final int LOOKUP = 4;
    T[] mData;
    private T[] mOldData;
    private int mOldDataStart;
    private int mOldDataSize;
    private int mNewDataStart;
    private SortedList.Callback mCallback;
    private SortedList.BatchedCallback mBatchedCallback;
    private int mSize;
    private final Class<T> mTClass;

    public SortedList(Class<T> klass, SortedList.Callback<T> callback) {
        this(klass, callback, 10);
    }

    public SortedList(Class<T> klass, SortedList.Callback<T> callback, int initialCapacity) {
        this.mTClass = klass;
        this.mData = (T[])(Array.newInstance(klass, initialCapacity));
        this.mCallback = callback;
        this.mSize = 0;
    }

    public int size() {
        return this.mSize;
    }

    public int add(T item) {
        this.throwIfInMutationOperation();
        return this.add(item, true);
    }

    public void addAll(T[] items, boolean mayModifyInput) {
        this.throwIfInMutationOperation();
        if(items.length != 0) {
            if(mayModifyInput) {
                this.addAllInternal(items);
            } else {
                this.addAllInternal(this.copyArray(items));
            }

        }
    }

    public void addAll(T... items) {
        this.addAll(items, false);
    }

    public void addAll(Collection<T> items) {
        T[] copy = (T[])(Array.newInstance(this.mTClass, items.size()));
        this.addAll(items.toArray(copy), true);
    }

    public void replaceAll(T[] items, boolean mayModifyInput) {
        this.throwIfInMutationOperation();
        if(mayModifyInput) {
            this.replaceAllInternal(items);
        } else {
            this.replaceAllInternal(this.copyArray(items));
        }

    }

    public void replaceAll(T... items) {
        this.replaceAll(items, false);
    }

    public void replaceAll(Collection<T> items) {
        T[] copy = (T[])(Array.newInstance(this.mTClass, items.size()));
        this.replaceAll(items.toArray(copy), true);
    }

    private void addAllInternal(T[] newItems) {
        if(newItems.length >= 1) {
            int newSize = this.sortAndDedup(newItems);
            if(this.mSize == 0) {
                this.mData = newItems;
                this.mSize = newSize;
                this.mCallback.onInserted(0, newSize);
            } else {
                this.merge(newItems, newSize);
            }

        }
    }

    private void replaceAllInternal(T[] newData) {
        boolean forceBatchedUpdates = !(this.mCallback instanceof SortedList.BatchedCallback);
        if(forceBatchedUpdates) {
            this.beginBatchedUpdates();
        }

        this.mOldDataStart = 0;
        this.mOldDataSize = this.mSize;
        this.mOldData = this.mData;
        this.mNewDataStart = 0;
        int newSize = this.sortAndDedup(newData);
        this.mData = (T[])(Array.newInstance(this.mTClass, newSize));

        while(this.mNewDataStart < newSize || this.mOldDataStart < this.mOldDataSize) {
            int itemCount;
            if(this.mOldDataStart >= this.mOldDataSize) {
                itemCount = this.mNewDataStart;
                itemCount = newSize - this.mNewDataStart;
                System.arraycopy(newData, itemCount, this.mData, itemCount, itemCount);
                this.mNewDataStart += itemCount;
                this.mSize += itemCount;
                this.mCallback.onInserted(itemCount, itemCount);
                break;
            }

            if(this.mNewDataStart >= newSize) {
                itemCount = this.mOldDataSize - this.mOldDataStart;
                this.mSize -= itemCount;
                this.mCallback.onRemoved(this.mNewDataStart, itemCount);
                break;
            }

            T oldItem = this.mOldData[this.mOldDataStart];
            T newItem = newData[this.mNewDataStart];
            int result = this.mCallback.compare(oldItem, newItem);
            if(result < 0) {
                this.replaceAllRemove();
            } else if(result > 0) {
                this.replaceAllInsert(newItem);
            } else if(!this.mCallback.areItemsTheSame(oldItem, newItem)) {
                this.replaceAllRemove();
                this.replaceAllInsert(newItem);
            } else {
                this.mData[this.mNewDataStart] = newItem;
                ++this.mOldDataStart;
                ++this.mNewDataStart;
                if(!this.mCallback.areContentsTheSame(oldItem, newItem)) {
                    this.mCallback.onChanged(this.mNewDataStart - 1, 1, this.mCallback.getChangePayload(oldItem, newItem));
                }
            }
        }

        this.mOldData = null;
        if(forceBatchedUpdates) {
            this.endBatchedUpdates();
        }

    }

    private void replaceAllInsert(T newItem) {
        this.mData[this.mNewDataStart] = newItem;
        ++this.mNewDataStart;
        ++this.mSize;
        this.mCallback.onInserted(this.mNewDataStart - 1, 1);
    }

    private void replaceAllRemove() {
        --this.mSize;
        ++this.mOldDataStart;
        this.mCallback.onRemoved(this.mNewDataStart, 1);
    }

    private int sortAndDedup(T[] items) {
        if(items.length == 0) {
            return 0;
        } else {
            Arrays.sort(items, this.mCallback);
            int rangeStart = 0;
            int rangeEnd = 1;

            for(int i = 1; i < items.length; ++i) {
                T currentItem = items[i];
                int compare = this.mCallback.compare(items[rangeStart], currentItem);
                if(compare == 0) {
                    int sameItemPos = this.findSameItem(currentItem, items, rangeStart, rangeEnd);
                    if(sameItemPos != -1) {
                        items[sameItemPos] = currentItem;
                    } else {
                        if(rangeEnd != i) {
                            items[rangeEnd] = currentItem;
                        }

                        ++rangeEnd;
                    }
                } else {
                    if(rangeEnd != i) {
                        items[rangeEnd] = currentItem;
                    }

                    rangeStart = rangeEnd++;
                }
            }

            return rangeEnd;
        }
    }

    private int findSameItem(T item, T[] items, int from, int to) {
        for(int pos = from; pos < to; ++pos) {
            if(this.mCallback.areItemsTheSame(items[pos], item)) {
                return pos;
            }
        }

        return -1;
    }

    private void merge(T[] newData, int newDataSize) {
        boolean forceBatchedUpdates = !(this.mCallback instanceof SortedList.BatchedCallback);
        if(forceBatchedUpdates) {
            this.beginBatchedUpdates();
        }

        this.mOldData = this.mData;
        this.mOldDataStart = 0;
        this.mOldDataSize = this.mSize;
        int mergedCapacity = this.mSize + newDataSize + 10;
        this.mData = (T[])(Array.newInstance(this.mTClass, mergedCapacity));
        this.mNewDataStart = 0;
        int newDataStart = 0;

        while(this.mOldDataStart < this.mOldDataSize || newDataStart < newDataSize) {
            int itemCount;
            if(this.mOldDataStart == this.mOldDataSize) {
                itemCount = newDataSize - newDataStart;
                System.arraycopy(newData, newDataStart, this.mData, this.mNewDataStart, itemCount);
                this.mNewDataStart += itemCount;
                this.mSize += itemCount;
                this.mCallback.onInserted(this.mNewDataStart - itemCount, itemCount);
                break;
            }

            if(newDataStart == newDataSize) {
                itemCount = this.mOldDataSize - this.mOldDataStart;
                System.arraycopy(this.mOldData, this.mOldDataStart, this.mData, this.mNewDataStart, itemCount);
                this.mNewDataStart += itemCount;
                break;
            }

            T oldItem = this.mOldData[this.mOldDataStart];
            T newItem = newData[newDataStart];
            int compare = this.mCallback.compare(oldItem, newItem);
            if(compare > 0) {
                this.mData[this.mNewDataStart++] = newItem;
                ++this.mSize;
                ++newDataStart;
                this.mCallback.onInserted(this.mNewDataStart - 1, 1);
            } else if(compare == 0 && this.mCallback.areItemsTheSame(oldItem, newItem)) {
                this.mData[this.mNewDataStart++] = newItem;
                ++newDataStart;
                ++this.mOldDataStart;
                if(!this.mCallback.areContentsTheSame(oldItem, newItem)) {
                    this.mCallback.onChanged(this.mNewDataStart - 1, 1, this.mCallback.getChangePayload(oldItem, newItem));
                }
            } else {
                this.mData[this.mNewDataStart++] = oldItem;
                ++this.mOldDataStart;
            }
        }

        this.mOldData = null;
        if(forceBatchedUpdates) {
            this.endBatchedUpdates();
        }

    }

    private void throwIfInMutationOperation() {
        if(this.mOldData != null) {
            throw new IllegalStateException("Data cannot be mutated in the middle of a batch update operation such as addAll or replaceAll.");
        }
    }

    public void beginBatchedUpdates() {
        this.throwIfInMutationOperation();
        if(!(this.mCallback instanceof SortedList.BatchedCallback)) {
            if(this.mBatchedCallback == null) {
                this.mBatchedCallback = new SortedList.BatchedCallback(this.mCallback);
            }

            this.mCallback = this.mBatchedCallback;
        }
    }

    public void endBatchedUpdates() {
        this.throwIfInMutationOperation();
        if(this.mCallback instanceof SortedList.BatchedCallback) {
            ((SortedList.BatchedCallback)this.mCallback).dispatchLastEvent();
        }

        if(this.mCallback == this.mBatchedCallback) {
            this.mCallback = this.mBatchedCallback.mWrappedCallback;
        }

    }

    private int add(T item, boolean notify) {
        int index = this.findIndexOf(item, this.mData, 0, this.mSize, 1);
        if(index == -1) {
            index = 0;
        } else if(index < this.mSize) {
            T existing = this.mData[index];
            if(this.mCallback.areItemsTheSame(existing, item)) {
                if(this.mCallback.areContentsTheSame(existing, item)) {
                    this.mData[index] = item;
                    return index;
                }

                this.mData[index] = item;
                this.mCallback.onChanged(index, 1, this.mCallback.getChangePayload(existing, item));
                return index;
            }
        }

        this.addToData(index, item);
        if(notify) {
            this.mCallback.onInserted(index, 1);
        }

        return index;
    }

    public boolean remove(T item) {
        this.throwIfInMutationOperation();
        return this.remove(item, true);
    }

    public T removeItemAt(int index) {
        this.throwIfInMutationOperation();
        T item = this.get(index);
        this.removeItemAtIndex(index, true);
        return item;
    }

    private boolean remove(T item, boolean notify) {
        int index = this.findIndexOf(item, this.mData, 0, this.mSize, 2);
        if(index == -1) {
            return false;
        } else {
            this.removeItemAtIndex(index, notify);
            return true;
        }
    }

    private void removeItemAtIndex(int index, boolean notify) {
        System.arraycopy(this.mData, index + 1, this.mData, index, this.mSize - index - 1);
        --this.mSize;
        this.mData[this.mSize] = null;
        if(notify) {
            this.mCallback.onRemoved(index, 1);
        }

    }

    public void updateItemAt(int index, T item) {
        this.throwIfInMutationOperation();
        T existing = this.get(index);
        boolean contentsChanged = existing == item || !this.mCallback.areContentsTheSame(existing, item);
        int newIndex;
        if(existing != item) {
            newIndex = this.mCallback.compare(existing, item);
            if(newIndex == 0) {
                this.mData[index] = item;
                if(contentsChanged) {
                    this.mCallback.onChanged(index, 1, this.mCallback.getChangePayload(existing, item));
                }

                return;
            }
        }

        if(contentsChanged) {
            this.mCallback.onChanged(index, 1, this.mCallback.getChangePayload(existing, item));
        }

        this.removeItemAtIndex(index, false);
        newIndex = this.add(item, false);
        if(index != newIndex) {
            this.mCallback.onMoved(index, newIndex);
        }

    }

    public void recalculatePositionOfItemAt(int index) {
        this.throwIfInMutationOperation();
        T item = this.get(index);
        this.removeItemAtIndex(index, false);
        int newIndex = this.add(item, false);
        if(index != newIndex) {
            this.mCallback.onMoved(index, newIndex);
        }

    }

    public T get(int index) throws IndexOutOfBoundsException {
        if(index < this.mSize && index >= 0) {
            return this.mOldData != null && index >= this.mNewDataStart?this.mOldData[index - this.mNewDataStart + this.mOldDataStart]:this.mData[index];
        } else {
            throw new IndexOutOfBoundsException("Asked to get item at " + index + " but size is " + this.mSize);
        }
    }

    public int indexOf(T item) {
        if(this.mOldData != null) {
            int index = this.findIndexOf(item, this.mData, 0, this.mNewDataStart, 4);
            if(index != -1) {
                return index;
            } else {
                index = this.findIndexOf(item, this.mOldData, this.mOldDataStart, this.mOldDataSize, 4);
                return index != -1?index - this.mOldDataStart + this.mNewDataStart:-1;
            }
        } else {
            return this.findIndexOf(item, this.mData, 0, this.mSize, 4);
        }
    }

    private int findIndexOf(T item, T[] mData, int left, int right, int reason) {
        while(left < right) {
            int middle = (left + right) / 2;
            T myItem = mData[middle];
            int cmp = this.mCallback.compare(myItem, item);
            if(cmp < 0) {
                left = middle + 1;
            } else {
                if(cmp == 0) {
                    if(this.mCallback.areItemsTheSame(myItem, item)) {
                        return middle;
                    }

                    int exact = this.linearEqualitySearch(item, middle, left, right);
                    if(reason == 1) {
                        return exact == -1?middle:exact;
                    }

                    return exact;
                }

                right = middle;
            }
        }

        return reason == 1?left:-1;
    }

    private int linearEqualitySearch(T item, int middle, int left, int right) {
        int next;
        Object nextItem;
        int cmp;
        for(next = middle - 1; next >= left; --next) {
            nextItem = this.mData[next];
            cmp = this.mCallback.compare(nextItem, item);
            if(cmp != 0) {
                break;
            }

            if(this.mCallback.areItemsTheSame(nextItem, item)) {
                return next;
            }
        }

        for(next = middle + 1; next < right; ++next) {
            nextItem = this.mData[next];
            cmp = this.mCallback.compare(nextItem, item);
            if(cmp != 0) {
                break;
            }

            if(this.mCallback.areItemsTheSame(nextItem, item)) {
                return next;
            }
        }

        return -1;
    }

    private void addToData(int index, T item) {
        if(index > this.mSize) {
            throw new IndexOutOfBoundsException("cannot add item to " + index + " because size is " + this.mSize);
        } else {
            if(this.mSize == this.mData.length) {
                T[] newData = (T[])(Array.newInstance(this.mTClass, this.mData.length + 10));
                System.arraycopy(this.mData, 0, newData, 0, index);
                newData[index] = item;
                System.arraycopy(this.mData, index, newData, index + 1, this.mSize - index);
                this.mData = newData;
            } else {
                System.arraycopy(this.mData, index, this.mData, index + 1, this.mSize - index);
                this.mData[index] = item;
            }

            ++this.mSize;
        }
    }

    private T[] copyArray(T[] items) {
        T[] copy = (T[])Array.newInstance(this.mTClass, items.length);
        System.arraycopy(items, 0, copy, 0, items.length);
        return copy;
    }

    public void clear() {
        this.throwIfInMutationOperation();
        if(this.mSize != 0) {
            int prevSize = this.mSize;
            Arrays.fill(this.mData, 0, prevSize, (Object)null);
            this.mSize = 0;
            this.mCallback.onRemoved(0, prevSize);
        }
    }

    public static class BatchedCallback<T2> extends SortedList.Callback<T2> {
        final SortedList.Callback<T2> mWrappedCallback;
        private final BatchingListUpdateCallback mBatchingListUpdateCallback;

        public BatchedCallback(SortedList.Callback<T2> wrappedCallback) {
            this.mWrappedCallback = wrappedCallback;
            this.mBatchingListUpdateCallback = new BatchingListUpdateCallback(this.mWrappedCallback);
        }

        public int compare(T2 o1, T2 o2) {
            return this.mWrappedCallback.compare(o1, o2);
        }

        public void onInserted(int position, int count) {
            this.mBatchingListUpdateCallback.onInserted(position, count);
        }

        public void onRemoved(int position, int count) {
            this.mBatchingListUpdateCallback.onRemoved(position, count);
        }

        public void onMoved(int fromPosition, int toPosition) {
            this.mBatchingListUpdateCallback.onMoved(fromPosition, toPosition);
        }

        public void onChanged(int position, int count) {
            this.mBatchingListUpdateCallback.onChanged(position, count, (Object)null);
        }

        public void onChanged(int position, int count, Object payload) {
            this.mBatchingListUpdateCallback.onChanged(position, count, payload);
        }

        public boolean areContentsTheSame(T2 oldItem, T2 newItem) {
            return this.mWrappedCallback.areContentsTheSame(oldItem, newItem);
        }

        public boolean areItemsTheSame(T2 item1, T2 item2) {
            return this.mWrappedCallback.areItemsTheSame(item1, item2);
        }


        public Object getChangePayload(T2 item1, T2 item2) {
            return this.mWrappedCallback.getChangePayload(item1, item2);
        }

        public void dispatchLastEvent() {
            this.mBatchingListUpdateCallback.dispatchLastEvent();
        }
    }

    public abstract static class Callback<T2> implements Comparator<T2>, ListUpdateCallback {
        public Callback() {
        }

        public abstract int compare(T2 var1, T2 var2);

        public abstract void onChanged(int var1, int var2);

        public void onChanged(int position, int count, Object payload) {
            this.onChanged(position, count);
        }

        public abstract boolean areContentsTheSame(T2 var1, T2 var2);

        public abstract boolean areItemsTheSame(T2 var1, T2 var2);


        public Object getChangePayload(T2 item1, T2 item2) {
            return null;
        }
    }
}
