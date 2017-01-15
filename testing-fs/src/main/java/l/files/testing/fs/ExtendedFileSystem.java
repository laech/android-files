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
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ExtendedFileSystem extends ForwardingFileSystem {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public ExtendedFileSystem(FileSystem delegate) {
        super(delegate);
    }


    public Observation observe(
            Path path,
            LinkOption option,
            Observer observer
    ) throws IOException, InterruptedException {

        return observe(path, option, observer, new Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return true;
            }
        }, null, -1);
    }

    public void listDirs(
            final Path path,
            final LinkOption option,
            final Consumer<? super Path> consumer
    ) throws IOException {

        list(path, option, new Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return !stat(entry, NOFOLLOW).isDirectory() ||
                        consumer.accept(entry);
            }
        });
    }

    public <C extends Collection<? super Path>> C listDirs(
            final Path path,
            final LinkOption option,
            final C collection
    ) throws IOException {

        listDirs(path, option, new FileSystem.Consumer<Path>() {
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
    public Path createDirs(Path path) throws IOException {
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

    /**
     * Creates this file as a file and creates any missing parents. This
     * will throw the same exceptions as {@link FileSystem#createFile(Path)}
     * except will not error if already exists.
     */
    public Path createFiles(Path path) throws IOException {
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

    public void removePermissions(Path path, Set<Permission> permissions)
            throws IOException {
        Set<Permission> existing = stat(path, FOLLOW).permissions();
        Set<Permission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        setPermissions(path, perms);
    }

    public Reader newReader(Path path, Charset charset) throws IOException {
        return new InputStreamReader(newInputStream(path), charset);
    }

    public Writer newWriter(Path path, Charset charset, boolean append)
            throws IOException {
        return new OutputStreamWriter(
                newOutputStream(path, append), charset
        );
    }

    public String readAllUtf8(Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        Reader reader = newReader(path, UTF_8);
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

    public void writeUtf8(Path path, CharSequence content) throws IOException {
        write(path, content, UTF_8);
    }

    public void write(Path path, CharSequence content, Charset charset)
            throws IOException {

        Writer writer = newWriter(path, charset, false);
        try {
            writer.write(content.toString());
        } finally {
            writer.close();
        }
    }

    public void appendUtf8(Path path, CharSequence content)
            throws IOException {
        Writer writer = newWriter(path, UTF_8, true);
        try {
            writer.write(content.toString());
        } finally {
            writer.close();
        }
    }


    public void deleteIfExists(Path path) throws IOException {
        try {
            delete(path);
        } catch (FileNotFoundException ignored) {
        }
    }

    public void deleteRecursive(final Path path) throws IOException {
        traverse(path, NOFOLLOW, new TraversalCallback.Base<Path>() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                deleteIfExists(path);
                return super.onPostVisit(path);
            }

            @Override
            public void onException(Path path, IOException e)
                    throws IOException {
                if (e instanceof FileNotFoundException) {
                    return;
                }
                super.onException(path, e);
            }

        });
    }

    public void deleteRecursiveIfExists(Path path) throws IOException {
        try {
            deleteRecursive(path);
        } catch (FileNotFoundException ignore) {
        }
    }

    public void copy(InputStream in, FileSystem fs, Path path)
            throws IOException {
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
