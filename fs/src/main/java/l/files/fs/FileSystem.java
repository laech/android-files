package l.files.fs;

import java.io.IOException;
import java.util.Arrays;

import l.files.fs.Path.Consumer;
import linux.Dirent;
import linux.Dirent.DIR;
import linux.ErrnoException;
import linux.Fcntl;

import static l.files.fs.LinkOption.NOFOLLOW;
import static linux.Fcntl.O_DIRECTORY;
import static linux.Fcntl.O_NOFOLLOW;

final class FileSystem extends Native {

    public static final FileSystem INSTANCE = new FileSystem();

    void list(
            Path path,
            LinkOption option,
            Consumer consumer
    ) throws IOException, ErrnoException {

        int flags = O_DIRECTORY;
        if (option == NOFOLLOW) {
            flags |= O_NOFOLLOW;
        }

        int fd = Fcntl.open(path.toByteArray(), flags, 0);
        DIR dir = Dirent.fdopendir(fd);
        try {
            Dirent entry = new Dirent();
            while ((entry = Dirent.readdir(dir, entry)) != null) {
                if (isSelfOrParent(entry)) {
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

    static boolean isSelfOrParent(Dirent entry) {
        int len = entry.d_name_len;
        byte[] name = entry.d_name;
        return (len == 1 && name[0] == '.') ||
                (len == 2 && name[0] == '.' && name[1] == '.');
    }

}
