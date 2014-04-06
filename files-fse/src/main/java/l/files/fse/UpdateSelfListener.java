package l.files.fse;

import java.io.File;

import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This listener handles events that could cause the properties (last modified,
 * attributes etc) of the directory to be changed, and updates the database
 * record for the directory in the database accordingly.
 */
final class UpdateSelfListener implements EventListener {

  private final String parent;
  private final String path;
  private final FileEventListener listener;

  UpdateSelfListener(String path, FileEventListener listener) {
    File file = new File(checkNotNull(path, "path"));
    this.parent = file.getParent();
    this.path = file.getName();
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
    listener.onFileChanged(event, parent, path);
  }

  private void notifySelfDeleted(int event) {
    listener.onFileRemoved(event, parent, path);
  }
}
