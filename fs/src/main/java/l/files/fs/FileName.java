package l.files.fs;

import com.google.auto.value.AutoValue;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

@AutoValue
public abstract class FileName implements CharSequence {

    FileName() {
    }

    abstract String value();

    public static FileName of(String name) {
        return new AutoValue_FileName(name);
    }

    public static FileName empty() {
        return of("");
    }

    /**
     * Locale sensitive name comparator.
     */
    public static Comparator<FileName> comparator(Locale locale) {
        final Collator collator = Collator.getInstance(locale);
        return new Comparator<FileName>() {
            @Override
            public int compare(FileName a, FileName b) {
                return collator.compare(a.toString(), b.toString());
            }
        };
    }

    private int indexOfExtSeparator() {
        int i = value().lastIndexOf('.');
        return (i == -1 || i == 0 || i == length() - 1) ? -1 : i;
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
        int i = indexOfExtSeparator();
        return i != -1 ? value().substring(0, i) : value();
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
    public String ext() {
        int i = indexOfExtSeparator();
        return i != -1 ? value().substring(i + 1) : "";
    }

    /**
     * {@link #ext()} with a leading dot if it's not empty.
     */
    public String dotExt() {
        String ext = ext();
        return ext.isEmpty() ? ext : "." + ext;
    }

    @Override
    public int length() {
        return value().length();
    }

    @Override
    public char charAt(int index) {
        return value().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return value().subSequence(start, end);
    }

    @Override
    public String toString() {
        return value();
    }

}
