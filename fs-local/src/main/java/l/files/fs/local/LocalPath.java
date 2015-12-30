package l.files.fs.local;

import android.os.Parcel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l.files.fs.FileSystem;
import l.files.fs.Name;
import l.files.fs.Path;

import static java.lang.System.arraycopy;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.Files.UTF_8;

final class LocalPath implements Path {

    static final byte DOT = 46; // '.' in UTF-8
    static final byte SEP = 47; // '/' in UTF-8

    static final byte[][] EMPTY_NAMES = new byte[0][];

    /*
     * Binary representation of this path, normally it's whatever
     * returned from the native calls, unless when it's created
     * from java.lang.String.
     *
     * Example:
     *
     *   /         -> []              absolute=true
     *   ""        -> []              absolute=false
     *   /dev/null -> [[dev],[null]]  absolute=true
     *   abc/hello -> [[abc],[hello]] absolute=false
     *
     * Using the original bytes from the OS means the path is
     * independent of charset encodings, avoiding byte loss when
     * converting from/to string, resulting certain files inaccessible.
     *
     * Currently, java.io.File suffers from the above issue, because
     * it stores the path as string internally. So it will fail to
     * handle certain files. For example, if a file whose binary file
     * name is [-19, -96, -67, -19, -80, -117], java.io.File.list on
     * the parent will return it, but any operation on that file will
     * fail.
     *
     * This class is free of the above issue.
     */
    private final byte[][] names;

    private final boolean absolute;

    private LocalPath(byte[][] names, boolean absolute) {
        this.names = requireNonNull(names);
        this.absolute = absolute;
    }

    public static LocalPath of(File file) {
        return of(file.getPath());
    }

    public static LocalPath of(String path) {
        return of(path.getBytes(UTF_8));
    }

    public static LocalPath of(byte[] path) {
        byte[][] names = toNames(path);
        boolean absolute = path.length > 0 && path[0] == SEP;
        return new LocalPath(names, absolute);
    }

    @Deprecated
    public byte[] bytes() {
        return toByteArray();
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toByteArray(out);
        return out.toByteArray();
    }

    @Override
    public int toByteArray(OutputStream out) throws IOException {

        int count = 0;
        if (absolute) {
            out.write(SEP);
            count++;
        }

        for (int i = 0; i < names.length; i++) {
            out.write(names[i]);
            count += names[i].length;

            if (i < names.length - 1) {
                out.write(SEP);
                count++;
            }
        }

        return count;
    }

    @Override
    public int toByteArray(ByteArrayOutputStream out) {
        try {
            return toByteArray((OutputStream) out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FileSystem fileSystem() {
        return LocalFileSystem.INSTANCE;
    }

    @Override
    public String toString() {
        return new String(toByteArray(), UTF_8);
    }

    @Override
    public URI toUri() {
        return new java.io.File(toString()).toURI();
    }

    @Override
    public int hashCode() {
        int result = Arrays.deepHashCode(names);
        return 31 * result + (absolute ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LocalPath)) {
            return false;
        }
        LocalPath that = (LocalPath) o;
        return absolute == that.absolute &&
                Arrays.deepEquals(names, that.names);

    }

    @Override
    public LocalPath resolve(String path) {
        return resolve(path.getBytes(UTF_8));
    }

    @Override
    public LocalPath resolve(Name name) {
        return resolve(((LocalName) name).bytes());
    }

    @Override
    public LocalPath resolve(byte[] path) {
        byte[][] children = toNames(path);
        if (children.length == 0) {
            return this;
        }
        byte[][] newNames = Arrays.copyOf(names, names.length + children.length);
        arraycopy(children, 0, newNames, names.length, children.length);
        return new LocalPath(newNames, absolute);
    }

    private static byte[][] toNames(byte[] path) {

        if (path.length == 0) {
            return EMPTY_NAMES;
        }

        int separatorCount = 0;
        for (byte b : path) {
            if (b == SEP) {
                separatorCount++;
            }
        }

        if (separatorCount == 0) {
            return new byte[][]{path};
        }

        List<byte[]> parts = new ArrayList<>(separatorCount + 1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte b : path) {
            if (b != SEP) {
                out.write(b);
            } else if (out.size() > 0) {
                parts.add(out.toByteArray());
                out.reset();
            }
        }
        if (out.size() > 0) {
            parts.add(out.toByteArray());
        }

        return parts.toArray(new byte[parts.size()][]);
    }

    @Override
    public LocalPath parent() {
        if (absolute && names.length == 1) {
            return new LocalPath(EMPTY_NAMES, true); // "/"
        } else if (names.length < 2) {
            return null;
        }
        byte[][] newNames = Arrays.copyOf(names, names.length - 1);
        return new LocalPath(newNames, absolute);
    }

    @Override
    public LocalName name() {
        if (names.length == 0) {
            return LocalName.of(new byte[0]);
        }
        return LocalName.of(names[names.length - 1]);
    }

    @Override
    public boolean isHidden() {
        return names.length > 0 &&
                names[names.length - 1][0] == DOT;
    }

    @Override
    public boolean startsWith(Path p) {

        byte[][] thatNames = ((LocalPath) p).names;
        byte[][] thisNames = names;

        if (thatNames.length > thisNames.length) {
            return false;
        }

        for (int i = 0; i < thatNames.length; i++) {
            if (!Arrays.equals(thisNames[i], thatNames[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public LocalPath rebase(Path src, Path dst) {
        return rebase(((LocalPath) src), ((LocalPath) dst));
    }

    LocalPath rebase(LocalPath src, LocalPath dst) {
        if (!startsWith(src)) {
            throw new IllegalArgumentException();
        }

        int retainLen = names.length - src.names.length;
        byte[][] newNames = Arrays.copyOf(dst.names, dst.names.length + retainLen);
        arraycopy(names, src.names.length, newNames, dst.names.length, retainLen);
        return new LocalPath(newNames, dst.absolute);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (absolute ? 1 : 0));
        dest.writeInt(names.length);
        for (byte[] name : names) {
            dest.writeByteArray(name);
        }
    }

    public static final Creator<LocalPath> CREATOR = new Creator<LocalPath>() {

        @Override
        public LocalPath createFromParcel(Parcel source) {
            boolean absolute = source.readByte() == 1;
            int len = source.readInt();
            byte[][] names = new byte[len][];
            for (int i = 0; i < len; i++) {
                names[i] = source.createByteArray();
            }
            return new LocalPath(names, absolute);
        }

        @Override
        public LocalPath[] newArray(int size) {
            return new LocalPath[size];
        }

    };

}
