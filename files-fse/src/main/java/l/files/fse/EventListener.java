package l.files.fse;

interface EventListener {
  void onOpen(String path);
  void onAccess(String path);
  void onAttrib(String path);
  void onCreate(String path);
  void onDelete(String path);
  void onModify(String path);
  void onMovedTo(String path);
  void onMoveSelf(String path);
  void onMovedFrom(String path);
  void onCloseWrite(String path);
  void onDeleteSelf(String path);
  void onCloseNoWrite(String path);
}
