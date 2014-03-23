package l.files.fse;

class EventAdapter implements EventListener {
  @Override public void onOpen(String path) {}
  @Override public void onAccess(String path) {}
  @Override public void onAttrib(String path) {}
  @Override public void onCreate(String path) {}
  @Override public void onDelete(String path) {}
  @Override public void onModify(String path) {}
  @Override public void onMovedTo(String path) {}
  @Override public void onMoveSelf(String path) {}
  @Override public void onMovedFrom(String path) {}
  @Override public void onCloseWrite(String path) {}
  @Override public void onDeleteSelf(String path) {}
  @Override public void onCloseNoWrite(String path) {}
}
