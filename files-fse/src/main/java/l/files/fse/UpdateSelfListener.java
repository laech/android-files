package l.files.fse;

import java.io.File;

import static android.os.FileObserver.ATTRIB;
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
final class UpdateSelfListener extends EventAdapter {

  private final String parent;
  private final String path;
  private final FileEventListener listener;

  UpdateSelfListener(String path, FileEventListener listener) {
    File file = new File(checkNotNull(path, "path"));
    this.parent = file.getParent();
    this.path = file.getName();
    this.listener = checkNotNull(listener, "listener");
  }

  @Override public void onAttrib(String path) {
    super.onAttrib(path);
    boolean self = path == null;
    if (self) {
      updateSelf(ATTRIB);
    }
  }

  @Override public void onCreate(String path) {
    super.onCreate(path);
    updateSelf(CREATE);
  }

  @Override public void onMovedTo(String path) {
    super.onMovedTo(path);
    updateSelf(MOVED_TO);
  }

  @Override public void onMovedFrom(String path) {
    super.onMovedFrom(path);
    updateSelf(MOVED_FROM);
  }

  @Override public void onDelete(String path) {
    super.onDelete(path);
    updateSelf(DELETE);
  }

  @Override public void onMoveSelf(String path) {
    super.onMoveSelf(path);
    deleteSelf(MOVE_SELF);
  }

  @Override public void onDeleteSelf(String path) {
    super.onDeleteSelf(path);
    deleteSelf(DELETE_SELF);
  }

  private void deleteSelf(int event) {
    listener.onFileRemoved(event, parent, path);
  }

  private void updateSelf(int event) {
    listener.onFileChanged(event, parent, path);
  }
}
