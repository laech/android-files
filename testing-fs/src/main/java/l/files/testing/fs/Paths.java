package l.files.testing.fs;

import l.files.fs.Path;
import l.files.fs.Path.Consumer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static l.files.base.io.Charsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class Paths {

    private Paths() {
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
    public static java.nio.file.Path createFiles(java.nio.file.Path path)
        throws IOException {
        createDirectories(path.getParent());
        try {
            createFile(path);
        } catch (FileAlreadyExistsException e) {
            if (!isRegularFile(path, NOFOLLOW_LINKS)) {
                throw e;
            }
        }
        return path;
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
    ) throws IOException {
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

    public static void deleteRecursive(java.nio.file.Path path)
        throws IOException {
        walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>() {
            @Override
            public FileVisitResult visitFile(
                java.nio.file.Path file,
                BasicFileAttributes attrs
            ) throws IOException {
                delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(
                java.nio.file.Path dir,
                IOException exc
            ) throws IOException {
                delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    public static void deleteRecursiveIfExists(java.nio.file.Path path)
        throws IOException {
        try {
            deleteRecursive(path);
        } catch (NoSuchFileException ignore) {
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
