package l.files.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import l.files.base.io.Closer;
import l.files.fs.FileSystem.Consumer;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class Files {

    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
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

    public static Stat newEmptyStat(Path path) {
        return path.fileSystem().newEmptyStat();
    }

    public static void stat(Path path, LinkOption option, Stat buffer) throws IOException {
        path.fileSystem().stat(path, option, buffer);
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

    /**
     * Creates this file and any missing parents as directories. This will
     * throw the same exceptions as {@link #createDir(Path)} except
     * will not error if already exists as a directory.
     */
    public static Path createDirs(Path path) throws IOException {
        try {
            if (stat(path, NOFOLLOW).isDirectory()) {
                return path;
            }
        } catch (FileNotFoundException ignore) {
        }

        Path parent = path.parent();
        if (parent != null) {
            createDirs(parent);
        }

        try {
            createDir(path);
        } catch (AlreadyExist ignore) {
        }

        return path;
    }

    public static Path createDir(Path path) throws IOException {
        path.fileSystem().createDir(path);
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

    public static void traverse(
            Path path,
            LinkOption option,
            TraversalCallback<? super Path> visitor) throws IOException {

        traverse(path, option, visitor, null);
    }

    /**
     * Performs a depth first traverse of this tree.
     * <p>
     * e.g. traversing the follow tree:
     * <pre>
     *     a
     *    / \
     *   b   c
     * </pre>
     * will generate:
     * <pre>
     * visitor.onPreVisit(a)
     * visitor.onPreVisit(b)
     * visitor.onPostVisit(b)
     * visitor.onPreVisit(c)
     * visitor.onPostVisit(c)
     * visitor.onPostVisit(a)
     * </pre>
     *
     * @param option applies to root only, child links are never followed
     */
    public static void traverse(
            Path path,
            LinkOption option,
            TraversalCallback<? super Path> visitor,
            Comparator<Path> childrenComparator) throws IOException {

        new Traverser(path, option, visitor, childrenComparator).traverse();
    }

    public static Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> consumer)
            throws IOException, InterruptedException {

        return path.fileSystem().observe(path, option, observer, consumer);
    }

    public static Observation observe(
            Path path,
            LinkOption option,
            Observer observer)
            throws IOException, InterruptedException {

        return observe(path, option, observer, new Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return true;
            }
        });

    }

    public static Observation observe(
            Path path,
            LinkOption option,
            BatchObserver batchObserver,
            Consumer<? super Path> childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit)
            throws IOException, InterruptedException {

        return observe(
                path,
                option,
                batchObserver,
                childrenConsumer,
                batchInterval,
                batchInternalUnit,
                true
        );
    }

    public static Observation observe(
            Path path,
            LinkOption option,
            BatchObserver batchObserver,
            Consumer<? super Path> childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit,
            boolean quickNotifyFirstEvent)
            throws IOException, InterruptedException {

        return new BatchObserverNotifier(
                batchObserver,
                batchInterval,
                batchInternalUnit,
                quickNotifyFirstEvent
        ).start(path, option, childrenConsumer);
    }

    public static void list(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException {

        path.fileSystem().list(path, option, consumer);
    }

    public static <C extends Collection<? super Path>> C list(
            final Path path,
            final LinkOption option,
            final C collection) throws IOException {

        path.fileSystem().list(path, option, new Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                collection.add(entry);
                return true;
            }
        });
        return collection;
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

    public static void deleteIfExists(Path path) throws IOException {
        try {
            delete(path);
        } catch (FileNotFoundException ignored) {
        }
    }

    public static void deleteRecursive(Path path) throws IOException {
        traverse(path, NOFOLLOW, new TraversalCallback.Base<Path>() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                deleteIfExists(path);
                return super.onPostVisit(path);
            }

            @Override
            public void onException(Path path, IOException e) throws IOException {
                if (e instanceof FileNotFoundException) {
                    return;
                }
                super.onException(path, e);
            }

        });
    }

    public static void deleteRecursiveIfExists(Path path) throws IOException {
        try {
            deleteRecursive(path);
        } catch (FileNotFoundException ignore) {
        }
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

    public static Reader newReader(Path path, Charset charset)
            throws IOException {
        return new InputStreamReader(newInputStream(path), charset);
    }

    public static Writer newWriter(Path path, Charset charset)
            throws IOException {
        return new OutputStreamWriter(newOutputStream(path), charset);
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

    public static String readAllUtf8(Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        Closer closer = Closer.create();
        try {
            Reader reader = closer.register(newReader(path, UTF_8));
            char[] buffer = new char[BUFFER_SIZE];
            for (int i; (i = reader.read(buffer)) != -1; ) {
                builder.append(buffer, 0, i);
            }

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        return builder.toString();
    }

    public static void writeUtf8(Path path, CharSequence content)
            throws IOException {
        write(path, content, UTF_8);
    }

    public static void write(
            Path path,
            CharSequence content,
            Charset charset) throws IOException {

        Closer closer = Closer.create();
        try {
            Writer writer = closer.register(newWriter(path, charset));
            writer.write(content.toString());

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static void appendUtf8(Path path, CharSequence content)
            throws IOException {

        Closer closer = Closer.create();
        try {
            Writer writer = closer.register(newWriter(path, UTF_8, true));
            writer.write(content.toString());

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static void copy(InputStream in, Path path) throws IOException {
        Closer closer = Closer.create();
        try {
            OutputStream out = closer.register(newOutputStream(path));
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i; (i = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, i);
            }
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    public static void removePermissions(Path path, Set<Permission> permissions)
            throws IOException {
        Set<Permission> existing = stat(path, FOLLOW).permissions();
        Set<Permission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        setPermissions(path, perms);
    }

    public static void setPermissions(Path path, Set<Permission> perms)
            throws IOException {
        path.fileSystem().setPermissions(path, perms);
    }

}
