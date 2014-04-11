package l.files.fse;

import l.files.io.Path;

import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This listener handles events that could cause the properties (last modified,
 * attributes etc) of the directory to be changed, and updates the database
 * record for the directory in the database accordingly.
 */
final class UpdateSelfListener implements EventListener {

  private final Path path;
  private final FileEventListener listener;

  UpdateSelfListener(Path path, FileEventListener listener) {
    checkNotNull(path, "path");
    checkArgument(path.parent() != null, "%s has no parent", path);
    this.path = path;
    this.listener = checkNotNull(listener, "listener");
  }

  @Override public void onEvent(int event, String path) {

    if (isSelfUpdated(event)) {
      notifySelfUpdated(event);

    } else if (isSelfDeleted(event)) {
      notifySelfDeleted(event);
    }
  }

  private boolean isSelfUpdated(int event) {
    return 0 != (event & CREATE)
        || 0 != (event & MOVED_TO)
        || 0 != (event & MOVED_FROM)
        || 0 != (event & DELETE);
  }

  private boolean isSelfDeleted(int event) {
    return 0 != (event & MOVE_SELF)
        || 0 != (event & DELETE_SELF);
  }

  private void notifySelfUpdated(int event) {
    listener.onFileChanged(event, path.parent().toString(), path.name());
  }

  private void notifySelfDeleted(int event) {
    listener.onFileRemoved(event, path.parent().toString(), path.name());
  }
}
