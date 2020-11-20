package l.files.testing.fs;

import l.files.fs.LinkOption;
import l.files.fs.Path;
import l.files.fs.Path.Consumer;
import l.files.fs.TraversalCallback;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.Files.getFileAttributeView;
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

    public static void removeReadPermissions(
        java.nio.file.Path path,
        java.nio.file.LinkOption... options
    ) throws IOException {
        removePermissions(
            path,
            PosixFilePermissions.fromString("r--r--r--"),
            options
        );
    }

    public static void removeWritePermissions(
        java.nio.file.Path path,
        java.nio.file.LinkOption... options
    ) throws IOException {
        removePermissions(
            path,
            PosixFilePermissions.fromString("-w--w--w-"),
            options
        );
    }

    public static void removePermissions(
        java.nio.file.Path path,
        Set<PosixFilePermission> permissions,
        java.nio.file.LinkOption... options
    ) throws IOException {
        PosixFileAttributeView view =
            getFileAttributeView(path, PosixFileAttributeView.class, options);
        Set<PosixFilePermission> newPermissions =
            EnumSet.copyOf(view.readAttributes().permissions());
        newPermissions.removeAll(permissions);
        view.setPermissions(newPermissions);
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
