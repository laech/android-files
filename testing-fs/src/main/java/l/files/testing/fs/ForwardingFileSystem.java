package l.files.testing.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.annotation.Nullable;

import l.files.fs.FileSystem;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import static l.files.base.Objects.requireNonNull;

public class ForwardingFileSystem extends FileSystem {

    protected final FileSystem delegate;

    public ForwardingFileSystem(FileSystem delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public void setPermissions(Path path, Set<Permission> permissions)
            throws IOException {
        delegate.setPermissions(path, permissions);
    }

    @Override
    public void setLastModifiedTime(
            Path path,
            LinkOption option,
            Instant instant
    ) throws IOException {
        delegate.setLastModifiedTime(path, option, instant);
    }

    @Override
    public Stat stat(Path path, LinkOption option) throws IOException {
        return delegate.stat(path, option);
    }

    @Override
    public Path createDir(Path path) throws IOException {
        return delegate.createDir(path);
    }

    @Override
    public Path createDir(Path path, Set<Permission> permissions)
            throws IOException {
        return delegate.createDir(path, permissions);
    }

    @Override
    public Path createFile(Path path) throws IOException {
        return delegate.createFile(path);
    }

    @Override
    public Path createSymbolicLink(Path link, Path target) throws IOException {
        return delegate.createSymbolicLink(link, target);
    }

    @Override
    public Path readSymbolicLink(Path path) throws IOException {
        return delegate.readSymbolicLink(path);
    }

    @Override
    public void move(Path src, Path dst) throws IOException {
        delegate.move(src, dst);
    }

    @Override
    public void delete(Path path) throws IOException {
        delegate.delete(path);
    }

    @Override
    public boolean exists(Path path, LinkOption option) throws IOException {
        return delegate.exists(path, option);
    }

    @Override
    public boolean isReadable(Path path) throws IOException {
        return delegate.isReadable(path);
    }

    @Override
    public boolean isWritable(Path path) throws IOException {
        return delegate.isWritable(path);
    }

    @Override
    public boolean isExecutable(Path path) throws IOException {
        return delegate.isExecutable(path);
    }

    @Override
    public Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> childrenConsumer,
            @Nullable String logTag,
            int watchLimit
    ) throws IOException, InterruptedException {

        return delegate.observe(
                path,
                option,
                observer,
                childrenConsumer,
                logTag,
                watchLimit
        );
    }

    @Override
    public void list(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer
    ) throws IOException {
        delegate.list(path, option, consumer);
    }

    @Override
    public InputStream newInputStream(Path path) throws IOException {
        return delegate.newInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(Path path, boolean append)
            throws IOException {
        return delegate.newOutputStream(path, append);
    }
}
