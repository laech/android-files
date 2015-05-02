package l.files.fs.local;

import android.system.Os;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.ExistsException;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.NotExistException;
import l.files.fs.NotFileException;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ExceptionHandler;
import l.files.fs.Visitor;
import l.files.fs.WatchEvent;

import static android.system.OsConstants.EACCES;
import static android.system.OsConstants.EISDIR;
import static android.system.OsConstants.O_APPEND;
import static android.system.OsConstants.O_CREAT;
import static android.system.OsConstants.O_EXCL;
import static android.system.OsConstants.O_NOFOLLOW;
import static android.system.OsConstants.O_RDONLY;
import static android.system.OsConstants.O_RDWR;
import static android.system.OsConstants.O_TRUNC;
import static android.system.OsConstants.O_WRONLY;
import static android.system.OsConstants.R_OK;
import static android.system.OsConstants.S_IRGRP;
import static android.system.OsConstants.S_IROTH;
import static android.system.OsConstants.S_IRUSR;
import static android.system.OsConstants.S_IRWXU;
import static android.system.OsConstants.S_ISDIR;
import static android.system.OsConstants.S_IWGRP;
import static android.system.OsConstants.S_IWOTH;
import static android.system.OsConstants.S_IWUSR;
import static android.system.OsConstants.S_IXGRP;
import static android.system.OsConstants.S_IXOTH;
import static android.system.OsConstants.S_IXUSR;
import static android.system.OsConstants.W_OK;
import static android.system.OsConstants.X_OK;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.GROUP_EXECUTE;
import static l.files.fs.Permission.GROUP_READ;
import static l.files.fs.Permission.GROUP_WRITE;
import static l.files.fs.Permission.OTHERS_EXECUTE;
import static l.files.fs.Permission.OTHERS_READ;
import static l.files.fs.Permission.OTHERS_WRITE;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

@AutoParcel
public abstract class LocalResource implements Resource {

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
        return unmodifiableSet(permissions);
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

    private static void checkLocalResource(Resource resource) {
        if (!(resource instanceof LocalResource)) {
            throw new IllegalArgumentException(resource.toString());
        }
    }

    @Override
    public String path() {
        return getFile().getPath();
    }

    @Override
    public String toString() {
        return uri().toString();
    }

    @Override
    public URI uri() {
        return sanitizedUri(getFile());
    }

    @Override
    public String name() {
        return getFile().getName();
    }

    @Override
    public boolean hidden() {
        return getFile().isHidden();
    }

    @Nullable
    @Override
    public LocalResource parent() {
        if ("/".equals(path())) {
            return null;
        } else {
            return new AutoParcel_LocalResource(getFile().getParentFile());
        }
    }

    @Override
    public List<Resource> hierarchy() {
        List<Resource> hierarchy = new ArrayList<>();
        for (Resource p = this; p != null; p = p.parent()) {
            hierarchy.add(p);
        }
        Collections.reverse(hierarchy);
        return hierarchy;
    }

    @Override
    public Closeable observe(LinkOption option, WatchEvent.Listener observer) throws IOException {
        return LocalResourceObservable.observe(this, option, observer);
    }

    @Override
    public boolean startsWith(Resource other) {
        if (other.parent() == null || other.equals(this)) {
            return true;
        }
        if (other instanceof LocalResource) {
            String thisPath = path();
            String thatPath = other.path();
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
        checkLocalResource(fromParent);
        checkLocalResource(toParent);
        checkArgument(startsWith(fromParent));
        File parent = ((LocalResource) toParent).getFile();
        String child = path().substring(fromParent.path().length());
        return new AutoParcel_LocalResource(new File(parent, child));
    }

    @Override
    public LocalStat stat(LinkOption option) throws IOException {
        return LocalStat.stat(this, option);
    }

    @Override
    public boolean exists(LinkOption option) throws IOException {
        requireNonNull(option, "option");
        try {
            // access() follows symbolic links
            // faccessat(AT_SYMLINK_NOFOLLOW) doesn't work on android
            // so use stat here
            stat(option);
            return true;
        } catch (NotExistException e) {
            return false;
        }
    }

    @Override
    public boolean readable() throws IOException {
        return access(R_OK);
    }

    @Override
    public boolean writable() throws IOException {
        return access(W_OK);
    }

    @Override
    public boolean executable() throws IOException {
        return access(X_OK);
    }

    private boolean access(int mode) throws IOException {
        try {
            Os.access(path(), mode);
            return true;
        } catch (android.system.ErrnoException e) {
            if (e.errno == EACCES) {
                return false;
            }
            throw ErrnoException.toIOException(e, path());
        }
    }

    @Override
    public void traverse(LinkOption option,
                         @Nullable Visitor pre,
                         @Nullable Visitor post) throws IOException {
        traverse(option, pre, post, new ExceptionHandler()
        {
            @Override
            public void handle(Resource resource, IOException e)
                    throws IOException
            {
                throw e;
            }
        });
    }

    @Override
    public void traverse(LinkOption option,
                         @Nullable Visitor pre,
                         @Nullable Visitor post,
                         @Nullable ExceptionHandler handler)
            throws IOException {
        new LocalResourceTraverser(this, option, pre, post, handler).traverse();
    }

    @Override
    public void list(LinkOption option, final Visitor visitor)
            throws IOException {
        LocalResourceStream.list(this, option, new LocalResourceStream.Callback()
        {
            @Override
            public boolean accept(long inode, String name, boolean directory)
                    throws IOException
            {
                return visitor.accept(resolve(name)) != TERMINATE;
            }
        });
    }

    @Override
    public <T extends Collection<? super Resource>> T list(
            final LinkOption option,
            final T collection) throws IOException {
        list(option, new Visitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                collection.add(resource);
                return CONTINUE;
            }
        });
        return collection;
    }

    @Override
    public List<Resource> list(LinkOption option) throws IOException {
        return list(option, new ArrayList<Resource>());
    }

    @Override
    public InputStream input(LinkOption option) throws IOException {
        requireNonNull(option, "option");

        int flags = O_RDONLY;
        if (option == NOFOLLOW) {
            flags |= O_NOFOLLOW;
        }

        try {

            FileDescriptor fd = Os.open(path(), flags, 0);
            // Above call allows opening directories, this check ensure this is
            // not a directory
            try {
                if (S_ISDIR(Os.fstat(fd).st_mode)) {
                    throw new NotFileException(path());
                }
            } catch (Throwable e) {
                Os.close(fd);
                throw e;
            }
            return new FileInputStream(fd);

        } catch (android.system.ErrnoException e) {
            if (ErrnoException.isCausedByNoFollowLink(e, this)) {
                throw new NotFileException(path(), e);
            }
            throw ErrnoException.toIOException(e, path());
        }
    }

    @Override
    public OutputStream output(LinkOption option) throws IOException {
        return output(option, false);
    }

    @Override
    public OutputStream output(LinkOption option, boolean append)
            throws IOException {
        requireNonNull(option, "option");

        // Same default flags and mode as FileOutputStream
        int mode = S_IRUSR | S_IWUSR;
        int flags = O_WRONLY | O_CREAT | (append ? O_APPEND : O_TRUNC);
        if (option == NOFOLLOW) {
            flags |= O_NOFOLLOW;
        }
        try {
            return new FileOutputStream(Os.open(path(), flags, mode));
        } catch (android.system.ErrnoException e) {
            if (ErrnoException.isCausedByNoFollowLink(e, this)) {
                throw new NotFileException(path(), e);
            }
            if (e.errno == EISDIR) {
                throw new NotFileException(path(), e);
            }
            throw ErrnoException.toIOException(e, path());
        }
    }

    @Override
    public Reader reader(LinkOption option, Charset charset) throws IOException {
        return new InputStreamReader(input(option), charset);
    }

    @Override
    public Writer writer(LinkOption option, Charset charset) throws IOException {
        return writer(option, charset, false);
    }

    @Override
    public Writer writer(LinkOption option, Charset charset, boolean append) throws IOException {
        return new OutputStreamWriter(output(option, append), charset);
    }

    @Override
    public LocalResource createDirectory() throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            Os.mkdir(path(), S_IRWXU);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, path());
        }
        return this;
    }

    @Override
    public LocalResource createDirectories() throws IOException {
        try {
            if (stat(NOFOLLOW).isDirectory()) {
                return this;
            }
        } catch (NotExistException ignore) {
            // Ignore will create
        }
        Resource parent = parent();
        if (parent != null) {
            parent.createDirectories();
        }
        createDirectory();
        return this;
    }

    @Override
    public LocalResource createFile() throws IOException {
        // Same flags and mode as java.io.File.createNewFile() on Android
        int flags = O_RDWR | O_CREAT | O_EXCL;
        int mode = S_IRUSR | S_IWUSR;
        try {
            FileDescriptor fd = Os.open(path(), flags, mode);
            Os.close(fd);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, path());
        }
        return this;
    }

    @Override
    public Resource createFiles() throws IOException {
        Resource parent = parent();
        if (parent != null) {
            parent.createDirectories();
        }
        return createFile();
    }

    @Override
    public LocalResource createLink(Resource target) throws IOException {
        checkLocalResource(target);
        String targetPath = target.path();
        try {
            Os.symlink(targetPath, path());
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, path(), targetPath);
        }
        return this;
    }

    @Override
    public LocalResource readLink() throws IOException {
        try {
            String link = Os.readlink(path());
            return create(new File(link));
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, path());
        }
    }

    @Override
    public void moveTo(Resource dst) throws IOException {
        checkLocalResource(dst);

        String dstPath = dst.path();
        String srcPath = path();
        try {
            // renameat2() not available on Android
            dst.stat(NOFOLLOW);
            throw new ExistsException(dstPath);
        } catch (NotExistException e) {
            // Okay
        }
        try {
            Os.rename(srcPath, dstPath);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, srcPath, dstPath);
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            Os.remove(path());
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, path());
        }
    }

    @Override
    public void setAccessTime(LinkOption option, Instant instant)
            throws IOException {
        requireNonNull(option, "option");
        requireNonNull(instant, "instant");
        try {
            long seconds = instant.getSeconds();
            int nanos = instant.getNanos();
            boolean followLink = option == FOLLOW;
            setAccessTime(path(), seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
    }

    private static native void setAccessTime(
            String path, long seconds, int nanos, boolean followLink
    ) throws ErrnoException;

    @Override
    public void setModificationTime(
            LinkOption option,
            Instant instant) throws IOException {

        requireNonNull(option, "option");
        requireNonNull(instant, "instant");
        try {
            long seconds = instant.getSeconds();
            int nanos = instant.getNanos();
            boolean followLink = option == FOLLOW;
            setModificationTime(path(), seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
    }

    private static native void setModificationTime(
            String path, long seconds, int nanos, boolean followLink
    ) throws ErrnoException;

    @Override
    public void setPermissions(Set<Permission> permissions) throws IOException {
        int mode = 0;
        for (Permission permission : permissions) {
            mode |= permissionBits.get(permission);
        }
        try {
            /* To change permission on the link itself instead of the target:
             *  - fchmodat(AT_FDCWD, path, mode, AT_SYMLINK_NOFOLLOW)
             * but not implemented.
             */
            Os.chmod(path(), mode);
        } catch (android.system.ErrnoException e) {
            throw ErrnoException.toIOException(e, path());
        }
    }

    @Override
    public void removePermissions(Set<Permission> permissions) throws IOException {
        Set<Permission> existing = stat(FOLLOW).permissions();
        Set<Permission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        setPermissions(perms);
    }

    @Override
    public String readString(LinkOption option, Charset charset)
            throws IOException {
        return readString(option, charset, new StringBuilder()).toString();
    }

    @Override
    public <T extends Appendable> T readString(
            LinkOption option,
            Charset charset,
            T appendable) throws IOException {

        try (Reader reader = reader(option, charset)) {
            for (CharBuffer buffer = CharBuffer.allocate(8192);
                 reader.read(buffer) > -1; ) {
                buffer.flip();
                appendable.append(buffer);
            }
        }
        return appendable;
    }

    @Override
    public void writeString(
            LinkOption option,
            Charset charset,
            CharSequence content) throws IOException {
        try (Writer writer = writer(option, charset)) {
            writer.write(content.toString());
        }
    }

}
