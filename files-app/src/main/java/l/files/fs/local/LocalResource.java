package l.files.fs.local;

import android.system.Os;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.TreeTraverser;
import com.google.common.net.MediaType;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.Instant;
import l.files.fs.NotExistException;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;

import static android.system.OsConstants.O_CREAT;
import static android.system.OsConstants.O_EXCL;
import static android.system.OsConstants.O_RDWR;
import static android.system.OsConstants.S_IRGRP;
import static android.system.OsConstants.S_IROTH;
import static android.system.OsConstants.S_IRUSR;
import static android.system.OsConstants.S_IRWXU;
import static android.system.OsConstants.S_IWGRP;
import static android.system.OsConstants.S_IWOTH;
import static android.system.OsConstants.S_IWUSR;
import static android.system.OsConstants.S_IXGRP;
import static android.system.OsConstants.S_IXOTH;
import static android.system.OsConstants.S_IXUSR;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static l.files.fs.Permission.GROUP_EXECUTE;
import static l.files.fs.Permission.GROUP_READ;
import static l.files.fs.Permission.GROUP_WRITE;
import static l.files.fs.Permission.OTHERS_EXECUTE;
import static l.files.fs.Permission.OTHERS_READ;
import static l.files.fs.Permission.OTHERS_WRITE;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;

@AutoParcel
public abstract class LocalResource extends Native implements Resource {

    private static final Map<Permission, Integer> permissionBits
            = createPermissionBits();

    private static Map<Permission, Integer> createPermissionBits() {
        Map<Permission, Integer> bits = new HashMap<>();
        bits.put(OWNER_READ, S_IRUSR);
        bits.put(OWNER_WRITE, S_IWUSR);
        bits.put(OWNER_EXECUTE, S_IXUSR);
        bits.put(GROUP_READ, S_IRGRP);
        bits.put(GROUP_WRITE, S_IWGRP);
        bits.put(GROUP_EXECUTE, S_IXGRP);
        bits.put(OTHERS_READ, S_IROTH);
        bits.put(OTHERS_WRITE, S_IWOTH);
        bits.put(OTHERS_EXECUTE, S_IXOTH);
        return unmodifiableMap(bits);
    }

    static Set<Permission> mapPermissions(int mode) {
        Set<Permission> permissions = new HashSet<>();
        if ((mode & S_IRUSR) != 0) permissions.add(OWNER_READ);
        if ((mode & S_IWUSR) != 0) permissions.add(OWNER_WRITE);
        if ((mode & S_IXUSR) != 0) permissions.add(OWNER_EXECUTE);
        if ((mode & S_IRGRP) != 0) permissions.add(GROUP_READ);
        if ((mode & S_IWGRP) != 0) permissions.add(GROUP_WRITE);
        if ((mode & S_IXGRP) != 0) permissions.add(GROUP_EXECUTE);
        if ((mode & S_IROTH) != 0) permissions.add(OTHERS_READ);
        if ((mode & S_IWOTH) != 0) permissions.add(OTHERS_WRITE);
        if ((mode & S_IXOTH) != 0) permissions.add(OTHERS_EXECUTE);
        return permissions;
    }

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

    String getPath() {
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
        if ("/".equals(getPath())) {
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
    public Closeable observe(WatchEvent.Listener observer) throws IOException {
        return LocalResourceObservable.observe(this, observer);
    }

    @Override
    public boolean startsWith(Resource other) {
        if (other.getParent() == null || other.equals(this)) {
            return true;
        }
        if (other instanceof LocalResource) {
            String thisPath = getPath();
            String thatPath = ((LocalResource) other).getPath();
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
        String child = getPath().substring(((LocalResource) fromParent).getPath().length());
        return new AutoParcel_LocalResource(new File(parent, child));
    }

    @Override
    public LocalResourceStatus readStatus(boolean followLink) throws IOException {
        return LocalResourceStatus.stat(this, followLink);
    }

    @Override
    public boolean exists() {
        try {
            Unistd.access(getPath(), Unistd.F_OK);
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
    public OutputStream openOutputStream(boolean append) throws IOException {
        return new FileOutputStream(getFile(), append);
    }

    @Override
    public void createDirectory() throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            Os.mkdir(getPath(), S_IRWXU);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, getPath());
        }
    }

    @Override
    public void createDirectories() throws IOException {
        try {
            if (readStatus(false).isDirectory()) {
                return;
            }
        } catch (NotExistException ignore) {
            // Ignore will create
        }
        LocalResource parent = getParent();
        assert parent != null;
        parent.createDirectories();
        createDirectory();
    }

    @Override
    public void createFile() throws IOException {
        // Same flags and mode as java.io.File.createNewFile() on Android
        int flags = O_RDWR | O_CREAT | O_EXCL;
        int mode = S_IRUSR | S_IWUSR;
        try {
            FileDescriptor fd = Os.open(getPath(), flags, mode);
            Os.close(fd);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, getPath());
        }
    }

    @Override
    public void createSymbolicLink(Resource target) throws IOException {
        String targetPath = ((LocalResource) target).getPath();
        try {
            Unistd.symlink(targetPath, getPath());
        } catch (ErrnoException e) {
            throw e.toIOException(getPath(), targetPath);
        }
    }

    @Override
    public Resource readSymbolicLink() throws IOException {
        try {
            String link = Unistd.readlink(getPath());
            return create(new File(link));
        } catch (ErrnoException e) {
            throw e.toIOException(getPath());
        }
    }

    @Override
    public void renameTo(Resource dst) throws IOException {
        String srcPath = getPath();
        String dstPath = ((LocalResource) dst).getPath();
        try {
            Os.rename(srcPath, dstPath);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, srcPath, dstPath);
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            Stdio.remove(getPath());
        } catch (ErrnoException e) {
            throw e.toIOException(getPath());
        }
    }

    @Override
    public void setAccessTime(Instant instant) throws IOException {
        try {
            setAccessTime(getPath(), instant.getSeconds(), instant.getNanos());
        } catch (ErrnoException e) {
            throw e.toIOException(getPath());
        }
    }

    private static native void setAccessTime(String path, long seconds, int nanos)
            throws ErrnoException;

    @Override
    public void setModificationTime(Instant instant) throws IOException {
        try {
            setModificationTime(getPath(), instant.getSeconds(), instant.getNanos());
        } catch (ErrnoException e) {
            throw e.toIOException(getPath());
        }
    }

    private static native void setModificationTime(String path, long seconds, int nanos)
            throws ErrnoException;

    @Override
    public void setPermissions(Set<Permission> permissions) throws IOException {
        int mode = 0;
        for (Permission permission : permissions) {
            mode |= permissionBits.get(permission);
        }
        try {
            Os.chmod(getPath(), mode);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, getPath());
        }
    }

    @Override
    public MediaType detectMediaType() throws IOException {
        return MagicFileTypeDetector.INSTANCE.detect(this);
    }

}
