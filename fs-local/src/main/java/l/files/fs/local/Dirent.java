package l.files.fs.local;

final class Dirent extends Native {

    static {
        init();
    }

    private static native void init();

    static native <E extends Throwable> void list(
            byte[] path,
            boolean followLink,
            Callback<E> callback) throws ErrnoException, E;

    interface Callback<E extends Throwable> {

        boolean onNext(byte[] nameBuffer, int nameLength, boolean isDirectory) throws E;

    }

}
