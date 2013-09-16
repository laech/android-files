package l.files.features.object;

import static android.os.SystemClock.sleep;
import static java.lang.Boolean.FALSE;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.app.Instrumentation;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public final class Instrumentations {

  private static final class InstrumentCallable<T> implements Callable<T> {

    private final Instrumentation in;
    private final Callable<T> delegate;

    private InstrumentCallable(Instrumentation in, Callable<T> delegate) {
      this.in = in;
      this.delegate = delegate;
    }

    @SuppressWarnings("unchecked") @Override public final T call() throws Exception {
      final Object[] result = {null};
      final Throwable[] error = {null};
      in.runOnMainSync(new Runnable() {
        @Override public void run() {
          try {
            result[0] = delegate.call();
          } catch (Exception e) {
            error[0] = e;
          }
        }
      });
      if (error[0] != null) {
        throw new AssertionError(error[0]);
      }
      return (T) result[0];
    }
  }

  public static <T> T awaitOnMainThread(Instrumentation in, final Callable<T> callable) {
    return await(new InstrumentCallable<T>(in, callable), 5, SECONDS);
  }

  public static void awaitOnMainThread(Instrumentation in, final Runnable runnable) {
    awaitOnMainThread(in, new Callable<Boolean>() {
      @Override public Boolean call() {
        runnable.run();
        return true;
      }
    });
  }

  public static <T> T await(Callable<T> callable, long time, TimeUnit unit) {
    long start = currentTimeMillis();
    long duration = unit.toMillis(time);
    while ((start + duration) > currentTimeMillis()) {
      try {
        T result = callable.call();
        if (result != null && !result.equals(FALSE)) {
          return result;
        }
      } catch (Exception e) {
        throw new AssertionError(e);
      }
      sleep(5);
    }
    throw new AssertionError("timed out");
  }

  private Instrumentations() {}
}
