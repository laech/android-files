package l.files.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import l.files.fs.FileSystem.Consumer;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class Files {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private Files() {
    }

    private static final int BUFFER_SIZE = 8192;

    public static List<Path> hierarchy(Path path) {
        List<Path> hierarchy = new ArrayList<>();
        for (Path p = path; p != null; p = p.parent()) {
            hierarchy.add(p);
        }
        reverse(hierarchy);
        return unmodifiableList(hierarchy);
    }

    public static Stat stat(Path path, LinkOption option) throws IOException {
        return path.fileSystem().stat(path, option);
    }

    public static boolean exists(Path path, LinkOption option) throws IOException {
        return path.fileSystem().exists(path, option);
    }


    public static boolean isReadable(Path path) throws IOException {
        return path.fileSystem().isReadable(path);
    }

    public static boolean isWritable(Path path) throws IOException {
        return path.fileSystem().isWritable(path);
    }

    public static boolean isExecutable(Path path) throws IOException {
        return path.fileSystem().isExecutable(path);
    }

    public static Path createDir(Path path) throws IOException {
        path.fileSystem().createDir(path);
        return path;
    }

    public static Path createDir(Path path, Set<Permission> permissions) throws IOException {
        path.fileSystem().createDir(path, permissions);
        return path;
    }

    /**
     * Creates this file as a file and creates any missing parents. This
     * will throw the same exceptions as {@link #createFile(Path)} except
     * will not error if already exists.
     */
    public static Path createFiles(Path path) throws IOException {
        try {
            if (stat(path, NOFOLLOW).isRegularFile()) {
                return path;
            }
        } catch (FileNotFoundException ignore) {
        }

        Path parent = path.parent();
        if (parent != null) {
            createDirs(parent);
        }

        try {
            createFile(path);
        } catch (AlreadyExist ignore) {
        }

        return path;
    }

    public static Path createFile(Path path) throws IOException {
        path.fileSystem().createFile(path);
        return path;
    }

    public static Path createSymbolicLink(Path link, Path target)
            throws IOException {
        target.fileSystem().createSymbolicLink(link, target);
        return link;
    }

    public static void move(Path src, Path dst) throws IOException {
        src.fileSystem().move(src, dst);
    }

    public static Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> consumer,
            @Nullable String logTag,
            int watchLimit)
            throws IOException, InterruptedException {

        return path.fileSystem().observe(path, option, observer, consumer, logTag, watchLimit);
    }

    public static void list(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException {

        path.fileSystem().list(path, option, consumer);
    }

    public static <C extends Collection<? super Path>> C listDirs(
            final Path path,
            final LinkOption option,
            final C collection) throws IOException {

        path.fileSystem().listDirs(path, option, new Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                collection.add(entry);
                return true;
            }
        });
        return collection;
    }

    public static void listDirs(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException {

        path.fileSystem().listDirs(path, option, consumer);
    }

    public static void delete(Path path) throws IOException {
        path.fileSystem().delete(path);
    }

    public static OutputStream newOutputStream(Path path) throws IOException {
        return newOutputStream(path, false);
    }

    public static OutputStream newOutputStream(Path path, boolean append)
            throws IOException {
        return path.fileSystem().newOutputStream(path, append);
    }

    public static InputStream newInputStream(Path path)
            throws IOException {
        return path.fileSystem().newInputStream(path);
    }

    public static InputStream newBufferedInputStream(Path path)
            throws IOException {
        return new BufferedInputStream(newInputStream(path));
    }

    public static DataInputStream newBufferedDataInputStream(Path path)
            throws IOException {
        return new DataInputStream(newBufferedInputStream(path));
    }

    public static OutputStream newBufferedOutputStream(Path path)
            throws IOException {
        return new BufferedOutputStream(newOutputStream(path));
    }

    public static DataOutputStream newBufferedDataOutputStream(Path path)
            throws IOException {
        return new DataOutputStream(newBufferedOutputStream(path));
    }

    public static Writer newWriter(
            Path path,
            Charset charset,
            boolean append) throws IOException {
        return new OutputStreamWriter(newOutputStream(path, append), charset);
    }

    public static void setLastModifiedTime(
            Path path,
            LinkOption option,
            Instant time) throws IOException {
        path.fileSystem().setLastModifiedTime(path, option, time);
    }

    public static Path readSymbolicLink(Path path) throws IOException {
        return path.fileSystem().readSymbolicLink(path);
    }

    public static void setPermissions(Path path, Set<Permission> perms)
            throws IOException {
        path.fileSystem().setPermissions(path, perms);
    }

}
