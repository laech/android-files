package l.files.operations;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class Progress {

    public static final Progress NONE = normalize(0, 0);

    Progress() {
    }

    public abstract long getTotal();

    public abstract long getProcessed();

    /**
     * Throws IllegalArgumentException if total < processed or negative
     */
    public static Progress create(long total, long processed) {
        if (total < 0) {
            throw new IllegalArgumentException("total=" + total);
        }
        if (processed < 0) {
            throw new IllegalArgumentException("processed=" + processed);
        }
        if (total < processed) {
            throw new IllegalArgumentException("total=" + total
                    + ", processed=" + processed);
        }
        return new AutoParcel_Progress(total, processed);
    }

    /**
     * If total is less than processed, set total and processed to have the
     * value of processed.
     */
    public static Progress normalize(long total, long processed) {
        if (total < processed) {
            return create(processed, processed);
        } else {
            return create(total, processed);
        }
    }

    public float getProcessedPercentage() {
        if (NONE == this) {
            return 1F;
        } else {
            return getProcessed() / (float) getTotal();
        }
    }

    public long getLeft() {
        return getTotal() - getProcessed();
    }

    public boolean isDone() {
        return getTotal() == getProcessed();
    }

}
