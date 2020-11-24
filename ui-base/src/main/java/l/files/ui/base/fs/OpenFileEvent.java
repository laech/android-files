package l.files.ui.base.fs;

import l.files.ui.base.messaging.MainThreadTopic;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public final class OpenFileEvent {

    public static final MainThreadTopic<OpenFileEvent> topic =
        new MainThreadTopic<>();

    public final Path path;

    public OpenFileEvent(Path path) {
        this.path = requireNonNull(path);
    }
}
