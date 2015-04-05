package l.files.fs.local;

import java.io.File;
import java.net.URI;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.Path;

import static com.google.common.base.Preconditions.checkArgument;

@AutoParcel
public abstract class LocalPath implements Path {

    LocalPath() {
    }

    abstract File getFile();

    @Override
    public LocalPath getPath() {
        return this;
    }

    @Override
    public LocalResource getResource() {
        return LocalResource.create(this);
    }

    @Override
    public URI getUri() {
        return sanitizedUri(getFile());
    }

    @Override
    public boolean isHidden() {
        return getFile().isHidden();
    }

    @Override
    public boolean startsWith(Path other) {
        if (other.getParent() == null || other.equals(this)) {
            return true;
        }
        if (other instanceof LocalPath) {
            String thisPath = getFile().getPath();
            String thatPath = ((LocalPath) other).getFile().getPath();
            return thisPath.startsWith(thatPath) &&
                    thisPath.charAt(thatPath.length()) == '/';
        }
        return false;
    }

    @Nullable
    @Override
    public LocalPath getParent() {
        if ("/".equals(getFile().getPath())) {
            return null;
        } else {
            return new AutoParcel_LocalPath(getFile().getParentFile());
        }
    }

    @Override
    public String getName() {
        return getFile().getName();
    }

    @Override
    public LocalPath resolve(String other) {
        return of(new File(getFile(), other));
    }

    @Override
    public LocalPath replace(Path prefix, Path replacement) {
        check(prefix);
        check(replacement);
        checkArgument(startsWith(prefix));

        File parent = ((LocalPath) replacement).getFile();
        String child = getFile().getPath().substring(prefix.toString().length());
        return new AutoParcel_LocalPath(new File(parent, child));
    }

    @Override
    public String toString() {
        return getFile().toString();
    }

    /**
     * If the given path is an instance of this class, throws
     * IllegalArgumentException if it's not.
     */
    public static LocalPath check(Path path) {
        if (path instanceof LocalPath) {
            return (LocalPath) path;
        } else {
            throw new IllegalArgumentException(path.getUri().toString());
        }
    }

    public static LocalPath of(File file) {
        return new AutoParcel_LocalPath(new File(sanitizedUri(file)));
    }

    public static LocalPath of(String path) {
        return of(new File(path));
    }

    private static URI sanitizedUri(File file) {
        /*
         * Don't return File.toURI as it will append a "/" to the end of the URI
         * depending on whether or not the file is a directory, that means two
         * calls to the method before and after the directory is deleted will
         * create two URIs that are not equal.
         */
        URI uri = file.toURI().normalize();
        String uriStr = uri.toString();
        if ("/".equals(uri.getRawPath()) && uriStr.endsWith("/")) {
            return URI.create(uriStr.substring(0, uriStr.length() - 1));
        }
        return uri;
    }

}
