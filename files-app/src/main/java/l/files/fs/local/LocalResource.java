package l.files.fs.local;

import android.system.Os;

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

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;
import l.files.fs.WatchService;

import static android.system.OsConstants.S_IRWXU;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

@AutoParcel
public abstract class LocalResource implements Resource {

    LocalResource() {
    }

    abstract File getFile();

    public static LocalResource create(File file) {
        return new AutoParcel_LocalResource(new File(sanitizedUri(file)));
    }

    private static URI sanitizedUri(File file) {
        /*
         * Don't return File.toURI as it will append a "/" to the end of the URI
         * depending on whether or not the file is a directory, that means two
         * calls to the method before and after the directory is deleted will
         * create two URIs that are not equal.
         */
        URI uri = file.toURI().normalize();
        String uriStr = uri.toString();
        if (!"/".equals(uri.getRawPath()) && uriStr.endsWith("/")) {
            return URI.create(uriStr.substring(0, uriStr.length() - 1));
        }
        return uri;
    }

    private String getFilePath() {
        return getFile().getPath();
    }

    @Override
    public String toString() {
        return getUri().toString();
    }

    @Override
    public URI getUri() {
        return sanitizedUri(getFile());
    }

    @Override
    public String getName() {
        return getFile().getName();
    }

    @Override
    public boolean isHidden() {
        return getFile().isHidden();
    }

    @Nullable
    @Override
    public LocalResource getParent() {
        if ("/".equals(getFilePath())) {
            return null;
        } else {
            return new AutoParcel_LocalResource(getFile().getParentFile());
        }
    }

    @Override
    public WatchService getWatcher() {
        return LocalWatchService.get();
    }

    @Override
    public boolean startsWith(Resource other) {
        if (other.getParent() == null || other.equals(this)) {
            return true;
        }
        if (other instanceof LocalResource) {
            String thisPath = getFilePath();
            String thatPath = ((LocalResource) other).getFilePath();
            return thisPath.startsWith(thatPath) &&
                    thisPath.charAt(thatPath.length()) == '/';
        }
        return false;
    }

    @Override
    public LocalResource resolve(String other) {
        return create(new File(getFile(), other));
    }


    @Override
    public LocalResource resolveParent(Resource fromParent, Resource toParent) {
        checkArgument(startsWith(fromParent));
        File parent = ((LocalResource) toParent).getFile();
        String child = getFilePath().substring(((LocalResource) fromParent).getFilePath().length());
        return new AutoParcel_LocalResource(new File(parent, child));
    }

    @Override
    public LocalResourceStatus readStatus(boolean followLink) throws IOException {
        return LocalResourceStatus.stat(this, followLink);
    }

    @Override
    public boolean exists() {
        try {
            Unistd.access(getFilePath(), Unistd.F_OK);
            return true;
        } catch (ErrnoException e) {
            return false;
        }
    }

    @Override
    public Stream traverse(
            TraversalOrder order,
            TraversalExceptionHandler handler) throws IOException {

        LocalPathEntry root = LocalPathEntry.stat(getFile());

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

        final Iterable<Resource> resources = Iterables.transform(iterable, new Function<LocalPathEntry, Resource>() {
            @Override
            public Resource apply(LocalPathEntry input) {
                return input.getResource();
            }
        });

        return new Stream() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public Iterator<Resource> iterator() {
                return resources.iterator();
            }
        };
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
                try (LocalResourceStream steam = LocalResourceStream.open(root.getResource())) {
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
    public Stream openDirectory() throws IOException {
        final LocalResourceStream stream = LocalResourceStream.open(this);
        return new Stream() {
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
        return new FileInputStream(getFile());
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream(getFile());
    }

    @Override
    public void createDirectory() throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            Os.mkdir(getFilePath(), S_IRWXU);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, getFilePath());
        }
    }

    @Override
    public void createDirectories() throws IOException {
        if (readStatus(false).isDirectory()) {
            return;
        }
        LocalResource parent = getParent();
        assert parent != null;
        parent.createDirectories();
        createDirectory();
    }

    @Override
    public void createFile() throws IOException {
        if (!getFile().createNewFile()) {
            throw new IOException(); // TODO use native code to get errno
        }
    }

    @Override
    public void createSymbolicLink(Resource target) throws IOException {
        try {
            Unistd.symlink(((LocalResource) target).getFilePath(), getFilePath());
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public Resource readSymbolicLink() throws IOException {
        try {
            String link = Unistd.readlink(getFilePath());
            return create(new File(link));
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public void move(Resource dst) throws IOException {
        String dstPath = ((LocalResource) dst).getFilePath();
        try {
            Stdio.rename(getFilePath(), dstPath);
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            Stdio.remove(getFilePath());
        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    @Override
    public void setLastModifiedTime(long time) throws IOException {
        if (!getFile().setLastModified(time)) {
            throw new IOException(); // TODO use native code to get errno
        }
    }

    @Override
    public MediaType detectMediaType() throws IOException {
        return MagicFileTypeDetector.INSTANCE.detect(this);
    }

}
