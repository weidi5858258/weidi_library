//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.util;

import android.support.v7.util.TileList.Tile;

interface ThreadUtil<T> {
    ThreadUtil.MainThreadCallback<T> getMainThreadProxy(ThreadUtil.MainThreadCallback<T> var1);

    ThreadUtil.BackgroundCallback<T> getBackgroundProxy(ThreadUtil.BackgroundCallback<T> var1);

    public interface BackgroundCallback<T> {
        void refresh(int var1);

        void updateRange(int var1, int var2, int var3, int var4, int var5);

        void loadTile(int var1, int var2);

        void recycleTile(Tile<T> var1);
    }

    public interface MainThreadCallback<T> {
        void updateItemCount(int var1, int var2);

        void addTile(int var1, Tile<T> var2);

        void removeTile(int var1, int var2);
    }
}
