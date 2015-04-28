package l.files.ui.browser;

import android.system.ErrnoException;
import android.system.OsConstants;

import java.io.IOException;

final class IOExceptions {

    private IOExceptions() {
    }

    static String getFailureMessage(IOException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof android.system.ErrnoException) {
            return OsConstants.errnoName(((ErrnoException) cause).errno);
        } else if (cause instanceof l.files.fs.local.ErrnoException) {
            return cause.getMessage();
        }
        return exception.getMessage();
    }

}
