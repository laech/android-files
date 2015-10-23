package l.files.operations;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Progress {

    public static final Progress NONE = normalize(0, 0);

    Progress() {
    }

    public abstract long total();

    public abstract long processed();

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
        return new AutoValue_Progress(total, processed);
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
            return processed() / (float) total();
        }
    }

    public long getLeft() {
        return total() - processed();
    }

    public boolean isDone() {
        return total() == processed();
    }

}
