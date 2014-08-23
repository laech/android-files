package l.files.features.object;

import android.app.Instrumentation;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static android.os.SystemClock.sleep;
import static java.lang.Boolean.FALSE;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class Instrumentations {

  private static final class InstrumentCallable<T> implements Callable<T> {
    private final Instrumentation mInstrumentation;
    private final Callable<T> mDelegate;

    private InstrumentCallable(Instrumentation instrumentation, Callable<T> delegate) {
      this.mInstrumentation = instrumentation;
      this.mDelegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override public final T call() throws Exception {
      final Object[] result = {null};
      final Throwable[] error = {null};
      mInstrumentation.runOnMainSync(new Runnable() {
        @Override public void run() {
          try {
            result[0] = mDelegate.call();
          } catch (Exception | AssertionError e) {
            error[0] = e;
          }
        }
      });
      if (error[0] instanceof AssertionError) {
        throw (AssertionError) error[0];
      }
      if (error[0] != null) {
        throw new AssertionError(error[0]);
      }
      return (T) result[0];
    }
  }

  public static <T> T awaitOnMainThread(Instrumentation in, final Callable<T> callable) {
    return await(new InstrumentCallable<>(in, callable), 5, SECONDS);
  }

  public static void awaitOnMainThread(Instrumentation in, final Runnable runnable) {
    awaitOnMainThread(in, new Callable<Boolean>() {
      @Override public Boolean call() {
        runnable.run();
        return true;
      }
    });
  }

  /**
   * Awaits the for {@code callable} to return a successful value
   * (a successful value is anything that is not null or false),
   * or until the specified duration times out - in this case an error will be
   * thrown.
   *
   * @deprecated use {@link #await(Runnable, long, TimeUnit)} instead
   */
  @Deprecated
  public static <T> T await(Callable<T> callable, long time, TimeUnit unit) {
    long start = currentTimeMillis();
    long duration = unit.toMillis(time);
    AssertionError error = null;
    while ((start + duration) > currentTimeMillis()) {
      try {
        T result = callable.call();
        if (result != null && !result.equals(FALSE)) {
          return result;
        }
      } catch (Exception e) {
        throw new AssertionError(e);
      } catch (AssertionError e) {
        error = e;
      }
      sleep(5);
    }
    if (error == null) {
      error = new AssertionError("Timed out.");
    }
    throw error;
  }

  /**
   * Awaits for the runnable to return without an error, or until the
   * specified duration times out - in this case an error will be thrown.
   */
  public static void await(Runnable runnable, long time, TimeUnit unit) {
    final long start = currentTimeMillis();
    final long duration = unit.toMillis(time);
    AssertionError error = null;
    while ((start + duration) > currentTimeMillis()) {
      try {
        runnable.run();
        return;
      } catch (AssertionError e) {
        error = e;
        sleep(5);
      }
    }
    if (error == null) {
      error = new AssertionError("Timed out.");
    }
    throw error;
  }

  public static void await(Runnable runnable) {
    await(runnable, 1, SECONDS);
  }

  private Instrumentations() {}
}
