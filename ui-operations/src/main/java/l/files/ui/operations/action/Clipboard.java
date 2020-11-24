package l.files.ui.operations.action;

import androidx.annotation.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public enum Clipboard {

    INSTANCE;

    public enum Action {
        CUT,
        COPY
    }

    @Nullable
    private Action action;
    private Set<Path> files = emptySet();

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
