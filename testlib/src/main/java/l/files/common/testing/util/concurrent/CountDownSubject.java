package l.files.common.testing.util.concurrent;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class CountDownSubject
    extends Subject<CountDownSubject, CountDownLatch> {

  public CountDownSubject(FailureStrategy fs, CountDownLatch subject) {
    super(fs, subject);
  }

  public static SubjectFactory<CountDownSubject, CountDownLatch> countDown() {
    return new SubjectFactory<CountDownSubject, CountDownLatch>() {
      @Override public CountDownSubject getSubject(
          FailureStrategy fs, CountDownLatch that) {
        return new CountDownSubject(fs, that);
      }
    };
  }

  public CountDownSubject await(long timeout, TimeUnit unit)
      throws InterruptedException {
    if (!getSubject().await(timeout, unit)) {
      fail("await " + timeout + " " + unit);
    }
    return this;
  }

}
