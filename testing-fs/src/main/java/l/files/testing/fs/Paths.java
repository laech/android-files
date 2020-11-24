package l.files.testing.fs;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public final class Paths {

    private Paths() {
    }

    /**
     * Creates this file as a file and creates any missing parents. This
     * will throw the same exceptions as
     * {@link Files#createFile(Path, FileAttribute[])}
     * except will not error if already exists.
     */
    public static Path createFiles(Path path)
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

    public static void removeReadPermissions(Path path, LinkOption... options)
        throws IOException {
        removePermissions(
            path,
            PosixFilePermissions.fromString("r--r--r--"),
            options
        );
    }

    public static void removeWritePermissions(Path path, LinkOption... options)
        throws IOException {
        removePermissions(
            path,
            PosixFilePermissions.fromString("-w--w--w-"),
            options
        );
    }

    public static void removePermissions(
        Path path,
        Set<PosixFilePermission> permissions,
        LinkOption... options
    ) throws IOException {
        PosixFileAttributeView view =
            getFileAttributeView(path, PosixFileAttributeView.class, options);
        Set<PosixFilePermission> newPermissions =
            EnumSet.copyOf(view.readAttributes().permissions());
        newPermissions.removeAll(permissions);
        view.setPermissions(newPermissions);
    }

    public static void deleteRecursive(Path path)
        throws IOException {
        walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(
                Path file,
                BasicFileAttributes attrs
            ) throws IOException {
                delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(
                Path dir,
                IOException exc
            ) throws IOException {
                delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    public static void deleteRecursiveIfExists(Path path)
        throws IOException {
        try {
            deleteRecursive(path);
        } catch (NoSuchFileException ignore) {
        }
    }

}
