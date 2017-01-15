package l.files.fs.local;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nullable;

import l.files.fs.Name;
import l.files.fs.Path;

import static com.google.common.base.Preconditions.checkNotNull;

final class AbsolutePath extends LocalPath {

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
    public AbsolutePath toAbsolutePath() {
        return this;
    }

    @Override
    public ImmutableList<Name> names() {
        return path.names();
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
    public AbsolutePath concat(Path path) {
        return new AbsolutePath(this.path.concat(path));
    }

    @Nullable
    @Override
    public AbsolutePath parent() {
        RelativePath parent = path.parent();
        return parent == null ? null : new AbsolutePath(parent);
    }

    @Override
    public Name name() {
        return path.name();
    }

    @Override
    public boolean isHidden() {
        return path.isHidden();
    }

    @Override
    public boolean startsWith(Path prefix) {
        return prefix instanceof AbsolutePath &&
                path.startsWith(((AbsolutePath) prefix).path);
    }

    @Override
    public Path rebase(Path oldPrefix, Path newPrefix) {
        ensurePrefixIsAbsolute(oldPrefix);
        try {

            return path.rebase(((AbsolutePath) oldPrefix).path, newPrefix);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "\"" + this + "\" does not start with " +
                            "\"" + oldPrefix + "\"", e);
        }
    }

    private void ensurePrefixIsAbsolute(Path oldPrefix) {
        if (!(oldPrefix instanceof AbsolutePath)) {
            throw new IllegalArgumentException(
                    "\"" + this + "\" does not start with " +
                            "\"" + oldPrefix + "\"");
        }
    }
}
