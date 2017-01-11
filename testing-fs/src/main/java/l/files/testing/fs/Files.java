package l.files.testing.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.AlreadyExist;
import l.files.fs.FileSystem;
import l.files.fs.LinkOption;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class Files { // TODO make this a subclass of FileSystem

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private Files() {
    }

    public static <C extends Collection<? super Path>> C listDirs(
            final FileSystem fs,
            final Path path,
            final LinkOption option,
            final C collection) throws IOException {

        fs.listDirs(path, option, new FileSystem.Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                collection.add(entry);
                return true;
            }
        });
        return collection;
    }

    /**
     * Creates this file and any missing parents as directories. This will
     * throw the same exceptions as {@link FileSystem#createDir(Path)} except
     * will not error if already exists as a directory.
     */
    public static Path createDirs(FileSystem fs, Path path) throws IOException {
        try {
            if (fs.stat(path, NOFOLLOW).isDirectory()) {
                return path;
            }
        } catch (FileNotFoundException ignore) {
        }

        Path parent = path.parent();
        if (parent != null) {
            createDirs(fs, parent);
        }

        try {
            fs.createDir(path);
        } catch (AlreadyExist ignore) {
        }

        return path;
    }


    /**
     * Creates this file as a file and creates any missing parents. This
     * will throw the same exceptions as {@link FileSystem#createFile(Path)} except
     * will not error if already exists.
     */
    public static Path createFiles(FileSystem fs, Path path) throws IOException {
        try {
            if (fs.stat(path, NOFOLLOW).isRegularFile()) {
                return path;
            }
        } catch (FileNotFoundException ignore) {
        }

        Path parent = path.parent();
        if (parent != null) {
            createDirs(fs, parent);
        }

        try {
            fs.createFile(path);
        } catch (AlreadyExist ignore) {
        }

        return path;
    }

    public static void removePermissions(
            FileSystem fs,
            Path path,
            Set<Permission> permissions) throws IOException {
        Set<Permission> existing = fs.stat(path, FOLLOW).permissions();
        Set<Permission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        fs.setPermissions(path, perms);
    }

    public static Reader newReader(
            FileSystem fs,
            Path path,
            Charset charset) throws IOException {
        return new InputStreamReader(fs.newInputStream(path), charset);
    }

    public static Writer newWriter(
            FileSystem fs,
            Path path,
            Charset charset,
            boolean append) throws IOException {
        return new OutputStreamWriter(fs.newOutputStream(path, append), charset);
    }

    public static String readAllUtf8(
            FileSystem fs,
            Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        Reader reader = newReader(fs, path, UTF_8);
        try {
            char[] buffer = new char[8192];
            for (int i; (i = reader.read(buffer)) != -1; ) {
                builder.append(buffer, 0, i);
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

    public static void writeUtf8(
            FileSystem fs,
            Path path,
            CharSequence content) throws IOException {
        write(fs, path, content, UTF_8);
    }

    public static void write(
            FileSystem fs,
            Path path,
            CharSequence content,
            Charset charset) throws IOException {

        Writer writer = newWriter(fs, path, charset, false);
        try {
            writer.write(content.toString());
        } finally {
            writer.close();
        }
    }

    public static void appendUtf8(
            FileSystem fs,
            Path path,
            CharSequence content) throws IOException {

        Writer writer = newWriter(fs, path, UTF_8, true);
        try {
            writer.write(content.toString());
        } finally {
            writer.close();
        }
    }


    public static void deleteIfExists(
            FileSystem fs,
            Path path) throws IOException {
        try {
            fs.delete(path);
        } catch (FileNotFoundException ignored) {
        }
    }

    public static void deleteRecursive(
            final FileSystem fs,
            final Path path) throws IOException {

        fs.traverse(path, NOFOLLOW, new TraversalCallback.Base<Path>() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                deleteIfExists(fs, path);
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

    public static void deleteRecursiveIfExists(
            FileSystem fs,
            Path path) throws IOException {
        try {
            deleteRecursive(fs, path);
        } catch (FileNotFoundException ignore) {
        }
    }

    public static void copy(InputStream in, FileSystem fs, Path path) throws IOException {
        OutputStream out = fs.newOutputStream(path, false);
        try {
            byte[] buffer = new byte[8192];
            for (int i; (i = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, i);
            }
        } finally {
            out.close();
        }
    }
}
