package l.files.features.objects;

import android.app.Instrumentation;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.os.SystemClock.sleep;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;

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
      Runnable code = new Runnable() {
        @Override public void run() {
          try {
            result[0] = mDelegate.call();
          } catch (Exception | AssertionError e) {
            error[0] = e;
          }
        }
      };
      if (getMainLooper() == myLooper()) {
        code.run();
      } else {
        mInstrumentation.runOnMainSync(code);
      }
      if (error[0] instanceof AssertionError) {
        throw (AssertionError) error[0];
      }
      if (error[0] != null) {
        throw new AssertionError(error[0]);
      }
      return (T) result[0];
    }
  }

  public static <T> T awaitOnMainThread(Instrumentation in, Callable<T> callable) {
    return await(in, new InstrumentCallable<>(in, callable), 5, SECONDS);
  }

  public static void awaitOnMainThread(Instrumentation in, final Runnable runnable) {
    awaitOnMainThread(in, new Callable<Boolean>() {
      @Override public Boolean call() {
        runnable.run();
        return true;
      }
    });
  }

  public static <T> T await(
      Instrumentation in, Callable<T> callable, long time, TimeUnit unit) {
    long start = currentTimeMillis();
    long duration = unit.toMillis(time);
    AssertionError error = null;
    while ((start + duration) > currentTimeMillis()) {
      try {
        return callable.call();
      } catch (RuntimeException e) {
        throw e;
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
    takeScreenshotAndThrow(in, error);
    return null;
  }

  private static void takeScreenshotAndThrow(
      Instrumentation in, AssertionError e) {
    File file = new File(getExternalStorageDirectory(),
        "test/failed-" + System.currentTimeMillis() + ".jpg");
    File parent = file.getParentFile();
    assertTrue(parent.isDirectory() || parent.mkdir());
    Bitmap screenshot = in.getUiAutomation().takeScreenshot();
    try (OutputStream out = new FileOutputStream(file)) {
      screenshot.compress(Bitmap.CompressFormat.JPEG, 90, out);
    } catch (IOException io) {
      AssertionError error = new AssertionError(
          "Failed to take screenshot on assertion failure. " +
              "Original assertion error is included below.", e);
      error.addSuppressed(io);
      throw error;
    } finally {
      screenshot.recycle();
    }
    throw new AssertionError(e.getMessage() +
        "\nAssertion failed, screenshot saved " + file, e);
  }

  public static void await(Instrumentation in, Runnable runnable) {
    await(in, Executors.callable(runnable), 1, SECONDS);
  }

}
