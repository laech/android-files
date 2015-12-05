package l.files.fs.local;

import android.os.Parcel;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.BaseFile;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Permission;

import static java.util.Collections.unmodifiableSet;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.Permission.GROUP_EXECUTE;
import static l.files.fs.Permission.GROUP_READ;
import static l.files.fs.Permission.GROUP_WRITE;
import static l.files.fs.Permission.OTHERS_EXECUTE;
import static l.files.fs.Permission.OTHERS_READ;
import static l.files.fs.Permission.OTHERS_WRITE;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.local.ErrnoException.EAGAIN;
import static l.files.fs.local.Fcntl.O_CREAT;
import static l.files.fs.local.Fcntl.O_EXCL;
import static l.files.fs.local.Fcntl.O_RDWR;
import static l.files.fs.local.Fcntl.open;
import static l.files.fs.local.Stat.S_IRGRP;
import static l.files.fs.local.Stat.S_IROTH;
import static l.files.fs.local.Stat.S_IRUSR;
import static l.files.fs.local.Stat.S_IRWXU;
import static l.files.fs.local.Stat.S_IWGRP;
import static l.files.fs.local.Stat.S_IWOTH;
import static l.files.fs.local.Stat.S_IWUSR;
import static l.files.fs.local.Stat.S_IXGRP;
import static l.files.fs.local.Stat.S_IXOTH;
import static l.files.fs.local.Stat.S_IXUSR;
import static l.files.fs.local.Stat.chmod;
import static l.files.fs.local.Stat.mkdir;
import static l.files.fs.local.Unistd.readlink;
import static l.files.fs.local.Unistd.symlink;

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

    LocalFile() {
    }

    static {
        Native.load();
    }

    @Override
    public abstract LocalPath path();

    public static LocalFile of(java.io.File file) {
        return of(file.getPath());
    }

    public static LocalFile of(String path) {
        return of(LocalPath.of(path.getBytes(UTF_8)));
    }

    public static LocalFile of(LocalPath path) {
        return new AutoValue_LocalFile(path);
    }

    public URI uri() {
        return new java.io.File(path().toString()).toURI();
    }

    @Override
    public LocalName name() {
        return path().name();
    }

    @Override
    public boolean isHidden() {
        return path().isHidden();
    }

    @Override
    public LocalFile parent() {
        LocalPath parent = path().parent();
        if (parent == null) {
            return null;
        }
        return of(parent);
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
    public LocalFile resolve(String other) {
        return of(path().resolve(other.getBytes(UTF_8)));
    }

    @Override
    public LocalFile resolve(Name other) {
        return of(path().resolve(((LocalName) other).bytes()));
    }

    public LocalFile resolve(byte[] other) {
        return of(path().resolve(other));
    }

    @Override
    public LocalFile rebase(File fromParent, File toParent) {
        LocalPath src = (LocalPath) fromParent.path();
        LocalPath dst = (LocalPath) toParent.path();
        return of(path().rebase(src, dst));
    }

    @Override
    public Stat stat(LinkOption option) throws IOException {
        return LocalFileSystem.INSTANCE.stat(path(), option);
    }

    @Override
    public boolean exists(LinkOption option) throws IOException {
        return LocalFileSystem.INSTANCE.exists(path(), option);
    }

    @Override
    public boolean isReadable() throws IOException {
        return LocalFileSystem.INSTANCE.isReadable(path());
    }

    @Override
    public boolean isWritable() throws IOException {
        return LocalFileSystem.INSTANCE.isWritable(path());
    }

    @Override
    public boolean isExecutable() throws IOException {
        return LocalFileSystem.INSTANCE.isExecutable(path());
    }

    @Override
    public <C extends Collection<? super File>> C list(
            final LinkOption option,
            final C collection) throws IOException {

        list(option, false, new Consumer<RuntimeException>() {
            @Override
            public boolean accept(File file) {
                collection.add(file);
                return true;
            }
        });
        return collection;
    }

    @Override
    public <C extends Collection<? super File>> C listDirs(
            final LinkOption option,
            final C collection) throws IOException {

        list(option, true, new Consumer<RuntimeException>() {
            @Override
            public boolean accept(File file) {
                collection.add(file);
                return true;
            }
        });
        return collection;
    }

    @Override
    public <E extends Throwable> void list(
            LinkOption option,
            Consumer<E> consumer) throws IOException, E {

        list(option, false, consumer);
    }

    @Override
    public <E extends Throwable> void listDirs(
            LinkOption option,
            Consumer<E> consumer) throws IOException, E {

        list(option, true, consumer);
    }

    private <E extends Throwable> void list(
            final LinkOption option,
            final boolean dirOnly,
            final Consumer<E> consumer) throws IOException, E {

        try {
            Dirent.list(path().toByteArray(), option == FOLLOW, new Dirent.Callback<E>() {

                @Override
                public boolean onNext(byte[] nameBuffer, int nameLength, boolean isDirectory) throws E {
                    if (dirOnly && !isDirectory) {
                        return true;
                    }
                    byte[] name = Arrays.copyOf(nameBuffer, nameLength);
                    return consumer.accept(resolve(name));
                }

            });
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
    }

    @Override
    public InputStream newInputStream() throws IOException {
        return LocalStreams.newInputStream(this);
    }

    @Override
    public OutputStream newOutputStream(boolean append) throws IOException {
        return LocalStreams.newOutputStream(this, append);
    }

    @Override
    public LocalFile createDir() throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            mkdir(path().toByteArray(), S_IRWXU);
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
        return this;
    }

    @Override
    public LocalFile createFile() throws IOException {
        try {
            createFileNative();
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
        return this;
    }

    private void createFileNative() throws ErrnoException {
        // Same flags and mode as java.io.File.createNewFile() on Android
        int flags = O_RDWR | O_CREAT | O_EXCL;
        int mode = S_IRUSR | S_IWUSR;
        int fd = open(path().toByteArray(), flags, mode);
        Unistd.close(fd);
    }

    @Override
    public LocalFile createLink(File target) throws IOException {
        LocalPath targetPath = (LocalPath) target.path();
        LocalPath linkPath = path();
        try {
            symlink(targetPath.toByteArray(), linkPath.toByteArray());
        } catch (ErrnoException e) {
            throw e.toIOException(path(), targetPath);
        }
        return this;
    }

    @Override
    public LocalFile readLink() throws IOException {
        try {
            byte[] link = readlink(path().toByteArray());
            return of(LocalPath.of(link));
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
    }

    @Override
    public void moveTo(File dst) throws IOException {
        LocalPath dstPath = (LocalPath) dst.path();
        LocalPath srcPath = path();
        try {
            Stdio.rename(srcPath.toByteArray(), dstPath.toByteArray());
        } catch (ErrnoException e) {
            throw e.toIOException(srcPath, dstPath);
        }
    }

    @Override
    public void delete() throws IOException {
        while (true) {
            try {
                Stdio.remove(path().toByteArray());
                break;
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e.toIOException(path());
                }
            }
        }
    }

    @Override
    public void setLastModifiedTime(LinkOption option, Instant instant) throws IOException {

        requireNonNull(option, "option");
        requireNonNull(instant, "instant");
        try {
            long seconds = instant.seconds();
            int nanos = instant.nanos();
            boolean followLink = option == FOLLOW;
            setModificationTime(path().toByteArray(), seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
    }

    private static native void setModificationTime(
            byte[] path,
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
            chmod(path().toByteArray(), mode);
        } catch (ErrnoException e) {
            throw e.toIOException(path());
        }
    }

    @Override
    public String toString() {
        return path().toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(path(), 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocalFile> CREATOR = new Creator<LocalFile>() {

        @Override
        public LocalFile createFromParcel(Parcel source) {
            LocalPath path = source.readParcelable(LocalPath.class.getClassLoader());
            return of(path);
        }

        @Override
        public LocalFile[] newArray(int size) {
            return new LocalFile[size];
        }

    };

}
