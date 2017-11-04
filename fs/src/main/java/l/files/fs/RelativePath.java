package l.files.fs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.support.annotation.Nullable;

import static java.util.Collections.unmodifiableList;

final class RelativePath extends Path {

    private final List<Name> names;

    @SuppressWarnings("unchecked")
    RelativePath(List<? extends Name> names) {
        this.names = unmodifiableList(new ArrayList<>(names));
    }

    @Override
    public void toByteArray(ByteArrayOutputStream out) {
        Iterator<Name> iterator = names.iterator();
        while (iterator.hasNext()) {
            iterator.next().appendTo(out);
            if (iterator.hasNext()) {
                out.write('/');
            }
        }
    }

    @Override
    public AbsolutePath toAbsolutePath() {
        return workingDirectoryPath().concat(this);
    }

    private AbsolutePath workingDirectoryPath() {
        return (AbsolutePath) of(new File("").getAbsolutePath());
    }

    @Override
    public List<Name> names() {
        return names;
    }

    @Override
    public RelativePath concat(Path path) {
        List<Name> names = new ArrayList<>();
        names.addAll(this.names);
        names.addAll(path.names());
        return new RelativePath(names);
    }

    @Nullable
    @Override
    public RelativePath parent() {
        return names.isEmpty() ? null : new RelativePath(parentNames());
    }

    private List<Name> parentNames() {
        return names.subList(0, names.size() - 1);
    }

    @Override
    public Name name() {
        return !names.isEmpty() ? lastName() : null;
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

    private List<Name> namesToLengthOf(Path prefix) {
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

    private List<Name> namesFromLengthOf(Path prefix) {
        return names.subList(prefix.names().size(), names.size());
    }
}
