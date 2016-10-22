package l.files.fs;

import java.io.File;

import static l.files.fs.Files.UTF_8;

public final class Paths {

    private Paths() {
    }

    public static Path get(File file) {
        return get(file.getAbsolutePath().getBytes(UTF_8));
    }

    public static Path get(String path) {
        return get(new File(path));
    }

    public static Path get(byte[] path) {
        return Holder.FILE_SYSTEM.path(path);
    }

    static class Holder {

        static FileSystem FILE_SYSTEM;

        static {
            try {
                FILE_SYSTEM =
                        (FileSystem) Class
                                .forName("l.files.fs.local.LocalFileSystem")
                                .getField("INSTANCE")
                                .get(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
