package l.files.ui.operations.actions;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static l.files.base.Objects.requireNonNull;

enum Clipboard {

    INSTANCE;

    enum Action {
        CUT, COPY
    }

    private Action action;
    private Set<File> files;

    Clipboard() {
        clear();
    }

    void clear() {
        action = null;
        files = emptySet();
    }

    @Nullable
    Action action() {
        return action;
    }

    Set<File> files() {
        return files;
    }

    void set(Action action, Collection<? extends File> files) {
        this.action = requireNonNull(action);
        this.files = unmodifiableSet(new HashSet<>(files));
    }

}
