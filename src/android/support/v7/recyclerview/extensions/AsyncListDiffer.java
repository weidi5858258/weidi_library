//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.recyclerview.extensions;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig.Builder;
import android.support.v7.util.AdapterListUpdateCallback;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.util.DiffUtil.Callback;
import android.support.v7.util.DiffUtil.DiffResult;
import android.support.v7.util.DiffUtil.ItemCallback;
import android.support.v7.widget.RecyclerView.Adapter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class AsyncListDiffer<T> {
    private final ListUpdateCallback mUpdateCallback;
    final AsyncDifferConfig<T> mConfig;
    final Executor mMainThreadExecutor;
    private static final Executor sMainThreadExecutor = new AsyncListDiffer.MainThreadExecutor();
    @Nullable
    private List<T> mList;
    @NonNull
    private List<T> mReadOnlyList;
    int mMaxScheduledGeneration;

    public AsyncListDiffer(@NonNull Adapter adapter, @NonNull ItemCallback<T> diffCallback) {
        this((ListUpdateCallback)(new AdapterListUpdateCallback(adapter)), (AsyncDifferConfig)(new Builder(diffCallback)).build());
    }

    public AsyncListDiffer(@NonNull ListUpdateCallback listUpdateCallback, @NonNull AsyncDifferConfig<T> config) {
        this.mReadOnlyList = Collections.emptyList();
        this.mUpdateCallback = listUpdateCallback;
        this.mConfig = config;
        if(config.getMainThreadExecutor() != null) {
            this.mMainThreadExecutor = config.getMainThreadExecutor();
        } else {
            this.mMainThreadExecutor = sMainThreadExecutor;
        }

    }

    @NonNull
    public List<T> getCurrentList() {
        return this.mReadOnlyList;
    }

    public void submitList(@Nullable final List<T> newList) {
        final int runGeneration = ++this.mMaxScheduledGeneration;
        if(newList != this.mList) {
            if(newList == null) {
                int countRemoved = this.mList.size();
                this.mList = null;
                this.mReadOnlyList = Collections.emptyList();
                this.mUpdateCallback.onRemoved(0, countRemoved);
            } else if(this.mList == null) {
                this.mList = newList;
                this.mReadOnlyList = Collections.unmodifiableList(newList);
                this.mUpdateCallback.onInserted(0, newList.size());
            } else {
                final List<T> oldList = this.mList;
                this.mConfig.getBackgroundThreadExecutor().execute(new Runnable() {
                    public void run() {
                        final DiffResult result = DiffUtil.calculateDiff(new Callback() {
                            public int getOldListSize() {
                                return oldList.size();
                            }

                            public int getNewListSize() {
                                return newList.size();
                            }

                            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                                T oldItem = oldList.get(oldItemPosition);
                                T newItem = newList.get(newItemPosition);
                                return oldItem != null && newItem != null?AsyncListDiffer.this.mConfig.getDiffCallback().areItemsTheSame(oldItem, newItem):oldItem == null && newItem == null;
                            }

                            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                                T oldItem = oldList.get(oldItemPosition);
                                T newItem = newList.get(newItemPosition);
                                if(oldItem != null && newItem != null) {
                                    return AsyncListDiffer.this.mConfig.getDiffCallback().areContentsTheSame(oldItem, newItem);
                                } else if(oldItem == null && newItem == null) {
                                    return true;
                                } else {
                                    throw new AssertionError();
                                }
                            }

                            @Nullable
                            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                                T oldItem = oldList.get(oldItemPosition);
                                T newItem = newList.get(newItemPosition);
                                if(oldItem != null && newItem != null) {
                                    return AsyncListDiffer.this.mConfig.getDiffCallback().getChangePayload(oldItem, newItem);
                                } else {
                                    throw new AssertionError();
                                }
                            }
                        });
                        AsyncListDiffer.this.mMainThreadExecutor.execute(new Runnable() {
                            public void run() {
                                if(AsyncListDiffer.this.mMaxScheduledGeneration == runGeneration) {
                                    AsyncListDiffer.this.latchList(newList, result);
                                }

                            }
                        });
                    }
                });
            }
        }
    }

    void latchList(@NonNull List<T> newList, @NonNull DiffResult diffResult) {
        this.mList = newList;
        this.mReadOnlyList = Collections.unmodifiableList(newList);
        diffResult.dispatchUpdatesTo(this.mUpdateCallback);
    }

    private static class MainThreadExecutor implements Executor {
        final Handler mHandler = new Handler(Looper.getMainLooper());

        MainThreadExecutor() {
        }

        public void execute(@NonNull Runnable command) {
            this.mHandler.post(command);
        }
    }
}
