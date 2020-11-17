package l.files.testing.fs;

import l.files.fs.LinkOption;
import l.files.fs.Path;
import l.files.fs.Path.Consumer;
import l.files.fs.TraversalCallback;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import l.files.fs.exception.AlreadyExist;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static l.files.base.io.Charsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class Paths {

    private Paths() {
    }

    public static Observation observe(
        Path path,
        LinkOption option,
        Observer observer
    ) throws IOException, InterruptedException {

        return path.observe(option, observer, __ -> {}, null, -1);
    }

    public static void listDirectories(
        Path path,
        Consumer consumer
    ) throws IOException {

        path.list((Consumer) entry -> !entry.stat(NOFOLLOW).isDirectory() ||
            consumer.accept(entry));
    }

    public static <C extends Collection<? super Path>> C listDirectories(
        Path path,
        C collection
    ) throws IOException {

        listDirectories(path, (Consumer) entry -> {
            collection.add(entry);
            return true;
        });
        return collection;
    }

    /**
     * Creates this file as a file and creates any missing parents. This
     * will throw the same exceptions as {@link Path#createFile()}
     * except will not error if already exists.
     */
    public static Path createFiles(Path path) throws IOException {
        try {
            if (path.stat(NOFOLLOW).isRegularFile()) {
                return path;
            }
        } catch (FileNotFoundException | NoSuchFileException ignore) {
        }

        Path parent = path.parent();
        if (parent != null) {
            parent.createDirectories();
        }

        try {
            path.createFile();
        } catch (AlreadyExist ignore) {
        }

        return path;
    }

    public static void removePermissions(
        Path path,
        Set<PosixFilePermission> permissions
    ) throws IOException {
        Set<PosixFilePermission> existing = path.readAttributes(
            PosixFileAttributes.class,
            java.nio.file.LinkOption.NOFOLLOW_LINKS
        ).permissions();
        Set<PosixFilePermission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        path.setPermissions(perms);
    }

    public static Reader newReader(Path path, Charset charset)
        throws IOException {
        return new InputStreamReader(path.newInputStream(), charset);
    }

    public static Writer newWriter(
        Path path,
        Charset charset,
        OpenOption... options
    )
        throws IOException {
        return new OutputStreamWriter(path.newOutputStream(options), charset);
    }

    public static String readAllUtf8(Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = newReader(path, UTF_8)) {
            char[] buffer = new char[8192];
            for (int i; (i = reader.read(buffer)) != -1; ) {
                builder.append(buffer, 0, i);
            }
        }
        return builder.toString();
    }

    public static void writeUtf8(Path path, CharSequence content)
        throws IOException {
        write(path, content, UTF_8);
    }

    public static void write(Path path, CharSequence content, Charset charset)
        throws IOException {

        try (Writer writer = newWriter(path, charset)) {
            writer.write(content.toString());
        }
    }

    public static void appendUtf8(Path path, CharSequence content)
        throws IOException {
        try (Writer writer = newWriter(path, UTF_8, CREATE, APPEND)) {
            writer.write(content.toString());
        }
    }


    public static void deleteIfExists(Path path) throws IOException {
        try {
            path.delete();
        } catch (FileNotFoundException | NoSuchFileException ignored) {
        }
    }

    public static void deleteRecursive(Path path) throws IOException {
        path.traverse(NOFOLLOW, new TraversalCallback.Base<Path>() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                deleteIfExists(path);
                return super.onPostVisit(path);
            }

            @Override
            public void onException(Path path, IOException e)
                throws IOException {
                if (e instanceof FileNotFoundException ||
                    e instanceof NoSuchFileException) {
                    return;
                }
                super.onException(path, e);
            }

        });
    }

    public static void deleteRecursiveIfExists(Path path) throws IOException {
        try {
            deleteRecursive(path);
        } catch (FileNotFoundException | NoSuchFileException ignore) {
        }
    }

    public static void copy(InputStream in, Path to)
        throws IOException {
        try (OutputStream out = to.newOutputStream()) {
            byte[] buffer = new byte[8192];
            for (int i; (i = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, i);
            }
        }
    }

}
