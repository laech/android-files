package l.files.fs.local;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.TreeTraverser;
import com.google.common.net.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import auto.parcel.AutoParcel;
import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.ResourceStream;
import l.files.fs.WatchService;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

@AutoParcel
public abstract class LocalResource implements Resource {

    LocalResource() {
    }

    @Override
    public abstract LocalPath getPath();

    public static LocalResource create(LocalPath path) {
        return new AutoParcel_LocalResource(path);
    }

    @Override
    public URI getUri() {
        return getPath().getUri();
    }

    @Override
    public String getName() {
        return getPath().getName();
    }

    @Override
    public LocalResource getResource() {
        return this;
    }

    @Override
    public WatchService getWatcher() {
        return LocalWatchService.get();
    }

    @Override
    public Resource resolve(String other) {
        return create(getPath().resolve(other));
    }

    @Override
    public LocalResourceStatus readStatus(boolean followLink) throws IOException {
        return LocalResourceStatus.stat(getPath(), followLink);
    }

    @Override
    public boolean exists() {
        try {
            Unistd.access(getPath().toString(), Unistd.F_OK);
            return true;
        } catch (ErrnoException e) {
            return false;
        }
    }

    @Override
    public Iterable<Resource> traverse(
            TraversalOrder order,
            TraversalExceptionHandler handler) throws IOException {

        LocalPathEntry root = LocalPathEntry.read(getPath());

        Iterable<LocalPathEntry> iterable;
        switch (order) {
            case BREATH_FIRST:
                iterable = new Traverser(handler).breadthFirstTraversal(root);
                break;
            case POST_ORDER:
                iterable = new Traverser(handler).postOrderTraversal(root);
                break;
            case PRE_ORDER:
                iterable = new Traverser(handler).preOrderTraversal(root);
                break;
            default:
                throw new AssertionError(order.name());
        }

        return Iterables.transform(iterable, new Function<LocalPathEntry, Resource>() {
            @Override
            public Resource apply(LocalPathEntry input) {
                return input.getResource();
            }
        });
    }

    private static final class Traverser extends TreeTraverser<LocalPathEntry> {

        private final TraversalExceptionHandler handler;

        Traverser(TraversalExceptionHandler handler) {
            this.handler = requireNonNull(handler, "handler");
        }

        @Override
        public Iterable<LocalPathEntry> children(LocalPathEntry root) {
            try {
                if (!root.isDirectory()) {
                    return emptyList();
                }
                try (LocalResourceStream steam = LocalResourceStream.open(root.getPath())) {
                    ArrayList<LocalPathEntry> children = new ArrayList<>();
                    for (LocalPathEntry entry : steam) {
                        children.add(entry);
                    }
                    children.trimToSize();
                    return unmodifiableList(children);
                }
            } catch (IOException e) {
                handler.handle(root.getResource(), e);
                return emptyList();
            }
        }
    }

    @Override
    public ResourceStream<Resource> openResourceStream() throws IOException {
        final LocalResourceStream stream = LocalResourceStream.open(getPath());
        return new ResourceStream<Resource>() {
            @Override
            public void close() throws IOException {
                stream.close();
            }

            @Override
            public Iterator<Resource> iterator() {
                return Iterators.transform(stream.iterator(), new Function<LocalPathEntry, Resource>() {
                    @Override
                    public Resource apply(LocalPathEntry input) {
                        return input.getResource();
                    }
                });
            }
        };
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new FileInputStream(getPath().toString());
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream(getPath().toString());
    }

    @Override
    public void createDirectory() throws IOException {
        createDirectory(getPath());
    }

    private void createDirectory(LocalPath path) throws IOException {
        if (!path.getParent().getResource().exists()) {
            createDirectory(path.getParent());
        }
        File f = new File(getPath().toString());
        if (!f.isDirectory() && !f.mkdir()) {
            throw new IOException(); // TODO use native code to get errno
        }
    }

    @Override
    public void createFile() throws IOException {
        if (!new File(getPath().toString()).createNewFile()) {
            throw new IOException(); // TODO use native code to get errno
        }
    }

    @Override
    public void createSymbolicLink(Resource target) throws IOException {
        createSymbolicLink(target.getPath());
    }

    @Override
    public void createSymbolicLink(Path target) throws IOException {
        LocalPath.check(target);
        try {
            Unistd.symlink(target.toString(), getPath().toString());
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public Resource readSymbolicLink() throws IOException {
        try {
            String link = Unistd.readlink(getPath().toString());
            return LocalResource.create(LocalPath.of(link));
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public void move(Path dst) throws IOException {
        LocalPath.check(dst);
        try {
            Stdio.rename(getPath().toString(), dst.toString());
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            Stdio.remove(getPath().toString());
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public void setLastModifiedTime(long time) throws IOException {
        if (!new File(getPath().toString()).setLastModified(time)) {
            throw new IOException(); // TODO use native code to get errno
        }
    }

    @Override
    public MediaType detectMediaType() throws IOException {
        return MagicFileTypeDetector.INSTANCE.detect(getPath());
    }

}
