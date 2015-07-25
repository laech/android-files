package l.files.features.objects;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import l.files.common.base.Consumer;
import l.files.ui.StableAdapter;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.os.SystemClock.sleep;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public final class Instrumentations {

  private static final class InstrumentCallable<T> implements Callable<T> {
    private final Instrumentation mInstrumentation;
    private final Callable<T> mDelegate;

    private InstrumentCallable(
        Instrumentation instrumentation,
        Callable<T> delegate) {
      this.mInstrumentation = instrumentation;
      this.mDelegate = delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T call() throws Exception {
      final Object[] result = {null};
      final Throwable[] error = {null};
      Runnable code = new Runnable() {
        @Override
        public void run() {
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

      if (error[0] instanceof Error) {
        throw (Error) error[0];
      }

      if (error[0] instanceof RuntimeException) {
        throw (RuntimeException) error[0];
      }

      if (error[0] != null) {
        throw new RuntimeException(error[0]);
      }

      return (T) result[0];
    }
  }

  public static <T> T awaitOnMainThread(
      Instrumentation in,
      Callable<T> callable) {
    return await(in, new InstrumentCallable<>(in, callable), 30, SECONDS);
  }

  public static void awaitOnMainThread(
      Instrumentation in, final Runnable runnable) {
    awaitOnMainThread(in, new Callable<Boolean>() {
      @Override
      public Boolean call() {
        runnable.run();
        return true;
      }
    });
  }

  public static <T> T await(
      Instrumentation in,
      Callable<T> callable,
      long time,
      TimeUnit unit) {
    AssertionError error = null;
    long end = currentTimeMillis() + unit.toMillis(time);
    while (currentTimeMillis() < end) {
      try {
        return callable.call();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      } catch (AssertionError e) {
        if (error == null) {
          error = e;
        } else {
          error.addSuppressed(e);
        }
      }
      sleep(500);
    }

    if (error == null) {
      error = new AssertionError("Timed out.");
    }

//    takeScreenshotAndThrow(in, error);
//    return null;
    throw error;
  }

  private static void takeScreenshotAndThrow(
      Instrumentation in,
      AssertionError e) {
    File file = new File(getExternalStorageDirectory(),
        "test/failed-" + currentTimeMillis() + ".jpg");
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

  public static void scrollToTop(
      Instrumentation in, final RecyclerView recycler) {
    awaitOnMainThread(in, new Runnable() {
      @Override
      public void run() {
        @SuppressWarnings("unchecked")
        StableAdapter<Object, ViewHolder> adapter =
            (StableAdapter<Object, ViewHolder>) recycler.getAdapter();

        if (adapter.getItemCount() > 0) {
          recycler.scrollToPosition(0);
        }
      }
    });
  }

  public static void clickItemOnMainThread(
      Instrumentation in,
      RecyclerView recycler,
      Object itemId) {
    findItemOnMainThread(in, recycler, itemId, new Consumer<View>() {
      @Override
      public void apply(View input) {
        input.performClick();
      }
    });
  }

  public static void longClickItemOnMainThread(
      Instrumentation in,
      RecyclerView recycler,
      Object itemId) {
    findItemOnMainThread(in, recycler, itemId, new Consumer<View>() {
      @Override
      public void apply(View input) {
        input.performLongClick();
      }
    });
  }

  public static void findItemOnMainThread(
      final Instrumentation in,
      final RecyclerView recycler,
      final Object itemId,
      final Consumer<View> consumer) {
    scrollToTop(in, recycler);
    awaitOnMainThread(in, new Runnable() {
      @Override
      public void run() {
        find(recycler, itemId, consumer);
      }
    });
  }

  private static void find(
      RecyclerView recycler,
      Object itemId,
      Consumer<View> consumer) {
    @SuppressWarnings("unchecked")
    StableAdapter<Object, ViewHolder> adapter =
        (StableAdapter<Object, ViewHolder>) recycler.getAdapter();

    for (int i = 0; i < adapter.getItemCount(); i++) {
      recycler.scrollBy(0, 5);
      for (int j = 0; j < recycler.getChildCount(); j++) {
        View child = recycler.getChildAt(j);
        ViewHolder holder = recycler.getChildViewHolder(child);
        int position = holder.getAdapterPosition();
        if (position == NO_POSITION) {
          fail();
        }
        if (position == i) {
          Object thatId = adapter.getItemIdObject(position);
          if (Objects.equals(itemId, thatId)) {
            consumer.apply(child);
            return;
          }
        }
      }
    }

    fail("Item not found: " + itemId);
  }
}
