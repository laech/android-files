package l.files.fs;

import com.google.common.base.Objects;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

final class AbsolutePath extends Path {

    private final RelativePath path;

    AbsolutePath(RelativePath path) {
        this.path = checkNotNull(path);
    }

    @Override
    void toByteArray(ByteArrayOutputStream out) {
        out.write('/');
        path.toByteArray(out);
    }

    @Override
    void toString(StringBuilder builder) {
        builder.append('/');
        path.toString(builder);
    }

    @Override
    AbsolutePath toAbsolutePath() {
        return this;
    }

    @Override
    RelativePath toRelativePath() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path, getClass());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AbsolutePath &&
                path.equals(((AbsolutePath) o).path);
    }

    @Override
    public Path concat(Path path) {
        return new AbsolutePath(this.path.concat(path));
    }

    @Nullable
    @Override
    public Path parent() {
        RelativePath parent = path.parent();
        if (parent != null) {
            return new AbsolutePath(parent);
        }
        return null;
    }

    @Override
    public Path name() {
        return path.name();
    }

    @Override
    public boolean isHidden() {
        return path.isHidden();
    }

    @Override
    public boolean startsWith(Path that) {
        return that instanceof AbsolutePath &&
                path.startsWith(((AbsolutePath) that).path);
    }

    @Override
    public Path rebase(Path src, Path dst) {
        if (!(src instanceof AbsolutePath)) {
            throw new IllegalArgumentException(
                    "\"" + this + "\" does not start with \"" + src + "\"");
        }
        try {
            return path.rebase(((AbsolutePath) src).path, dst);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "\"" + this + "\" does not start with \"" + src + "\"", e);
        }
    }
}
