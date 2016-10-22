package l.files.fs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

import static l.files.fs.Files.UTF_8;

public final class FileName implements Parcelable {

    private final byte[] bytes;

    private FileName(byte[] bytes) {
        this.bytes = bytes.clone();
        for (byte b : this.bytes) {
            if (b == '/') {
                throw new IllegalArgumentException("'/' is not allowed in a file name: "
                        + new String(bytes, UTF_8));
            }
        }
    }

    public static FileName fromString(String name) {
        return fromBytes(name.getBytes(UTF_8));
    }

    public static FileName fromBytes(byte[] name) {
        return new FileName(name);
    }

    private int indexOfExtensionSeparator() {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] != '.') {
            i--;
        }
        return (i == -1 || i == 0 || i == bytes.length - 1) ? -1 : i;
    }

    public byte[] toByteArray() {
        return bytes.clone();
    }

    /**
     * The name part without extension.
     * <pre>
     *  base.ext  ->  base
     *  base      ->  base
     *  base.     ->  base.
     * .base.ext  -> .base
     * .base      -> .base
     * .base.     -> .base.
     * .          -> .
     * ..         -> ..
     * </pre>
     */
    public String base() {
        int i = indexOfExtensionSeparator();
        return i != -1
                ? new String(bytes, 0, i, UTF_8)
                : toString();
    }

    /**
     * The extension part without base name.
     * <pre>
     *  base.ext  ->  ext
     * .base.ext  ->  ext
     *  base      ->  ""
     *  base.     ->  ""
     * .base      ->  ""
     * .base.     ->  ""
     * .          ->  ""
     * ..         ->  ""
     * </pre>
     */
    public String extension() {
        int i = indexOfExtensionSeparator();
        if (i == -1) {
            return "";
        }
        int start = i + 1;
        int count = bytes.length - start;
        return new String(bytes, start, count, UTF_8);
    }

    /**
     * {@link #extension()} with a leading dot if it's not empty.
     */
    public String dotExtension() {
        String ext = extension();
        return ext.isEmpty() ? ext : "." + ext;
    }

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
        return o instanceof FileName &&
                Arrays.equals(bytes, ((FileName) o).bytes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(bytes);
    }

    public static final Creator<FileName> CREATOR = new Creator<FileName>() {

        @Override
        public FileName createFromParcel(Parcel source) {
            return FileName.fromBytes(source.createByteArray());
        }

        @Override
        public FileName[] newArray(int size) {
            return new FileName[size];
        }

    };

}
