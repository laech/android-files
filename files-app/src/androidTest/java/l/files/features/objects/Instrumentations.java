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
import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;

public final class Instrumentations
{

    private static final class InstrumentCallable<T> implements Callable<T>
    {
        private final Instrumentation mInstrumentation;
        private final Callable<T> mDelegate;

        private InstrumentCallable(
                final Instrumentation instrumentation,
                final Callable<T> delegate)
        {
            this.mInstrumentation = instrumentation;
            this.mDelegate = delegate;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final T call() throws Exception
        {
            final Object[] result = {null};
            final Throwable[] error = {null};
            final Runnable code = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        result[0] = mDelegate.call();
                    }
                    catch (Exception | AssertionError e)
                    {
                        error[0] = e;
                    }
                }
            };

            if (getMainLooper() == myLooper())
            {
                code.run();
            }
            else
            {
                mInstrumentation.runOnMainSync(code);
            }

            if (error[0] instanceof Error)
            {
                throw (Error) error[0];
            }

            if (error[0] instanceof RuntimeException)
            {
                throw (RuntimeException) error[0];
            }

            if (error[0] != null)
            {
                throw new RuntimeException(error[0]);
            }

            return (T) result[0];
        }
    }

    public static <T> T awaitOnMainThread(
            final Instrumentation in,
            final Callable<T> callable)
    {
        return await(in, new InstrumentCallable<>(in, callable), 5, SECONDS);
    }

    public static void awaitOnMainThread(
            final Instrumentation in,
            final Runnable runnable)
    {
        awaitOnMainThread(in, new Callable<Boolean>()
        {
            @Override
            public Boolean call()
            {
                runnable.run();
                return true;
            }
        });
    }

    public static <T> T await(
            final Instrumentation in,
            final Callable<T> callable,
            final long time,
            final TimeUnit unit)
    {
        AssertionError error = null;
        final long end = currentTimeMillis() + unit.toMillis(time);
        while (currentTimeMillis() < end)
        {
            try
            {
                return callable.call();
            }
            catch (final RuntimeException e)
            {
                throw e;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
            catch (final AssertionError e)
            {
                error = e;
            }
            sleep(5);
        }

        if (error == null)
        {
            error = new AssertionError("Timed out.");
        }

        takeScreenshotAndThrow(in, error);
        return null;
    }

    private static void takeScreenshotAndThrow(
            final Instrumentation in,
            final AssertionError e)
    {
        final File file = new File(getExternalStorageDirectory(),
                "test/failed-" + currentTimeMillis() + ".jpg");
        final File parent = file.getParentFile();
        assertTrue(parent.isDirectory() || parent.mkdir());
        final Bitmap screenshot = in.getUiAutomation().takeScreenshot();
        try (OutputStream out = new FileOutputStream(file))
        {
            screenshot.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }
        catch (final IOException io)
        {
            final AssertionError error = new AssertionError(
                    "Failed to take screenshot on assertion failure. " +
                            "Original assertion error is included below.", e);
            error.addSuppressed(io);
            throw error;
        }
        finally
        {
            screenshot.recycle();
        }
        throw new AssertionError(nullToEmpty(e.getMessage()) +
                "\nAssertion failed, screenshot saved " + file, e);
    }

    public static void await(final Instrumentation in, final Runnable runnable)
    {
        await(in, Executors.callable(runnable), 1, SECONDS);
    }

}
