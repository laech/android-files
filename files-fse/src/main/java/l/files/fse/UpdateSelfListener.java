package l.files.fse;

import java.io.File;

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
      updateSelf();
    }
  }

  @Override public void onCreate(String path) {
    super.onCreate(path);
    updateSelf();
  }

  @Override public void onMovedTo(String path) {
    super.onMovedTo(path);
    updateSelf();
  }

  @Override public void onMovedFrom(String path) {
    super.onMovedFrom(path);
    updateSelf();
  }

  @Override public void onDelete(String path) {
    super.onDelete(path);
    updateSelf();
  }

  @Override public void onMoveSelf(String path) {
    super.onMoveSelf(path);
    deleteSelf();
  }

  @Override public void onDeleteSelf(String path) {
    super.onDeleteSelf(path);
    deleteSelf();
  }

  private void deleteSelf() {
    listener.onFileRemoved(parent, path);
  }

  private void updateSelf() {
    listener.onFileChanged(parent, path);
  }
}
