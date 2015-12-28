package l.files.fs;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

public final class Paths {

    private Paths() {
    }

    public static Path get(URI uri) {
        if (uri.getScheme() == null) {
            throw new IllegalArgumentException(uri.toString());
        }
        for (FileSystem fs : Holder.FILE_SYSTEMS) {
            if (fs.scheme().equals(uri.getScheme())) {
                return fs.path(uri);
            }
        }
        throw new IllegalArgumentException(uri.toString());
    }

    public static Path get(File file) {
        return get(file.toURI());
    }

    public static Path get(String localPath) {
        return get(new File(localPath));
    }

    public static Path get(byte[] localPath) {
        for (FileSystem fs : Holder.FILE_SYSTEMS) {
            if (fs.scheme().equals("file")) {
                return fs.path(localPath);
            }
        }
        throw new IllegalArgumentException(Arrays.toString(localPath));
    }

    private static class Holder {

        static FileSystem[] FILE_SYSTEMS;

        static {
            List<FileSystem> fileSystems = new ArrayList<>();
            for (FileSystem fs : ServiceLoader.load(FileSystem.class)) {
                fileSystems.add(fs);
            }
            FILE_SYSTEMS = fileSystems.toArray(new FileSystem[fileSystems.size()]);
        }
    }

}
