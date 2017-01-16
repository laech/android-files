package l.files.testing.fs;

import android.annotation.SuppressLint;

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
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

@SuppressLint("ParcelCreator")
public final class ExtendedPath extends ForwardingPath {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private ExtendedPath(Path delegate) {
        super(delegate);
    }

    public static ExtendedPath wrap(Path path) {
        if (path instanceof ExtendedPath) {
            return (ExtendedPath) path;
        }
        return new ExtendedPath(path);
    }

    @Override
    public ExtendedPath concat(Name name) {
        return new ExtendedPath(super.concat(name));
    }

    @Override
    public ExtendedPath concat(Path path) {
        return new ExtendedPath(super.concat(path));
    }

    @Override
    public ExtendedPath concat(String path) {
        return new ExtendedPath(super.concat(path));
    }

    @Override
    public ExtendedPath concat(byte[] path) {
        return new ExtendedPath(super.concat(path));
    }

    @Override
    public ExtendedPath createDir() throws IOException {
        return (ExtendedPath) super.createDir();
    }

    @Override
    public ExtendedPath createDir(Set<Permission> permissions)
            throws IOException {
        return (ExtendedPath) super.createDir(permissions);
    }

    @Override
    public ExtendedPath createFile() throws IOException {
        return (ExtendedPath) super.createFile();
    }

    @Override
    public ExtendedPath createSymbolicLink(Path target) throws IOException {
        return (ExtendedPath) super.createSymbolicLink(target);
    }

    public Observation observe(LinkOption option, Observer observer)
            throws IOException, InterruptedException {

        return observe(option, observer, new Consumer() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return true;
            }
        }, null, -1);
    }

    public void listDirs(final LinkOption option, final Consumer consumer)
            throws IOException {

        list(option, new Consumer() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return !entry.stat(NOFOLLOW).isDirectory() ||
                        consumer.accept(entry);
            }
        });
    }

    public <C extends Collection<? super Path>> C listDirs(
            final LinkOption option,
            final C collection
    ) throws IOException {

        listDirs(option, new Consumer() {
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
     * throw the same exceptions as {@link Path#createDir()} except
     * will not error if already exists as a directory.
     */
    public ExtendedPath createDirs() throws IOException {
        try {
            if (stat(NOFOLLOW).isDirectory()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
        }

        ExtendedPath parent = parent();
        if (parent != null) {
            parent.createDirs();
        }

        try {
            createDir();
        } catch (AlreadyExist ignore) {
        }

        return this;
    }

    /**
     * Creates this file as a file and creates any missing parents. This
     * will throw the same exceptions as {@link Path#createFile()}
     * except will not error if already exists.
     */
    public ExtendedPath createFiles() throws IOException {
        try {
            if (stat(NOFOLLOW).isRegularFile()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
        }

        ExtendedPath parent = parent();
        if (parent != null) {
            parent.createDirs();
        }

        try {
            createFile();
        } catch (AlreadyExist ignore) {
        }

        return this;
    }

    public void removePermissions(Set<Permission> permissions)
            throws IOException {
        Set<Permission> existing = stat(FOLLOW).permissions();
        Set<Permission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        setPermissions(perms);
    }

    public Reader newReader(Charset charset) throws IOException {
        return new InputStreamReader(newInputStream(), charset);
    }

    public Writer newWriter(Charset charset, boolean append)
            throws IOException {
        return new OutputStreamWriter(newOutputStream(append), charset);
    }

    public String readAllUtf8() throws IOException {
        StringBuilder builder = new StringBuilder();
        Reader reader = newReader(UTF_8);
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

    public void writeUtf8(CharSequence content) throws IOException {
        write(content, UTF_8);
    }

    public void write(CharSequence content, Charset charset)
            throws IOException {

        Writer writer = newWriter(charset, false);
        try {
            writer.write(content.toString());
        } finally {
            writer.close();
        }
    }

    public void appendUtf8(CharSequence content)
            throws IOException {
        Writer writer = newWriter(UTF_8, true);
        try {
            writer.write(content.toString());
        } finally {
            writer.close();
        }
    }


    public void deleteIfExists() throws IOException {
        try {
            delete();
        } catch (FileNotFoundException ignored) {
        }
    }

    public void deleteRecursive() throws IOException {
        traverse(NOFOLLOW, new TraversalCallback.Base<Path>() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                new ExtendedPath(path).deleteIfExists();
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

    public void deleteRecursiveIfExists() throws IOException {
        try {
            deleteRecursive();
        } catch (FileNotFoundException ignore) {
        }
    }

    public void copy(InputStream in, Path path)
            throws IOException {
        OutputStream out = path.newOutputStream(false);
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
