package l.files.base;

import android.util.Log;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

public final class Throwables {

    private Throwables() {
    }

    public static void addSuppressed(Throwable e, Throwable suppressed) {
        if (SDK_INT >= KITKAT) {
            e.addSuppressed(suppressed);
        } else {
            Log.w(Throwables.class.getSimpleName(),
                    "addSuppressed(...)", e);
        }
    }

}
