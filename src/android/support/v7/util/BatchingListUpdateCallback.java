//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.v7.util;

public class BatchingListUpdateCallback implements ListUpdateCallback {
    private static final int TYPE_NONE = 0;
    private static final int TYPE_ADD = 1;
    private static final int TYPE_REMOVE = 2;
    private static final int TYPE_CHANGE = 3;
    final ListUpdateCallback mWrapped;
    int mLastEventType = 0;
    int mLastEventPosition = -1;
    int mLastEventCount = -1;
    Object mLastEventPayload = null;

    public BatchingListUpdateCallback(ListUpdateCallback callback) {
        this.mWrapped = callback;
    }

    public void dispatchLastEvent() {
        if(this.mLastEventType != 0) {
            switch(this.mLastEventType) {
                case 1:
                    this.mWrapped.onInserted(this.mLastEventPosition, this.mLastEventCount);
                    break;
                case 2:
                    this.mWrapped.onRemoved(this.mLastEventPosition, this.mLastEventCount);
                    break;
                case 3:
                    this.mWrapped.onChanged(this.mLastEventPosition, this.mLastEventCount, this.mLastEventPayload);
            }

            this.mLastEventPayload = null;
            this.mLastEventType = 0;
        }
    }

    public void onInserted(int position, int count) {
        if(this.mLastEventType == 1 && position >= this.mLastEventPosition && position <= this.mLastEventPosition + this.mLastEventCount) {
            this.mLastEventCount += count;
            this.mLastEventPosition = Math.min(position, this.mLastEventPosition);
        } else {
            this.dispatchLastEvent();
            this.mLastEventPosition = position;
            this.mLastEventCount = count;
            this.mLastEventType = 1;
        }
    }

    public void onRemoved(int position, int count) {
        if(this.mLastEventType == 2 && this.mLastEventPosition >= position && this.mLastEventPosition <= position + count) {
            this.mLastEventCount += count;
            this.mLastEventPosition = position;
        } else {
            this.dispatchLastEvent();
            this.mLastEventPosition = position;
            this.mLastEventCount = count;
            this.mLastEventType = 2;
        }
    }

    public void onMoved(int fromPosition, int toPosition) {
        this.dispatchLastEvent();
        this.mWrapped.onMoved(fromPosition, toPosition);
    }

    public void onChanged(int position, int count, Object payload) {
        if(this.mLastEventType == 3 && position <= this.mLastEventPosition + this.mLastEventCount && position + count >= this.mLastEventPosition && this.mLastEventPayload == payload) {
            int previousEnd = this.mLastEventPosition + this.mLastEventCount;
            this.mLastEventPosition = Math.min(position, this.mLastEventPosition);
            this.mLastEventCount = Math.max(previousEnd, position + count) - this.mLastEventPosition;
        } else {
            this.dispatchLastEvent();
            this.mLastEventPosition = position;
            this.mLastEventCount = count;
            this.mLastEventPayload = payload;
            this.mLastEventType = 3;
        }
    }
}
