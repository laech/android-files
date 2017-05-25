package l.files.fs;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.annotation.Nullable;

import static l.files.base.Objects.requireNonNull;

final class AbsolutePath extends Path {

    private final RelativePath path;

    AbsolutePath(RelativePath path) {
        this.path = requireNonNull(path);
    }

    @Override
    public void toByteArray(ByteArrayOutputStream out) {
        out.write('/');
        path.toByteArray(out);
    }

    @Override
    public AbsolutePath toAbsolutePath() {
        return this;
    }

    @Override
    public List<Name> names() {
        return path.names();
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
