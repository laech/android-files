package l.files.fs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;

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
    public Path toAbsolutePath() {
        return workingDirectoryPath().concat(this);
    }

    private Path workingDirectoryPath() {
        return fromString(new File("").getAbsolutePath());
    }

    @Override
    public boolean isAbsolute() {
        return false;
    }

    @Override
    ImmutableList<Name> names() {
        return names;
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
                .addAll(path.names())
                .build());
    }

    @Nullable
    @Override
    public RelativePath parent() {
        return names.isEmpty() ? null : new RelativePath(parentNames());
    }

    private ImmutableList<Name> parentNames() {
        return names.subList(0, names.size() - 1);
    }

    @Override
    public RelativePath name() {
        return new RelativePath(lastNameAsListOrEmpty());
    }

    private ImmutableList<Name> lastNameAsListOrEmpty() {
        return names.subList(max(0, names.size() - 1), names.size());
    }

    public boolean isHidden() {
        return !names.isEmpty() && lastName().isHidden();
    }

    private Name lastName() {
        return names.get(names.size() - 1);
    }

    @Override
    public boolean startsWith(Path prefix) {
        return prefix instanceof RelativePath &&
                prefix.names().size() <= names.size() &&
                prefix.names().equals(namesToLengthOf(prefix));
    }

    private ImmutableList<Name> namesToLengthOf(Path prefix) {
        return names.subList(0, prefix.names().size());
    }

    @Override
    public Path rebase(Path oldPrefix, Path newPrefix) {
        ensurePrefixIsValid(oldPrefix);
        return newPrefix.concat(pathFromLengthOf(oldPrefix));
    }

    private void ensurePrefixIsValid(Path prefix) {
        if (!startsWith(prefix)) {
            throw new IllegalArgumentException(
                    "\"" + this + "\" does not start with " +
                            "\"" + prefix + "\"");
        }
    }

    private RelativePath pathFromLengthOf(Path prefix) {
        return new RelativePath(namesFromLengthOf(prefix));
    }

    private ImmutableList<Name> namesFromLengthOf(Path prefix) {
        return names.subList(prefix.names().size(), names.size());
    }
}
