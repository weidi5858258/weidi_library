//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.util;

import android.support.v7.util.ThreadUtil.BackgroundCallback;
import android.support.v7.util.ThreadUtil.MainThreadCallback;
import android.support.v7.util.TileList.Tile;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

public class AsyncListUtil<T> {
    static final String TAG = "AsyncListUtil";
    static final boolean DEBUG = false;
    final Class<T> mTClass;
    final int mTileSize;
    final AsyncListUtil.DataCallback<T> mDataCallback;
    final AsyncListUtil.ViewCallback mViewCallback;
    final TileList<T> mTileList;
    final MainThreadCallback<T> mMainThreadProxy;
    final BackgroundCallback<T> mBackgroundProxy;
    final int[] mTmpRange = new int[2];
    final int[] mPrevRange = new int[2];
    final int[] mTmpRangeExtended = new int[2];
    boolean mAllowScrollHints;
    private int mScrollHint = 0;
    int mItemCount = 0;
    int mDisplayedGeneration = 0;
    int mRequestedGeneration;
    final SparseIntArray mMissingPositions;
    private final MainThreadCallback<T> mMainThreadCallback;
    private final BackgroundCallback<T> mBackgroundCallback;

    void log(String s, Object... args) {
        Log.d("AsyncListUtil", "[MAIN] " + String.format(s, args));
    }

    public AsyncListUtil(Class<T> klass, int tileSize, AsyncListUtil.DataCallback<T> dataCallback, AsyncListUtil.ViewCallback viewCallback) {
        this.mRequestedGeneration = this.mDisplayedGeneration;
        this.mMissingPositions = new SparseIntArray();
        this.mMainThreadCallback = new MainThreadCallback<T>() {
            public void updateItemCount(int generation, int itemCount) {
                if(this.isRequestedGeneration(generation)) {
                    AsyncListUtil.this.mItemCount = itemCount;
                    AsyncListUtil.this.mViewCallback.onDataRefresh();
                    AsyncListUtil.this.mDisplayedGeneration = AsyncListUtil.this.mRequestedGeneration;
                    this.recycleAllTiles();
                    AsyncListUtil.this.mAllowScrollHints = false;
                    AsyncListUtil.this.updateRange();
                }
            }

            public void addTile(int generation, Tile<T> tile) {
                if(!this.isRequestedGeneration(generation)) {
                    AsyncListUtil.this.mBackgroundProxy.recycleTile(tile);
                } else {
                    Tile<T> duplicate = AsyncListUtil.this.mTileList.addOrReplace(tile);
                    if(duplicate != null) {
                        Log.e("AsyncListUtil", "duplicate tile @" + duplicate.mStartPosition);
                        AsyncListUtil.this.mBackgroundProxy.recycleTile(duplicate);
                    }

                    int endPosition = tile.mStartPosition + tile.mItemCount;
                    int index = 0;

                    while(true) {
                        while(index < AsyncListUtil.this.mMissingPositions.size()) {
                            int position = AsyncListUtil.this.mMissingPositions.keyAt(index);
                            if(tile.mStartPosition <= position && position < endPosition) {
                                AsyncListUtil.this.mMissingPositions.removeAt(index);
                                AsyncListUtil.this.mViewCallback.onItemLoaded(position);
                            } else {
                                ++index;
                            }
                        }

                        return;
                    }
                }
            }

            public void removeTile(int generation, int position) {
                if(this.isRequestedGeneration(generation)) {
                    Tile<T> tile = AsyncListUtil.this.mTileList.removeAtPos(position);
                    if(tile == null) {
                        Log.e("AsyncListUtil", "tile not found @" + position);
                    } else {
                        AsyncListUtil.this.mBackgroundProxy.recycleTile(tile);
                    }
                }
            }

            private void recycleAllTiles() {
                for(int i = 0; i < AsyncListUtil.this.mTileList.size(); ++i) {
                    AsyncListUtil.this.mBackgroundProxy.recycleTile(AsyncListUtil.this.mTileList.getAtIndex(i));
                }

                AsyncListUtil.this.mTileList.clear();
            }

            private boolean isRequestedGeneration(int generation) {
                return generation == AsyncListUtil.this.mRequestedGeneration;
            }
        };
        this.mBackgroundCallback = new BackgroundCallback<T>() {
            private Tile<T> mRecycledRoot;
            final SparseBooleanArray mLoadedTiles = new SparseBooleanArray();
            private int mGeneration;
            private int mItemCount;
            private int mFirstRequiredTileStart;
            private int mLastRequiredTileStart;

            public void refresh(int generation) {
                this.mGeneration = generation;
                this.mLoadedTiles.clear();
                this.mItemCount = AsyncListUtil.this.mDataCallback.refreshData();
                AsyncListUtil.this.mMainThreadProxy.updateItemCount(this.mGeneration, this.mItemCount);
            }

            public void updateRange(int rangeStart, int rangeEnd, int extRangeStart, int extRangeEnd, int scrollHint) {
                if(rangeStart <= rangeEnd) {
                    int firstVisibleTileStart = this.getTileStart(rangeStart);
                    int lastVisibleTileStart = this.getTileStart(rangeEnd);
                    this.mFirstRequiredTileStart = this.getTileStart(extRangeStart);
                    this.mLastRequiredTileStart = this.getTileStart(extRangeEnd);
                    if(scrollHint == 1) {
                        this.requestTiles(this.mFirstRequiredTileStart, lastVisibleTileStart, scrollHint, true);
                        this.requestTiles(lastVisibleTileStart + AsyncListUtil.this.mTileSize, this.mLastRequiredTileStart, scrollHint, false);
                    } else {
                        this.requestTiles(firstVisibleTileStart, this.mLastRequiredTileStart, scrollHint, false);
                        this.requestTiles(this.mFirstRequiredTileStart, firstVisibleTileStart - AsyncListUtil.this.mTileSize, scrollHint, true);
                    }

                }
            }

            private int getTileStart(int position) {
                return position - position % AsyncListUtil.this.mTileSize;
            }

            private void requestTiles(int firstTileStart, int lastTileStart, int scrollHint, boolean backwards) {
                for(int i = firstTileStart; i <= lastTileStart; i += AsyncListUtil.this.mTileSize) {
                    int tileStart = backwards?lastTileStart + firstTileStart - i:i;
                    AsyncListUtil.this.mBackgroundProxy.loadTile(tileStart, scrollHint);
                }

            }

            public void loadTile(int position, int scrollHint) {
                if(!this.isTileLoaded(position)) {
                    Tile<T> tile = this.acquireTile();
                    tile.mStartPosition = position;
                    tile.mItemCount = Math.min(AsyncListUtil.this.mTileSize, this.mItemCount - tile.mStartPosition);
                    AsyncListUtil.this.mDataCallback.fillData(tile.mItems, tile.mStartPosition, tile.mItemCount);
                    this.flushTileCache(scrollHint);
                    this.addTile(tile);
                }
            }

            public void recycleTile(Tile<T> tile) {
                AsyncListUtil.this.mDataCallback.recycleData(tile.mItems, tile.mItemCount);
                tile.mNext = this.mRecycledRoot;
                this.mRecycledRoot = tile;
            }

            private Tile<T> acquireTile() {
                if(this.mRecycledRoot != null) {
                    Tile<T> result = this.mRecycledRoot;
                    this.mRecycledRoot = this.mRecycledRoot.mNext;
                    return result;
                } else {
                    return new Tile(AsyncListUtil.this.mTClass, AsyncListUtil.this.mTileSize);
                }
            }

            private boolean isTileLoaded(int position) {
                return this.mLoadedTiles.get(position);
            }

            private void addTile(Tile<T> tile) {
                this.mLoadedTiles.put(tile.mStartPosition, true);
                AsyncListUtil.this.mMainThreadProxy.addTile(this.mGeneration, tile);
            }

            private void removeTile(int position) {
                this.mLoadedTiles.delete(position);
                AsyncListUtil.this.mMainThreadProxy.removeTile(this.mGeneration, position);
            }

            private void flushTileCache(int scrollHint) {
                int cacheSizeLimit = AsyncListUtil.this.mDataCallback.getMaxCachedTiles();

                while(true) {
                    while(this.mLoadedTiles.size() >= cacheSizeLimit) {
                        int firstLoadedTileStart = this.mLoadedTiles.keyAt(0);
                        int lastLoadedTileStart = this.mLoadedTiles.keyAt(this.mLoadedTiles.size() - 1);
                        int startMargin = this.mFirstRequiredTileStart - firstLoadedTileStart;
                        int endMargin = lastLoadedTileStart - this.mLastRequiredTileStart;
                        if(startMargin <= 0 || startMargin < endMargin && scrollHint != 2) {
                            if(endMargin <= 0 || startMargin >= endMargin && scrollHint != 1) {
                                return;
                            }

                            this.removeTile(lastLoadedTileStart);
                        } else {
                            this.removeTile(firstLoadedTileStart);
                        }
                    }

                    return;
                }
            }

            private void log(String s, Object... args) {
                Log.d("AsyncListUtil", "[BKGR] " + String.format(s, args));
            }
        };
        this.mTClass = klass;
        this.mTileSize = tileSize;
        this.mDataCallback = dataCallback;
        this.mViewCallback = viewCallback;
        this.mTileList = new TileList(this.mTileSize);
        ThreadUtil<T> threadUtil = new MessageThreadUtil();
        this.mMainThreadProxy = threadUtil.getMainThreadProxy(this.mMainThreadCallback);
        this.mBackgroundProxy = threadUtil.getBackgroundProxy(this.mBackgroundCallback);
        this.refresh();
    }

    private boolean isRefreshPending() {
        return this.mRequestedGeneration != this.mDisplayedGeneration;
    }

    public void onRangeChanged() {
        if(!this.isRefreshPending()) {
            this.updateRange();
            this.mAllowScrollHints = true;
        }
    }

    public void refresh() {
        this.mMissingPositions.clear();
        this.mBackgroundProxy.refresh(++this.mRequestedGeneration);
    }


    public T getItem(int position) {
        if(position >= 0 && position < this.mItemCount) {
            T item = this.mTileList.getItemAt(position);
            if(item == null && !this.isRefreshPending()) {
                this.mMissingPositions.put(position, 0);
            }

            return item;
        } else {
            throw new IndexOutOfBoundsException(position + " is not within 0 and " + this.mItemCount);
        }
    }

    public int getItemCount() {
        return this.mItemCount;
    }

    void updateRange() {
        this.mViewCallback.getItemRangeInto(this.mTmpRange);
        if(this.mTmpRange[0] <= this.mTmpRange[1] && this.mTmpRange[0] >= 0) {
            if(this.mTmpRange[1] < this.mItemCount) {
                if(!this.mAllowScrollHints) {
                    this.mScrollHint = 0;
                } else if(this.mTmpRange[0] <= this.mPrevRange[1] && this.mPrevRange[0] <= this.mTmpRange[1]) {
                    if(this.mTmpRange[0] < this.mPrevRange[0]) {
                        this.mScrollHint = 1;
                    } else if(this.mTmpRange[0] > this.mPrevRange[0]) {
                        this.mScrollHint = 2;
                    }
                } else {
                    this.mScrollHint = 0;
                }

                this.mPrevRange[0] = this.mTmpRange[0];
                this.mPrevRange[1] = this.mTmpRange[1];
                this.mViewCallback.extendRangeInto(this.mTmpRange, this.mTmpRangeExtended, this.mScrollHint);
                this.mTmpRangeExtended[0] = Math.min(this.mTmpRange[0], Math.max(this.mTmpRangeExtended[0], 0));
                this.mTmpRangeExtended[1] = Math.max(this.mTmpRange[1], Math.min(this.mTmpRangeExtended[1], this.mItemCount - 1));
                this.mBackgroundProxy.updateRange(this.mTmpRange[0], this.mTmpRange[1], this.mTmpRangeExtended[0], this.mTmpRangeExtended[1], this.mScrollHint);
            }
        }
    }

    public abstract static class ViewCallback {
        public static final int HINT_SCROLL_NONE = 0;
        public static final int HINT_SCROLL_DESC = 1;
        public static final int HINT_SCROLL_ASC = 2;

        public ViewCallback() {
        }

        @UiThread
        public abstract void getItemRangeInto(int[] var1);

        @UiThread
        public void extendRangeInto(int[] range, int[] outRange, int scrollHint) {
            int fullRange = range[1] - range[0] + 1;
            int halfRange = fullRange / 2;
            outRange[0] = range[0] - (scrollHint == 1?fullRange:halfRange);
            outRange[1] = range[1] + (scrollHint == 2?fullRange:halfRange);
        }

        @UiThread
        public abstract void onDataRefresh();

        @UiThread
        public abstract void onItemLoaded(int var1);
    }

    public abstract static class DataCallback<T> {
        public DataCallback() {
        }

        @WorkerThread
        public abstract int refreshData();

        @WorkerThread
        public abstract void fillData(T[] var1, int var2, int var3);

        @WorkerThread
        public void recycleData(T[] data, int itemCount) {
        }

        @WorkerThread
        public int getMaxCachedTiles() {
            return 10;
        }
    }
}
