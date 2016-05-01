package l.files.fs.local;

import android.os.Parcel;

import java.util.Arrays;

import l.files.fs.Name;

import static l.files.fs.Files.UTF_8;
import static l.files.fs.local.LocalPath.DOT;
import static l.files.fs.local.LocalPath.SEP;

final class LocalName implements Name {

    final byte[] bytes;

    LocalName(byte[] bytes) {
        for (byte b : bytes) {
            if (b == SEP) {
                throw new IllegalArgumentException();
            }
        }
        this.bytes = bytes;
    }

    public static LocalName of(String name) {
        return wrap(name.getBytes(UTF_8));
    }

    static LocalName wrap(byte[] name) {
        return new LocalName(name);
    }

    private int indexOfExtSeparator() {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] != DOT) {
            i--;
        }
        return (i == -1 || i == 0 || i == bytes.length - 1) ? -1 : i;
    }

    @Override
    public byte[] toByteArray() {
        return bytes.clone();
    }

    @Override
    public String base() {
        int i = indexOfExtSeparator();
        return i != -1
                ? new String(bytes, 0, i, UTF_8)
                : toString();
    }

    @Override
    public String ext() {
        int i = indexOfExtSeparator();
        if (i == -1) {
            return "";
        }
        int start = i + 1;
        int count = bytes.length - start;
        return new String(bytes, start, count, UTF_8);
    }

    @Override
    public String dotExt() {
        String ext = ext();
        return ext.isEmpty() ? ext : "." + ext;
    }

    @Override
    public boolean isEmpty() {
        return bytes.length == 0;
    }

    @Override
    public String toString() {
        return new String(bytes, UTF_8);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LocalName &&
                Arrays.equals(bytes, ((LocalName) o).bytes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(bytes);
    }

    public static final Creator<LocalName> CREATOR = new Creator<LocalName>() {

        @Override
        public LocalName createFromParcel(Parcel source) {
            return LocalName.wrap(source.createByteArray());
        }

        @Override
        public LocalName[] newArray(int size) {
            return new LocalName[size];
        }

    };

}
