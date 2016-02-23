package l.files.fs;

import android.os.Parcelable;

public interface Name extends Parcelable {

    byte[] toByteArray();

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
    String base();

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
    String ext();

    /**
     * {@link #ext()} with a leading dot if it's not empty.
     */
    String dotExt();

    boolean isEmpty();

    boolean isHidden();
}
