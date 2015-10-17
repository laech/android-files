package l.files.fs.local;

import android.os.AsyncTask;
import android.os.Parcel;
import android.system.ErrnoException;
import android.system.Os;

import com.google.auto.value.AutoValue;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import l.files.fs.BaseFile;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.FileName;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.fs.Stream;
import l.files.fs.Visitor;

import static android.system.OsConstants.EACCES;
import static android.system.OsConstants.EEXIST;
import static android.system.OsConstants.O_CREAT;
import static android.system.OsConstants.O_EXCL;
import static android.system.OsConstants.O_RDWR;
import static android.system.OsConstants.R_OK;
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
import static android.system.OsConstants.W_OK;
import static android.system.OsConstants.X_OK;
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
import static l.files.fs.local.ErrnoExceptions.toIOException;

@AutoValue
public abstract class LocalFile extends BaseFile {

    private static final int[] PERMISSION_BITS = permissionsToBits();

    private static int[] permissionsToBits() {
        int[] bits = new int[9];
        bits[OWNER_READ.ordinal()] = S_IRUSR;
        bits[OWNER_WRITE.ordinal()] = S_IWUSR;
        bits[OWNER_EXECUTE.ordinal()] = S_IXUSR;
        bits[GROUP_READ.ordinal()] = S_IRGRP;
        bits[GROUP_WRITE.ordinal()] = S_IWGRP;
        bits[GROUP_EXECUTE.ordinal()] = S_IXGRP;
        bits[OTHERS_READ.ordinal()] = S_IROTH;
        bits[OTHERS_WRITE.ordinal()] = S_IWOTH;
        bits[OTHERS_EXECUTE.ordinal()] = S_IXOTH;
        return bits;
    }

    public static Set<Permission> permissionsFromMode(int mode) {
        Set<Permission> permissions = new HashSet<>(9);
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

    private FileName name;

    LocalFile() {
    }

    static {
        Native.load();
        initTika();
    }

    private static void initTika() {
        // TODO do this cleaner
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                TikaHolder.tika.detect("");
            }
        });
    }

    abstract java.io.File file();

    public static LocalFile of(String path) {
        return of(new java.io.File(path));
    }

    public static LocalFile of(java.io.File file) {
        return new AutoValue_LocalFile(file);
    }

    @Deprecated
    public static LocalFile create(java.io.File file) {
        return new AutoValue_LocalFile(file);
    }

    private static void ensureIsLocal(File file) {
        if (!(file instanceof LocalFile)) {
            throw new IllegalArgumentException(file.toString());
        }
    }

    @Override
    public String scheme() {
        return "file";
    }

    @Override
    public String path() {
        return file().getPath();
    }

    @Override
    public URI uri() {
        return file().toURI();
    }

    @Override
    public FileName name() {
        if (name == null) {
            name = FileName.of(file().getName());
        }
        return name;
    }

    @Override
    public boolean isHidden() {
        return name().length() > 0 && name().charAt(0) == '.';
    }

    @Override
    public File root() {
        return of(new java.io.File("/"));
    }

    @Override
    public LocalFile parent() {
        java.io.File parent = file().getParentFile();
        if (parent == null) {
            return null;
        }
        return new AutoValue_LocalFile(parent);
    }

    @Override
    public Observation observe(
            LinkOption option,
            Observer observer,
            FileConsumer childrenConsumer) throws IOException, InterruptedException {

        LocalObservable observable = new LocalObservable(this, observer);
        observable.start(option, childrenConsumer);
        return observable;
    }

    @Override
    public boolean pathStartsWith(File that) {
        ensureIsLocal(that);
        if (that.parent() == null || that.equals(this)) {
            return true;
        }

        String thisPath = path();
        String thatPath = that.path();
        return thisPath.startsWith(thatPath) &&
                thisPath.charAt(thatPath.length()) == '/';
    }

    @Override
    public LocalFile resolve(String other) {
        return of(new java.io.File(file(), other));
    }

    @Override
    public LocalFile resolveParent(File fromParent, File toParent) {
        ensureIsLocal(fromParent);
        ensureIsLocal(toParent);
        if (!pathStartsWith(fromParent)) {
            throw new IllegalArgumentException();
        }
        java.io.File parent = ((LocalFile) toParent).file();
        String child = path().substring(fromParent.path().length());
        return new AutoValue_LocalFile(new java.io.File(parent, child));
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
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isReadable() throws IOException {
        return accessible(R_OK);
    }

    @Override
    public boolean isWritable() throws IOException {
        return accessible(W_OK);
    }

    @Override
    public boolean isExecutable() throws IOException {
        return accessible(X_OK);
    }

    private boolean accessible(int mode) throws IOException {
        try {
            Os.access(path(), mode);
            return true;
        } catch (ErrnoException e) {
            if (e.errno == EACCES) {
                return false;
            }
            throw toIOException(e, path());
        }
    }

    @Override
    public void traverse(LinkOption option, Visitor visitor) throws IOException {
        new LocalTraverser(this, option, visitor).traverse();
    }

    @Override
    public Stream<File> list(LinkOption option) throws IOException {
        return list(option, false);
    }

    @Override
    public Stream<File> listDirs(LinkOption option) throws IOException {
        return list(option, true);
    }

    private Stream<File> list(
            final LinkOption option,
            final boolean dirOnly) throws IOException {

        final Stream<Dirent> stream = Dirent.stream(LocalFile.this, option, dirOnly);
        return new Stream<File>() {

            @Override
            public void close() throws IOException {
                stream.close();
            }

            @Override
            public Iterator<File> iterator() {
                final Iterator<Dirent> iterator = stream.iterator();
                return new Iterator<File>() {

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public File next() {
                        return resolve(iterator.next().name());
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }

                };
            }
        };
    }

    @Override
    public InputStream newInputStream() throws IOException {
        return new FileInputStream(file());
    }

    @Override
    public OutputStream newOutputStream(boolean append) throws IOException {
        return new FileOutputStream(file(), append);
    }

    @Override
    public LocalFile createDir() throws IOException {
        try {
            mkdir();
        } catch (ErrnoException e) {
            throw toIOException(e, path());
        }
        return this;
    }

    private void mkdir() throws ErrnoException {
        // Same permission bits as java.io.File.mkdir() on Android
        Os.mkdir(path(), S_IRWXU);
    }

    @Override
    public LocalFile createDirs() throws IOException {
        try {
            if (stat(NOFOLLOW).isDirectory()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
            // Ignore will create
        }

        File parent = parent();
        if (parent != null) {
            parent.createDirs();
        }

        try {
            mkdir();
        } catch (ErrnoException e) {
            if (e.errno != EEXIST) {
                throw toIOException(e, path());
            }
        }

        return this;
    }

    @Override
    public LocalFile createFile() throws IOException {
        try {
            createFileNative();
        } catch (ErrnoException e) {
            throw toIOException(e, path());
        }
        return this;
    }

    private void createFileNative() throws ErrnoException {
        // Same flags and mode as java.io.File.createNewFile() on Android
        int flags = O_RDWR | O_CREAT | O_EXCL;
        int mode = S_IRUSR | S_IWUSR;
        FileDescriptor fd = Os.open(path(), flags, mode);
        Os.close(fd);
    }

    @Override
    public File createFiles() throws IOException {
        try {
            if (stat(NOFOLLOW).isRegularFile()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
            // Ignore will create
        }

        File parent = parent();
        if (parent != null) {
            parent.createDirs();
        }

        try {
            createFileNative();
        } catch (ErrnoException e) {
            if (e.errno != EEXIST) {
                throw toIOException(e, path());
            }
        }

        return this;
    }

    @Override
    public LocalFile createLink(File target) throws IOException {
        ensureIsLocal(target);
        String targetPath = target.path();
        try {
            Os.symlink(targetPath, path());
        } catch (ErrnoException e) {
            throw toIOException(e, path(), targetPath);
        }
        return this;
    }

    @Override
    public LocalFile readLink() throws IOException {
        try {
            String link = Os.readlink(path());
            return of(link);
        } catch (ErrnoException e) {
            throw toIOException(e, path());
        }
    }

    @Override
    public void moveTo(File dst) throws IOException {
        ensureIsLocal(dst);
        String dstPath = dst.path();
        String srcPath = path();
        try {
            Os.rename(srcPath, dstPath);
        } catch (ErrnoException e) {
            throw toIOException(e, srcPath, dstPath);
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            Os.remove(path());
        } catch (ErrnoException e) {
            throw toIOException(e, path());
        }
    }

    @Override
    public void setLastAccessedTime(LinkOption option, Instant instant) throws IOException {
        requireNonNull(option, "option");
        requireNonNull(instant, "instant");
        try {
            long seconds = instant.seconds();
            int nanos = instant.nanos();
            boolean followLink = option == FOLLOW;
            setAccessTime(path(), seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw toIOException(e, path());
        }
    }

    private static native void setAccessTime(
            String path,
            long seconds,
            int nanos,
            boolean followLink) throws ErrnoException;

    @Override
    public void setLastModifiedTime(LinkOption option, Instant instant) throws IOException {

        requireNonNull(option, "option");
        requireNonNull(instant, "instant");
        try {
            long seconds = instant.seconds();
            int nanos = instant.nanos();
            boolean followLink = option == FOLLOW;
            setModificationTime(path(), seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw toIOException(e, path());
        }
    }

    private static native void setModificationTime(
            String path,
            long seconds,
            int nanos,
            boolean followLink) throws ErrnoException;

    @Override
    public void setPermissions(Set<Permission> permissions) throws IOException {
        int mode = 0;
        for (Permission permission : permissions) {
            mode |= PERMISSION_BITS[permission.ordinal()];
        }
        try {
            Os.chmod(path(), mode);
        } catch (ErrnoException e) {
            throw toIOException(e, path());
        }
    }

    @Override
    public String detectBasicMediaType(l.files.fs.Stat stat) throws IOException {
        return BasicDetector.INSTANCE.detect(this, stat);
    }

    @Override
    public String detectContentMediaType(Stat stat) throws IOException {
        return MagicDetector.INSTANCE.detect(this, stat);
    }

    @Override
    public String detectMediaType(Stat stat) throws IOException {
        return MetaMagicDetector.INSTANCE.detect(this, stat);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocalFile> CREATOR = new Creator<LocalFile>() {

        @Override
        public LocalFile createFromParcel(Parcel source) {
            return of(source.readString());
        }

        @Override
        public LocalFile[] newArray(int size) {
            return new LocalFile[size];
        }

    };

}
