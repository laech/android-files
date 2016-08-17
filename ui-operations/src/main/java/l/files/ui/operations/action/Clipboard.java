package l.files.ui.operations.action;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static l.files.base.Objects.requireNonNull;

public enum Clipboard {

    INSTANCE;

    public enum Action {
        CUT, COPY
    }

    private Action action;
    private Set<Path> files;

    Clipboard() {
        clear();
    }

    public void clear() {
        action = null;
        files = emptySet();
    }

    @Nullable
    public Action action() {
        return action;
    }

    public Set<Path> paths() {
        return files;
    }

    void set(Action action, Collection<? extends Path> files) {
        this.action = requireNonNull(action);
        this.files = unmodifiableSet(new HashSet<>(files));
    }

}
