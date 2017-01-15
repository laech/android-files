package l.files.fs.local;

import android.os.Parcel;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
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

public abstract class LocalPath extends Path {

    public static final Creator<LocalPath> CREATOR = new Creator<LocalPath>() {

        @Override
        public LocalPath createFromParcel(Parcel source) {
            return LocalPath.fromByteArray(source.createByteArray());
        }

        @Override
        public LocalPath[] newArray(int size) {
            return new LocalPath[size];
        }
    };

    private static final LocalFileSystem fs = LocalFileSystem.INSTANCE;

    static final Charset ENCODING =
            Charset.forName(System.getProperty("sun.jnu.encoding"));

    public static LocalPath fromFile(File file) {
        return fromString(file.getPath());
    }

    public static LocalPath fromString(String path) {
        return fromByteArray(path.getBytes(ENCODING));
    }

    public static LocalPath fromByteArray(byte[] path) {
        RelativePath result = new RelativePath(getNames(path));
        boolean absolute = path.length > 0 && path[0] == '/';
        return absolute ? new AbsolutePath(result) : result;
    }

    private static ImmutableList<Name> getNames(byte[] path) {
        ImmutableList.Builder<Name> names = ImmutableList.builder();
        for (int start = 0, end; start < path.length; start = end + 1) {
            end = ArrayUtils.indexOf(path, (byte) '/', start);
            if (end == -1) {
                end = path.length;
            }
            if (end > start) {
                names.add(new LocalName(path, start, end));
            }
        }
        return names.build();
    }

    @Override
    public final byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toByteArray(out);
        return out.toByteArray();
    }

    abstract void toByteArray(ByteArrayOutputStream out);

    @Override
    public final String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toByteArray(out);
        try {
            return out.toString(ENCODING.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public URI toUri() {
        return new File(toString()).toURI();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

    public final LocalPath concat(Name name) {
        return concat(name.toPath());
    }

    public final LocalPath concat(String path) {
        return concat(fromString(path));
    }

    public final LocalPath concat(byte[] path) {
        return concat(fromByteArray(path));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(toByteArray());
    }

    @Override
    public void setPermissions(Set<Permission> permissions)
            throws IOException {
        fs.setPermissions(this, permissions);
    }

    @Override
    public void setLastModifiedTime(LinkOption option, Instant instant)
            throws IOException {
        fs.setLastModifiedTime(this, option, instant);
    }

    @Override
    public Stat stat(LinkOption option) throws IOException {
        return fs.stat(this, option);
    }

    @Override
    public Path createDir() throws IOException {
        fs.createDir(this);
        return this;
    }

    @Override
    public Path createDir(Set<Permission> permissions) throws IOException {
        fs.createDir(this, permissions);
        return this;
    }

    @Override
    public Path createFile() throws IOException {
        fs.createFile(this);
        return this;
    }

    @Override
    public Path createSymbolicLink(Path target) throws IOException {
        // TODO verify target is LocalPath?
        fs.createSymbolicLink(this, target);
        return this;
    }

    @Override
    public Path readSymbolicLink() throws IOException {
        return fs.readSymbolicLink(this);
    }

    @Override
    public void move(Path destination) throws IOException {
        fs.move(this, destination); // TODO verify destination is LocalPath?
    }

    @Override
    public void delete() throws IOException {
        fs.delete(this);
    }

    @Override
    public boolean exists(LinkOption option) throws IOException {
        return fs.exists(this, option);
    }

    @Override
    public boolean isReadable() throws IOException {
        return fs.isReadable(this);
    }

    @Override
    public boolean isWritable() throws IOException {
        return fs.isWritable(this);
    }

    @Override
    public boolean isExecutable() throws IOException {
        return fs.isExecutable(this);
    }

    @Override
    public Observation observe(
            LinkOption option,
            Observer observer,
            Consumer childrenConsumer,
            @Nullable String logTag,
            int watchLimit
    ) throws IOException, InterruptedException {

        return fs.observe(
                this,
                option,
                observer,
                childrenConsumer,
                logTag,
                watchLimit
        );
    }

    @Override
    public void list(LinkOption option, Consumer consumer) throws IOException {
        fs.list(this, option, consumer);
    }

    @Override
    public InputStream newInputStream() throws IOException {
        return fs.newInputStream(this);
    }

    @Override
    public OutputStream newOutputStream(boolean append) throws IOException {
        return fs.newOutputStream(this, append);
    }

}
