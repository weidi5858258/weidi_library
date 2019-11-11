//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.recyclerview.extensions;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v7.util.DiffUtil.ItemCallback;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class AsyncDifferConfig<T> {
    @NonNull
    private final Executor mMainThreadExecutor;
    @NonNull
    private final Executor mBackgroundThreadExecutor;
    @NonNull
    private final ItemCallback<T> mDiffCallback;

    AsyncDifferConfig(@NonNull Executor mainThreadExecutor, @NonNull Executor backgroundThreadExecutor, @NonNull ItemCallback<T> diffCallback) {
        this.mMainThreadExecutor = mainThreadExecutor;
        this.mBackgroundThreadExecutor = backgroundThreadExecutor;
        this.mDiffCallback = diffCallback;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @NonNull
    public Executor getMainThreadExecutor() {
        return this.mMainThreadExecutor;
    }

    @NonNull
    public Executor getBackgroundThreadExecutor() {
        return this.mBackgroundThreadExecutor;
    }

    @NonNull
    public ItemCallback<T> getDiffCallback() {
        return this.mDiffCallback;
    }

    public static final class Builder<T> {
        private Executor mMainThreadExecutor;
        private Executor mBackgroundThreadExecutor;
        private final ItemCallback<T> mDiffCallback;
        private static final Object sExecutorLock = new Object();
        private static Executor sDiffExecutor = null;

        public Builder(@NonNull ItemCallback<T> diffCallback) {
            this.mDiffCallback = diffCallback;
        }

        @RestrictTo({Scope.LIBRARY_GROUP})
        @NonNull
        public AsyncDifferConfig.Builder<T> setMainThreadExecutor(Executor executor) {
            this.mMainThreadExecutor = executor;
            return this;
        }

        @NonNull
        public AsyncDifferConfig.Builder<T> setBackgroundThreadExecutor(Executor executor) {
            this.mBackgroundThreadExecutor = executor;
            return this;
        }

        @NonNull
        public AsyncDifferConfig<T> build() {
            if(this.mBackgroundThreadExecutor == null) {
                Object var1 = sExecutorLock;
                synchronized(sExecutorLock) {
                    if(sDiffExecutor == null) {
                        sDiffExecutor = Executors.newFixedThreadPool(2);
                    }
                }

                this.mBackgroundThreadExecutor = sDiffExecutor;
            }

            return new AsyncDifferConfig(this.mMainThreadExecutor, this.mBackgroundThreadExecutor, this.mDiffCallback);
        }
    }
}
