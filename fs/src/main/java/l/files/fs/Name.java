package l.files.fs;

import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

public abstract class Name implements Parcelable {

    public abstract byte[] toByteArray();

    public abstract Path toPath();

    public abstract boolean isHidden();

    public abstract void appendTo(ByteArrayOutputStream out);

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
    public abstract String base();

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
    public abstract String extension();

    /**
     * {@link #extension()} with a leading dot if it's not empty.
     */
    public String dotExtension() {
        String ext = extension();
        return ext.isEmpty() ? ext : "." + ext;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
