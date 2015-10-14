package l.files.testing;

import java.util.concurrent.TimeUnit;

import l.files.common.base.Executable;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;

public final class Tests {
    private Tests() {
    }

    /**
     * Retries the given assertion by catching any {@link AssertionError}. If
     * the assertion does not succeed within the given timeout, the {@link
     * AssertionError} from the assertion will be thrown.
     */
    public static void timeout(
            final long time,
            final TimeUnit unit,
            final Executable assertion) throws Exception {
        final long millis = unit.toMillis(time);
        final long start = currentTimeMillis();
        while (true) {
            try {
                assertion.execute();
                return;
            } catch (final AssertionError e) {
                if (currentTimeMillis() - start > millis) {
                    throw e;
                } else {
                    sleep(5);
                }
            }
        }
    }

}
