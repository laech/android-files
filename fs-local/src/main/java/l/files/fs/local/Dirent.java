package l.files.fs.local;

import java.io.IOException;

final class Dirent extends Native {

    static {
        init();
    }

    private static native void init();

    static native void list(
            byte[] path,
            boolean followLink,
            Callback callback) throws ErrnoException, IOException;

    interface Callback {

        boolean onNext(
                byte[] nameBuffer,
                int nameLength,
                boolean isDirectory) throws IOException;

    }

}
