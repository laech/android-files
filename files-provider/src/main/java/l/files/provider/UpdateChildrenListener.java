package l.files.provider;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileLocation;

final class UpdateChildrenListener extends DirWatcherListenerAdapter {

  private final File dir;
  private final String location;
  private final Uri contentUri;
  private final SQLiteOpenHelper helper;
  private final Processor processor;
  private final Callback callback;

  UpdateChildrenListener(
      File dir, Processor processor, SQLiteOpenHelper helper, Callback callback) {
    this.dir = checkNotNull(dir, "dir");
    this.helper = checkNotNull(helper, "helper");
    this.processor = checkNotNull(processor, "processor");
    this.callback = checkNotNull(callback, "callback");
    this.location = getFileLocation(dir);
    this.contentUri = buildFileUri(location);
  }

  @Override public void onCreate(String path) {
    super.onCreate(path);
    updateChild(path, true);
  }

  @Override public void onMovedTo(String path) {
    super.onMovedTo(path);
    updateChild(path, true);
  }

  @Override public void onAttrib(String path) {
    super.onAttrib(path);
    updateChild(path, false);
  }

  @Override public void onModify(String path) {
    super.onModify(path);
    updateChild(path, false);
  }

  @Override public void onMovedFrom(String path) {
    super.onMovedFrom(path);
    deleteChild(path);
  }

  @Override public void onDelete(String path) {
    super.onDelete(path);
    deleteChild(path);
  }

  private void updateChild(String path, boolean isNew) {
    if (path != null) {
      File child = new File(dir, path);
      postUpdate(child);
      if (isNew) {
        callback.onChildAdded(child, getFileLocation(child));
      }
    }
  }

  private void deleteChild(String path) {
    if (path != null) {
      File child = new File(dir, path);
      String childLocation = getFileLocation(child);
      postDelete(childLocation);
      callback.onChildRemoved(child, childLocation);
    }
  }

  private void postUpdate(final File child) {
    processor.post(new Runnable() {
      @Override public void run() {
        SQLiteDatabase db = helper.getWritableDatabase();
        FilesDb.insertChildren(db, location, FileData.from(child));
      }
    }, contentUri);
  }

  private void postDelete(final String childLocation) {
    processor.post(new Runnable() {
      @Override public void run() {
        SQLiteDatabase db = helper.getWritableDatabase();
        FilesDb.deleteChild(db, childLocation);
      }
    }, contentUri);
  }

  static interface Callback {
    /**
     * Notified the given child has been added to this directory. This will be
     * called on the same thread as the file observer's event thread.
     *
     * @param child the child that was added, note that this file may already
     * been changed or deleted by the time this method is called.
     * @param childLocation the {@link FileInfo#LOCATION} of this child
     */
    void onChildAdded(File child, String childLocation);

    /**
     * Notified the given child has been removed from this directory. This will
     * be called on the same thread as the file observer's event thread.
     *
     * @param child the child that was removed, note that this is a handle of
     * the deleted file, any properties return from this handle may not be valid
     * (and new file with same name may already been created by the time this
     * method is called).
     * @param childLocation the {@link FileInfo#LOCATION} of this child
     */
    void onChildRemoved(File child, String childLocation);
  }
}
