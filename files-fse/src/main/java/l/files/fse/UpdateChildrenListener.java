package l.files.fse;

import l.files.io.Path;

import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CLOSE_WRITE;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.MODIFY;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;

final class UpdateChildrenListener implements EventListener {

  private final Path parent;
  private final FileEventListener listener;

  UpdateChildrenListener(Path parent, FileEventListener listener) {
    this.parent = parent;
    this.listener = listener;
  }

  @Override public void onEvent(int event, String path) {

    if (isChildAdded(event)) {
      notifyChildAdded(event, path);

    } else if (isChildUpdated(event)) {
      notifyChildUpdated(event, path);

    } else if (isChildDeleted(event)) {
      notifyChildDeleted(event, path);
    }
  }

  private boolean isChildAdded(int event) {
    return 0 != (event & CREATE)
        || 0 != (event & MOVED_TO);
  }

  private boolean isChildUpdated(int event) {
    return 0 != (event & ATTRIB)
        || 0 != (event & MODIFY)
        || 0 != (event & CLOSE_WRITE);
  }

  private boolean isChildDeleted(int event) {
    return 0 != (event & MOVED_FROM)
        || 0 != (event & DELETE);
  }

  private void notifyChildAdded(int event, String path) {
    listener.onFileAdded(event, parent.toString(), path);
  }

  private void notifyChildUpdated(int event, String path) {
    if (path != null) {
      listener.onFileChanged(event, parent.toString(), path);
    }
  }

  private void notifyChildDeleted(int event, String path) {
    listener.onFileRemoved(event, parent.toString(), path);
  }
}
