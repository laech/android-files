package l.files.fs;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

public interface Path extends Parcelable {

    FileSystem fileSystem();

    /**
     * Returns a string representation of this path.
     * <p/>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     *
     * @see String#String(byte[], int, int, Charset)
     * @see ByteArrayOutputStream#toString(String)
     * @see #toByteArray(ByteArrayOutputStream)
     */
    @Override
    String toString();

    URI toUri();

    /**
     * @return the number of bytes written
     */
    int toByteArray(OutputStream out) throws IOException;

    byte[] toByteArray() throws IOException;

    /**
     * @return the number of bytes written
     */
    int toByteArray(ByteArrayOutputStream out);

    /**
     * Returns the file name of this path.
     */
    Name name();

    @Nullable
    Path parent();

    Path resolve(String name);

    Path resolve(Name name);

    Path rebase(Path src, Path dst);

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

    boolean isHidden();
}
