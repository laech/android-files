package l.files.ui.operations.actions;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Name;
import l.files.fs.Path;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static l.files.base.Objects.requireNonNull;

enum Clipboard {

    INSTANCE;

    enum Action {
        CUT, COPY
    }

    private Action action;
    private Path sourceDirectory;
    private Set<Name> sourceFiles = emptySet();

    Clipboard() {
        clear();
    }

    void clear() {
        action = null;
        sourceFiles = emptySet();
        sourceDirectory = null;
    }

    @Nullable
    Action action() {
        return action;
    }

    @Nullable
    Path sourceDirectory() {
        return sourceDirectory;
    }

    Set<Name> sourceFiles() {
        return sourceFiles;
    }

    void set(Action action, Path directory, Collection<? extends Name> sourceFiles) {
        this.action = requireNonNull(action);
        this.sourceDirectory = requireNonNull(directory);
        this.sourceFiles = unmodifiableSet(new HashSet<>(sourceFiles));
    }

}
