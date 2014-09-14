package l.files.operations.testing;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;

import l.files.operations.Progress;

import static com.google.common.truth.Truth.ASSERT;

public final class ProgressSubject extends Subject<ProgressSubject, Progress> {

  public ProgressSubject(FailureStrategy failureStrategy, Progress subject) {
    super(failureStrategy, subject);
  }

  public static SubjectFactory<ProgressSubject, Progress> progress() {
    return new SubjectFactory<ProgressSubject, Progress>() {
      @Override
      public ProgressSubject getSubject(FailureStrategy fs, Progress that) {
        return new ProgressSubject(fs, that);
      }
    };
  }

  public static ProgressSubject assertThat(Progress target) {
    return ASSERT.about(progress()).that(target);
  }

  public ProgressSubject total(long total) {
    if (getSubject().total() != total) {
      fail("total is", total);
    }
    return this;
  }

  public ProgressSubject processed(long processed) {
    if (getSubject().processed() != processed) {
      fail("processed", processed);
    }
    return this;
  }

  public ProgressSubject processedPercentage(float percentage) {
    if (getSubject().processedPercentage() != percentage) {
      fail("processed percentage", percentage);
    }
    return this;
  }

  public ProgressSubject left(int left) {
    if (getSubject().left() != left) {
      fail("left", left);
    }
    return this;
  }

  public ProgressSubject isDone(boolean done) {
    if (getSubject().isDone() != done) {
      fail("is done", done);
    }
    return this;
  }
}
