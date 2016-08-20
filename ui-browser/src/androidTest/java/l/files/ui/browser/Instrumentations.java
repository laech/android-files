package l.files.ui.browser;

import android.app.Instrumentation;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import l.files.base.Consumer;
import l.files.base.Provider;
import l.files.ui.base.widget.StableAdapter;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.os.SystemClock.sleep;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

final class Instrumentations {

    private static final class InstrumentCallable<T> implements Callable<T> {

        private final Instrumentation instrumentation;
        private final Callable<T> delegate;

        private InstrumentCallable(
                Instrumentation instrumentation, Callable<T> delegate) {
            this.instrumentation = instrumentation;
            this.delegate = delegate;
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
                        result[0] = delegate.call();
                    } catch (Exception | AssertionError e) {
                        error[0] = e;
                    }
                }
            };

            if (instrumentation != null && getMainLooper() != myLooper()) {
                instrumentation.runOnMainSync(code);
            } else {
                code.run();
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

    static <T> T await(Callable<T> callable) {
        return await(callable, 1, MINUTES);
    }

    static <T> T awaitOnMainThread(
            Instrumentation in, Callable<T> callable) {
        return await(new InstrumentCallable<>(in, callable), 1, MINUTES);
    }

    static void awaitOnMainThread(Instrumentation in, final Runnable runnable) {
        awaitOnMainThread(in, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                runnable.run();
                return true;
            }
        });
    }

    static <T> T await(Callable<T> callable, long time, TimeUnit unit) {
        if (!(callable instanceof InstrumentCallable<?>)) {
            callable = new InstrumentCallable<>(null, callable);
        }
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
                error = e;
            }
            sleep(10);
        }

        if (error == null) {
            throw new AssertionError("Timed out.");
        }

        AssertionError e = new AssertionError(error.getMessage());
        e.initCause(error);
        throw e;
    }

    static void scrollToTop(
            final Instrumentation in,
            final Provider<RecyclerView> recycler) {

        awaitOnMainThread(in, new Runnable() {
            @Override
            public void run() {
                if (getStableAdapter(recycler).getItemCount() > 0) {
                    recycler.get().scrollToPosition(0);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static StableAdapter<Object, ViewHolder> getStableAdapter(
            Provider<RecyclerView> recycler) {
        return (StableAdapter<Object, ViewHolder>) recycler.get().getAdapter();
    }

    static void clickItemOnMainThread(
            Instrumentation in,
            Provider<RecyclerView> recycler,
            Object itemId) {
        findItemOnMainThread(in, recycler, itemId, new Consumer<View>() {
            @Override
            public void accept(View input) {
                input.performClick();
            }
        });
    }

    static void longClickItemOnMainThread(
            Instrumentation in,
            Provider<RecyclerView> recycler,
            Object itemId) {
        findItemOnMainThread(in, recycler, itemId, new Consumer<View>() {
            @Override
            public void accept(View input) {
                assertTrue(input.isEnabled());
                input.performLongClick();
            }
        });
    }

    static void findItemOnMainThread(
            final Instrumentation in,
            final Provider<RecyclerView> recycler,
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
            Provider<RecyclerView> recycler,
            Object itemId,
            Consumer<View> consumer) {

        RecyclerView view = recycler.get();
        StableAdapter<Object, ViewHolder> adapter = getStableAdapter(recycler);

        for (int i = 0; i < adapter.getItemCount(); i++) {
            view.scrollBy(0, 5);
            for (int j = 0; j < view.getChildCount(); j++) {
                View child = view.getChildAt(j);
                ViewHolder holder = view.getChildViewHolder(child);
                int position = holder.getAdapterPosition();
                if (position == NO_POSITION) {
                    fail();
                }
                if (position == i) {
                    Object thatId = adapter.getItemIdObject(position);
                    if (itemId.equals(thatId)) {
                        consumer.accept(child);
                        return;
                    }
                }
            }
        }

        fail("Item not found: " + itemId);
    }

}
