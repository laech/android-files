package l.files.operations;

import android.support.annotation.Nullable;

public final class Progress {

    public static final Progress NONE = normalize(0, 0);

    private final long total;
    private final long processed;

    private Progress(long total, long processed) {
        this.total = total;
        this.processed = processed;
    }

    public long total() {
        return total;
    }

    public long processed() {
        return processed;
    }

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
        return new Progress(total, processed);
    }

    /**
     * If total is less than processed, set total and processed to have the
     * value of processed.
     */
    static Progress normalize(long total, long processed) {
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
            return processed() / (float) total();
        }
    }

    public long getLeft() {
        return total() - processed();
    }

    public boolean isDone() {
        return total() == processed();
    }

    @Override
    public String toString() {
        return "Progress{" +
                "total=" + total +
                ", processed=" + processed +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Progress progress = (Progress) o;

        return total == progress.total &&
                processed == progress.processed;

    }

    @Override
    public int hashCode() {
        int result = (int) (total ^ (total >>> 32));
        result = 31 * result + (int) (processed ^ (processed >>> 32));
        return result;
    }
}
