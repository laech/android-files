package l.files.ui.base.fs;

import androidx.annotation.Nullable;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.messaging.MainThreadTopic;

import static l.files.base.Objects.requireNonNull;

public final class OpenFileEvent {

    public static final MainThreadTopic<OpenFileEvent> topic =
        new MainThreadTopic<>();

    public final Path path;

    @Nullable
    public final Stat stat;

    public OpenFileEvent(java.nio.file.Path path, @Nullable Stat stat) {
        this(Path.of(path), stat);
    }

    public OpenFileEvent(Path path, @Nullable Stat stat) {
        this.path = requireNonNull(path);
        this.stat = stat;
    }
}
