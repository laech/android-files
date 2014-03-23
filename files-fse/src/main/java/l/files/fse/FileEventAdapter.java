package l.files.fse;

public class FileEventAdapter implements FileEventListener {
  @Override public void onFileAdded(String parent, String path) {}
  @Override public void onFileChanged(String parent, String path) {}
  @Override public void onFileRemoved(String parent, String path) {}
}
