package l.files.fs.local;

import android.os.Parcel;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
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

import static l.files.fs.LinkOption.FOLLOW;

@AutoValue
public abstract class LocalFile extends BaseFile {

    public static Set<Permission> permissionsFromMode(int mode) {
        return LocalFileSystem.permissionsFromMode(mode);
    }

    LocalFile() {
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
        return LocalFileSystem.INSTANCE.newInputStream(path());
    }

    @Override
    public OutputStream newOutputStream(boolean append) throws IOException {
        return LocalFileSystem.INSTANCE.newOutputStream(path(), append);
    }

    @Override
    public LocalFile createDir() throws IOException {
        LocalFileSystem.INSTANCE.createDir(path());
        return this;
    }

    @Override
    public LocalFile createFile() throws IOException {
        LocalFileSystem.INSTANCE.createFile(path());
        return this;
    }

    @Override
    public LocalFile createLink(File target) throws IOException {
        LocalFileSystem.INSTANCE.createLink(target.path(), path());
        return this;
    }

    @Override
    public LocalFile readLink() throws IOException {
        return of(LocalFileSystem.INSTANCE.readLink(path()));
    }

    @Override
    public void moveTo(File dst) throws IOException {
        LocalFileSystem.INSTANCE.move(path(), dst.path());
    }

    @Override
    public void delete() throws IOException {
        LocalFileSystem.INSTANCE.delete(path());
    }

    @Override
    public void setLastModifiedTime(LinkOption option, Instant instant) throws IOException {
        LocalFileSystem.INSTANCE.setLastModifiedTime(path(), option, instant);
    }

    @Override
    public void setPermissions(Set<Permission> permissions) throws IOException {
        LocalFileSystem.INSTANCE.setPermissions(path(), permissions);
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
