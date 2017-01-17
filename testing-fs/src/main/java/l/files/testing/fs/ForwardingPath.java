package l.files.testing.fs;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Parcel;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.annotation.Nullable;

import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import static l.files.base.Objects.requireNonNull;

@SuppressLint("ParcelCreator")
public class ForwardingPath extends Path {

    protected final Path delegate;

    public ForwardingPath(Path delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public byte[] toByteArray() {
        return delegate.toByteArray();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public Uri toUri() {
        return delegate.toUri();
    }

    @Override
    public Path toAbsolutePath() {
        return delegate.toAbsolutePath();
    }

    @Override
    public ImmutableList<Name> names() {
        return delegate.names();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ForwardingPath) {
            return equals(((ForwardingPath) o).delegate);
        }
        if (delegate instanceof ForwardingPath) {
            return ((ForwardingPath) delegate).delegate.equals(o);
        }
        return delegate.equals(o);
    }

    @Override
    public Path concat(Path path) {
        return delegate.concat(path);
    }

    @Override
    public Path concat(Name name) {
        return delegate.concat(name);
    }

    @Override
    public Path concat(String path) {
        return delegate.concat(path);
    }

    @Override
    public Path concat(byte[] path) {
        return delegate.concat(path);
    }

    @Nullable
    @Override
    public ExtendedPath parent() {
        Path parent = delegate.parent();
        if (parent != null) {
            return ExtendedPath.wrap(parent);
        }
        return null;
    }

    @Nullable
    @Override
    public Name name() {
        return delegate.name();
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
    public void setPermissions(Set<Permission> permissions)
            throws IOException {
        delegate.setPermissions(permissions);
    }

    @Override
    public void setLastModifiedTime(LinkOption option, Instant instant)
            throws IOException {
        delegate.setLastModifiedTime(option, instant);
    }

    @Override
    public Stat stat(LinkOption option) throws IOException {
        return delegate.stat(option);
    }

    @Override
    public Path createDir() throws IOException {
        return delegate.createDir();
    }

    @Override
    public Path createDir(Set<Permission> permissions) throws IOException {
        return delegate.createDir(permissions);
    }

    @Override
    public Path createFile() throws IOException {
        return delegate.createFile();
    }

    @Override
    public Path createSymbolicLink(Path target) throws IOException {
        return delegate.createSymbolicLink(target);
    }

    @Override
    public Path readSymbolicLink() throws IOException {
        return delegate.readSymbolicLink();
    }

    @Override
    public void move(Path destination) throws IOException {
        delegate.move(destination);
    }

    @Override
    public void delete() throws IOException {
        delegate.delete();
    }

    @Override
    public boolean exists(LinkOption option) throws IOException {
        return delegate.exists(option);
    }

    @Override
    public boolean isReadable() throws IOException {
        return delegate.isReadable();
    }

    @Override
    public boolean isWritable() throws IOException {
        return delegate.isWritable();
    }

    @Override
    public boolean isExecutable() throws IOException {
        return delegate.isExecutable();
    }

    @Override
    public Observation observe(
            LinkOption option,
            Observer observer,
            Consumer childrenConsumer,
            @Nullable String logTag,
            int watchLimit
    ) throws IOException, InterruptedException {

        return delegate.observe(
                option,
                observer,
                childrenConsumer,
                logTag,
                watchLimit
        );
    }

    @Override
    public void list(LinkOption option, Consumer consumer) throws IOException {
        delegate.list(option, consumer);
    }

    @Override
    public InputStream newInputStream() throws IOException {
        return delegate.newInputStream();
    }

    @Override
    public OutputStream newOutputStream(boolean append) throws IOException {
        return delegate.newOutputStream(append);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException();
    }
}
