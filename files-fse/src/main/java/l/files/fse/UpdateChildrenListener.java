package l.files.fse;

final class UpdateChildrenListener extends EventAdapter {

  private final String parent;
  private final FileEventListener listener;

  UpdateChildrenListener(String parent, FileEventListener listener) {
    this.parent = parent;
    this.listener = listener;
  }

  @Override public void onCreate(String path) {
    super.onCreate(path);
    addChild(path);
  }

  @Override public void onMovedTo(String path) {
    super.onMovedTo(path);
    addChild(path);
  }

  @Override public void onAttrib(String path) {
    super.onAttrib(path);
    updateChild(path);
  }

  @Override public void onModify(String path) {
    super.onModify(path);
    updateChild(path);
  }

  @Override public void onMovedFrom(String path) {
    super.onMovedFrom(path);
    deleteChild(path);
  }

  @Override public void onDelete(String path) {
    super.onDelete(path);
    deleteChild(path);
  }

  private void addChild(String path) {
    listener.onFileAdded(parent, path);
  }

  private void updateChild(String path) {
    if (path != null) {
      listener.onFileChanged(parent, path);
    }
  }

  private void deleteChild(String path) {
    listener.onFileRemoved(parent, path);
  }
}
