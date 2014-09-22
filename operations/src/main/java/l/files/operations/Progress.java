package l.files.operations;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Progress {

  private static final Progress NONE = normalize(0, 0);

  Progress() {}

  public abstract long total();
  public abstract long processed();

  /**
   * Returns an instance with total and processed set to 0.
   */
  public static Progress none() {
    return NONE;
  }

  /**
   * @throws IllegalArgumentException if total < processed or negative
   */
  public static Progress create(long total, long processed) {
    if (total < 0) {
      throw new IllegalArgumentException("total=" + total);
    }
    if (processed < 0) {
      throw new IllegalArgumentException("processed=" + processed);
    }
    if (total < processed) {
      throw new IllegalArgumentException(
          "total=" + total + ", processed=" + processed);
    }
    return new AutoValue_Progress(total, processed);
  }

  /**
   * If total is less than processed, set {@link #total()} and
   * {@link #processed()} to have the value of processed.
   */
  public static Progress normalize(long total, long processed) {
    if (total < processed) {
      total = processed;
    }
    return new AutoValue_Progress(total, processed);
  }

  /**
   * Returns a value between 0 and 1.
   */
  public float processedPercentage() {
    if (none().equals(this)) {
      return 1;
    }
    return processed() / (float) total();
  }

  /**
   * Number of work left.
   */
  public long left() {
    return total() - processed();
  }

  /**
   * Returns true if total is processed.
   */
  public boolean isDone() {
    return total() == processed();
  }
}
