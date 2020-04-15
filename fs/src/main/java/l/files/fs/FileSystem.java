package l.files.fs;

import java.io.IOException;
import java.util.Arrays;

import l.files.fs.Path.Consumer;
import linux.Dirent;
import linux.Dirent.DIR;
import linux.ErrnoException;

final class FileSystem extends Native {

    public static final FileSystem INSTANCE = new FileSystem();

    void list(
            Path path,
            Consumer consumer
    ) throws IOException, ErrnoException {

        DIR dir = Dirent.opendir(path.toByteArray());
        try {
            Dirent entry = new Dirent();
            while ((entry = Dirent.readdir(dir, entry)) != null) {
                if (entry.isSelfOrParent()) {
                    continue;
                }
                byte[] name = Arrays.copyOfRange(entry.d_name, 0, entry.d_name_len);
                if (!consumer.accept(path.concat(name))) {
                    break;
                }
            }
        } finally {
            Dirent.closedir(dir);
        }
    }

}
