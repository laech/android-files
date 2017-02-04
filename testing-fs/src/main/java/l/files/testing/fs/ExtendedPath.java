package l.files.testing.fs;

import android.annotation.SuppressLint;

import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;
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

import javax.annotation.Nullable;

import l.files.fs.AlreadyExist;
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import static com.google.common.base.Charsets.UTF_8;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

@SuppressLint("ParcelCreator")
public final class ExtendedPath extends Path {

    private final Path delegate;

    private ExtendedPath(Path delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public static ExtendedPath wrap(Path path) {
        if (path instanceof ExtendedPath) {
            return (ExtendedPath) path;
        }
        return new ExtendedPath(path);
    }

    @Nullable
    @Override
    public ExtendedPath parent() {
        Path parent = delegate.parent();
        if (parent == null) {
            return null;
        }
        return wrap(parent);
    }

    @Override
    public boolean isHidden() {
        return delegate.isHidden();
    }

    @Override
    public boolean startsWith(Path prefix) {
        return delegate.startsWith(prefix);
    }

    @Override
    public Path rebase(Path oldPrefix, Path newPrefix) {
        return delegate.rebase(oldPrefix, newPrefix);
    }

    @Override
    public ExtendedPath concat(Name name) {
        return wrap(super.concat(name));
    }

    @Override
    public void toByteArray(ByteArrayOutputStream out) {
        delegate.toByteArray(out);
    }

    @Override
    public Path toAbsolutePath() {
        return delegate.toAbsolutePath();
    }

    @Override
    public ExtendedPath concat(Path path) {
        return wrap(delegate.concat(path));
    }

    @Override
    public ExtendedPath concat(String path) {
        return wrap(super.concat(path));
    }

    @Override
    public ExtendedPath concat(byte[] path) {
        return wrap(super.concat(path));
    }

    @Override
    public ImmutableList<Name> names() {
        return delegate.names();
    }

    @Nullable
    @Override
    public Name name() {
        return delegate.name();
    }

    @Override
    public ExtendedPath createDirectory() throws IOException {
        super.createDirectory();
        return this;
    }

    @Override
    public ExtendedPath createDirectory(Set<Permission> permissions)
            throws IOException {
        super.createDirectory(permissions);
        return this;
    }

    @Override
    public ExtendedPath createFile() throws IOException {
        super.createFile();
        return this;
    }

    @Override
    public ExtendedPath createSymbolicLink(Path target) throws IOException {
        super.createSymbolicLink(target);
        return this;
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

    @Override
    public ExtendedPath createDirectories() throws IOException {
        return (ExtendedPath) super.createDirectories();
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
            parent.createDirectories();
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
                wrap(path).deleteIfExists();
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

    public void copy(InputStream in)
            throws IOException {
        OutputStream out = newOutputStream(false);
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
