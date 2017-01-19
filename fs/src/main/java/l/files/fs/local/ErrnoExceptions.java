package l.files.fs.local;

import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

import l.files.fs.AlreadyExist;
import l.files.fs.DirectoryNotEmpty;
import linux.ErrnoException;

import static linux.Errno.EEXIST;
import static linux.Errno.ENOENT;
import static linux.Errno.ENOTEMPTY;

final class ErrnoExceptions {

    private ErrnoExceptions() {
    }

    static IOException toIOException(ErrnoException cause, Object... paths) {
        String message = TextUtils.join(", ", paths);
        if (cause.errno == ENOENT) {
            FileNotFoundException e = new FileNotFoundException(message);
            e.initCause(cause);
            return e;
        }
        if (cause.errno == EEXIST) {
            return new AlreadyExist(message, cause);
        }
        if (cause.errno == ENOTEMPTY) {
            return new DirectoryNotEmpty(message, cause);
        }
        return new IOException(message, cause);
    }

}
