//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.util.ThreadUtil.BackgroundCallback;
import android.support.v7.util.ThreadUtil.MainThreadCallback;
import android.support.v7.util.TileList.Tile;
import android.util.Log;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

class MessageThreadUtil<T> implements ThreadUtil<T> {
    MessageThreadUtil() {
    }

    public MainThreadCallback<T> getMainThreadProxy(final MainThreadCallback<T> callback) {
        return new MainThreadCallback<T>() {
            final MessageThreadUtil.MessageQueue mQueue = new MessageThreadUtil.MessageQueue();
            private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
            static final int UPDATE_ITEM_COUNT = 1;
            static final int ADD_TILE = 2;
            static final int REMOVE_TILE = 3;
            private Runnable mMainThreadRunnable = new Runnable() {
                public void run() {
                    for(MessageThreadUtil.SyncQueueItem msg = mQueue.next(); msg != null; msg = mQueue.next()) {
                        switch(msg.what) {
                            case 1:
                                callback.updateItemCount(msg.arg1, msg.arg2);
                                break;
                            case 2:
                                callback.addTile(msg.arg1, (Tile)msg.data);
                                break;
                            case 3:
                                callback.removeTile(msg.arg1, msg.arg2);
                                break;
                            default:
                                Log.e("ThreadUtil", "Unsupported message, what=" + msg.what);
                        }
                    }

                }
            };

            public void updateItemCount(int generation, int itemCount) {
                this.sendMessage(MessageThreadUtil.SyncQueueItem.obtainMessage(1, generation, itemCount));
            }

            public void addTile(int generation, Tile<T> tile) {
                this.sendMessage(MessageThreadUtil.SyncQueueItem.obtainMessage(2, generation, tile));
            }

            public void removeTile(int generation, int position) {
                this.sendMessage(MessageThreadUtil.SyncQueueItem.obtainMessage(3, generation, position));
            }

            private void sendMessage(MessageThreadUtil.SyncQueueItem msg) {
                this.mQueue.sendMessage(msg);
                this.mMainThreadHandler.post(this.mMainThreadRunnable);
            }
        };
    }

    public BackgroundCallback<T> getBackgroundProxy(final BackgroundCallback<T> callback) {
        return new BackgroundCallback<T>() {
            final MessageThreadUtil.MessageQueue mQueue = new MessageThreadUtil.MessageQueue();
            private final Executor mExecutor;
            AtomicBoolean mBackgroundRunning;
            static final int REFRESH = 1;
            static final int UPDATE_RANGE = 2;
            static final int LOAD_TILE = 3;
            static final int RECYCLE_TILE = 4;
            private Runnable mBackgroundRunnable;

            {
                this.mExecutor = AsyncTask.THREAD_POOL_EXECUTOR;
                this.mBackgroundRunning = new AtomicBoolean(false);
                this.mBackgroundRunnable = new Runnable() {
                    public void run() {
                        while(true) {
                            MessageThreadUtil.SyncQueueItem msg = mQueue.next();
                            if(msg == null) {
                                mBackgroundRunning.set(false);
                                return;
                            }

                            switch(msg.what) {
                                case 1:
                                    mQueue.removeMessages(1);
                                    callback.refresh(msg.arg1);
                                    break;
                                case 2:
                                    mQueue.removeMessages(2);
                                    mQueue.removeMessages(3);
                                    callback.updateRange(msg.arg1, msg.arg2, msg.arg3, msg.arg4, msg.arg5);
                                    break;
                                case 3:
                                    callback.loadTile(msg.arg1, msg.arg2);
                                    break;
                                case 4:
                                    callback.recycleTile((Tile)msg.data);
                                    break;
                                default:
                                    Log.e("ThreadUtil", "Unsupported message, what=" + msg.what);
                            }
                        }
                    }
                };
            }

            public void refresh(int generation) {
                this.sendMessageAtFrontOfQueue(MessageThreadUtil.SyncQueueItem.obtainMessage(1, generation, (Object)null));
            }

            public void updateRange(int rangeStart, int rangeEnd, int extRangeStart, int extRangeEnd, int scrollHint) {
                this.sendMessageAtFrontOfQueue(MessageThreadUtil.SyncQueueItem.obtainMessage(2, rangeStart, rangeEnd, extRangeStart, extRangeEnd, scrollHint, (Object)null));
            }

            public void loadTile(int position, int scrollHint) {
                this.sendMessage(MessageThreadUtil.SyncQueueItem.obtainMessage(3, position, scrollHint));
            }

            public void recycleTile(Tile<T> tile) {
                this.sendMessage(MessageThreadUtil.SyncQueueItem.obtainMessage(4, 0, tile));
            }

            private void sendMessage(MessageThreadUtil.SyncQueueItem msg) {
                this.mQueue.sendMessage(msg);
                this.maybeExecuteBackgroundRunnable();
            }

            private void sendMessageAtFrontOfQueue(MessageThreadUtil.SyncQueueItem msg) {
                this.mQueue.sendMessageAtFrontOfQueue(msg);
                this.maybeExecuteBackgroundRunnable();
            }

            private void maybeExecuteBackgroundRunnable() {
                if(this.mBackgroundRunning.compareAndSet(false, true)) {
                    this.mExecutor.execute(this.mBackgroundRunnable);
                }

            }
        };
    }

    static class MessageQueue {
        private MessageThreadUtil.SyncQueueItem mRoot;

        MessageQueue() {
        }

        synchronized MessageThreadUtil.SyncQueueItem next() {
            if(this.mRoot == null) {
                return null;
            } else {
                MessageThreadUtil.SyncQueueItem next = this.mRoot;
                this.mRoot = this.mRoot.next;
                return next;
            }
        }

        synchronized void sendMessageAtFrontOfQueue(MessageThreadUtil.SyncQueueItem item) {
            item.next = this.mRoot;
            this.mRoot = item;
        }

        synchronized void sendMessage(MessageThreadUtil.SyncQueueItem item) {
            if(this.mRoot == null) {
                this.mRoot = item;
            } else {
                MessageThreadUtil.SyncQueueItem last;
                for(last = this.mRoot; last.next != null; last = last.next) {
                    ;
                }

                last.next = item;
            }
        }

        synchronized void removeMessages(int what) {
            MessageThreadUtil.SyncQueueItem prev;
            while(this.mRoot != null && this.mRoot.what == what) {
                prev = this.mRoot;
                this.mRoot = this.mRoot.next;
                prev.recycle();
            }

            if(this.mRoot != null) {
                prev = this.mRoot;

                MessageThreadUtil.SyncQueueItem next;
                for(MessageThreadUtil.SyncQueueItem item = prev.next; item != null; item = next) {
                    next = item.next;
                    if(item.what == what) {
                        prev.next = next;
                        item.recycle();
                    } else {
                        prev = item;
                    }
                }
            }

        }
    }

    static class SyncQueueItem {
        private static MessageThreadUtil.SyncQueueItem sPool;
        private static final Object sPoolLock = new Object();
        MessageThreadUtil.SyncQueueItem next;
        public int what;
        public int arg1;
        public int arg2;
        public int arg3;
        public int arg4;
        public int arg5;
        public Object data;

        SyncQueueItem() {
        }

        void recycle() {
            this.next = null;
            this.what = this.arg1 = this.arg2 = this.arg3 = this.arg4 = this.arg5 = 0;
            this.data = null;
            Object var1 = sPoolLock;
            synchronized(sPoolLock) {
                if(sPool != null) {
                    this.next = sPool;
                }

                sPool = this;
            }
        }

        static MessageThreadUtil.SyncQueueItem obtainMessage(int what, int arg1, int arg2, int arg3, int arg4, int arg5, Object data) {
            Object var7 = sPoolLock;
            synchronized(sPoolLock) {
                MessageThreadUtil.SyncQueueItem item;
                if(sPool == null) {
                    item = new MessageThreadUtil.SyncQueueItem();
                } else {
                    item = sPool;
                    sPool = sPool.next;
                    item.next = null;
                }

                item.what = what;
                item.arg1 = arg1;
                item.arg2 = arg2;
                item.arg3 = arg3;
                item.arg4 = arg4;
                item.arg5 = arg5;
                item.data = data;
                return item;
            }
        }

        static MessageThreadUtil.SyncQueueItem obtainMessage(int what, int arg1, int arg2) {
            return obtainMessage(what, arg1, arg2, 0, 0, 0, (Object)null);
        }

        static MessageThreadUtil.SyncQueueItem obtainMessage(int what, int arg1, Object data) {
            return obtainMessage(what, arg1, 0, 0, 0, 0, data);
        }
    }
}
