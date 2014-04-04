package l.files.fse;

import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CLOSE_WRITE;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.MODIFY;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;

final class UpdateChildrenListener extends EventAdapter {

  private final String parent;
  private final FileEventListener listener;

  UpdateChildrenListener(String parent, FileEventListener listener) {
    this.parent = parent;
    this.listener = listener;
  }

  @Override public void onCreate(String path) {
    super.onCreate(path);
    addChild(CREATE, path);
  }

  @Override public void onMovedTo(String path) {
    super.onMovedTo(path);
    addChild(MOVED_TO, path);
  }

  @Override public void onAttrib(String path) {
    super.onAttrib(path);
    updateChild(ATTRIB, path);
  }

  @Override public void onModify(String path) {
    super.onModify(path);
    updateChild(MODIFY, path);
  }

  @Override public void onCloseWrite(String path) {
    super.onCloseWrite(path);
    updateChild(CLOSE_WRITE, path);
  }

  @Override public void onMovedFrom(String path) {
    super.onMovedFrom(path);
    deleteChild(MOVED_FROM, path);
  }

  @Override public void onDelete(String path) {
    super.onDelete(path);
    deleteChild(DELETE, path);
  }

  private void addChild(int event, String path) {
    listener.onFileAdded(event, parent, path);
  }

  private void updateChild(int event, String path) {
    if (path != null) {
      listener.onFileChanged(event, parent, path);
    }
  }

  private void deleteChild(int event, String path) {
    listener.onFileRemoved(event, parent, path);
  }
}
