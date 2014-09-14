package l.files.operations.testing;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;

import l.files.operations.Target;

import static com.google.common.base.Objects.equal;
import static com.google.common.truth.Truth.ASSERT;

public final class TargetSubject extends Subject<TargetSubject, Target> {

  public TargetSubject(FailureStrategy failureStrategy, Target subject) {
    super(failureStrategy, subject);
  }

  public static SubjectFactory<TargetSubject, Target> target() {
    return new SubjectFactory<TargetSubject, Target>() {
      @Override
      public TargetSubject getSubject(FailureStrategy fs, Target that) {
        return new TargetSubject(fs, that);
      }
    };
  }

  public static TargetSubject assertThat(Target target) {
    return ASSERT.about(target()).that(target);
  }

  public TargetSubject source(String source) {
    if (!equal(getSubject().source(), source)) {
      fail("has source", source);
    }
    return this;
  }

  public TargetSubject destination(String destination) {
    if (!equal(getSubject().destination(), destination)) {
      fail("has destination", destination);
    }
    return this;
  }
}
