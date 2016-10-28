package l.files.fs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.min;

final class RelativePath extends Path {

    private final ImmutableList<Name> names;

    RelativePath(ImmutableList<Name> names) {
        this.names = checkNotNull(names);
    }

    @Override
    void toByteArray(ByteArrayOutputStream out) {
        UnmodifiableIterator<Name> iterator = names.iterator();
        while (iterator.hasNext()) {
            iterator.next().appendTo(out);
            if (iterator.hasNext()) {
                out.write('/');
            }
        }
    }

    @Override
    void toString(StringBuilder builder) {
        Joiner.on('/').appendTo(builder, names);
    }

    @Override
    public AbsolutePath toAbsolutePath() {
        Path workingDirectory = fromString(new File("").getAbsolutePath());
        return workingDirectory.concat(this).toAbsolutePath();
    }

    @Override
    RelativePath toRelativePath() {
        return this;
    }

    @Override
    public int hashCode() {
        return names.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RelativePath &&
                names.equals(((RelativePath) o).names);
    }

    @Override
    public RelativePath concat(Path path) {
        return new RelativePath(ImmutableList.<Name>builder()
                .addAll(names)
                .addAll(path.toRelativePath().names)
                .build());
    }

    @Nullable
    @Override
    public RelativePath parent() {
        if (names.isEmpty()) {
            return null;
        }
        return new RelativePath(names.subList(0, names.size() - 1));
    }

    @Override
    public RelativePath name() {
        return new RelativePath(names.reverse().subList(0, min(1, names.size())));
    }

    public boolean isHidden() {
        return !names.isEmpty() &&
                names.get(names.size() - 1).isHidden();
    }

    @Override
    public boolean startsWith(Path prefix) {
        if (!(prefix instanceof RelativePath)) {
            return false;
        }
        ImmutableList<Name> prefixNames = ((RelativePath) prefix).names;
        return prefixNames.size() <= names.size() &&
                prefixNames.equals(names.subList(0, prefixNames.size()));
    }

    @Override
    public Path rebase(Path src, Path dst) {
        if (!startsWith(src)) {
            throw new IllegalArgumentException(
                    "\"" + this + "\" does not start with \"" + src + "\"");
        }
        int prefixSize = ((RelativePath) src).names.size();
        return dst.concat(new RelativePath(names.subList(prefixSize, names.size())));
    }
}
