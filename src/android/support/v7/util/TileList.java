//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.util;

import android.util.SparseArray;
import java.lang.reflect.Array;

class TileList<T> {
    final int mTileSize;
    private final SparseArray<TileList.Tile<T>> mTiles = new SparseArray(10);
    TileList.Tile<T> mLastAccessedTile;

    public TileList(int tileSize) {
        this.mTileSize = tileSize;
    }

    public T getItemAt(int pos) {
        if(this.mLastAccessedTile == null || !this.mLastAccessedTile.containsPosition(pos)) {
            int startPosition = pos - pos % this.mTileSize;
            int index = this.mTiles.indexOfKey(startPosition);
            if(index < 0) {
                return null;
            }

            this.mLastAccessedTile = (TileList.Tile)this.mTiles.valueAt(index);
        }

        return this.mLastAccessedTile.getByPosition(pos);
    }

    public int size() {
        return this.mTiles.size();
    }

    public void clear() {
        this.mTiles.clear();
    }

    public TileList.Tile<T> getAtIndex(int index) {
        return (TileList.Tile)this.mTiles.valueAt(index);
    }

    public TileList.Tile<T> addOrReplace(TileList.Tile<T> newTile) {
        int index = this.mTiles.indexOfKey(newTile.mStartPosition);
        if(index < 0) {
            this.mTiles.put(newTile.mStartPosition, newTile);
            return null;
        } else {
            TileList.Tile<T> oldTile = (TileList.Tile)this.mTiles.valueAt(index);
            this.mTiles.setValueAt(index, newTile);
            if(this.mLastAccessedTile == oldTile) {
                this.mLastAccessedTile = newTile;
            }

            return oldTile;
        }
    }

    public TileList.Tile<T> removeAtPos(int startPosition) {
        TileList.Tile<T> tile = (TileList.Tile)this.mTiles.get(startPosition);
        if(this.mLastAccessedTile == tile) {
            this.mLastAccessedTile = null;
        }

        this.mTiles.delete(startPosition);
        return tile;
    }

    public static class Tile<T> {
        public final T[] mItems;
        public int mStartPosition;
        public int mItemCount;
        TileList.Tile<T> mNext;

        public Tile(Class<T> klass, int size) {
            this.mItems = (T[])((Object[])Array.newInstance(klass, size));
        }

        boolean containsPosition(int pos) {
            return this.mStartPosition <= pos && pos < this.mStartPosition + this.mItemCount;
        }

        T getByPosition(int pos) {
            return this.mItems[pos - this.mStartPosition];
        }
    }
}
