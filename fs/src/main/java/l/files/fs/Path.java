package l.files.fs;

import android.os.Parcelable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public interface Path extends Parcelable {

    /**
     * Returns a string representation of this path.
     * <p/>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     *
     * @see String#String(byte[], Charset)
     */
    @Override
    String toString();

    /**
     * Returns the file name of this path.
     */
    Name name();

    /**
     * Returns true if the given path is an ancestor of this path,
     * or equal to this path.
     */
    boolean startsWith(Path p);

    /**
     * @deprecated use {@link #toByteArray(OutputStream)} instead
     */
    @Deprecated
    void writeTo(OutputStream out) throws IOException;

    void toByteArray(OutputStream out) throws IOException;

}
