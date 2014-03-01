package l.files.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileLocation;

/**
 * This listener handles events that could cause the properties (last modified,
 * attributes etc) of the directory to be changed, and updates the database
 * record for the directory in the database accordingly.
 */
final class UpdateSelfListener extends DirWatcherListenerAdapter
    implements Runnable {

  private final File dir;
  private final String parentLocation;
  private final Uri parentContentUri;
  private final Processor processor;
  private final SQLiteOpenHelper helper;
  private final Callback callback;
  private final Context context;

  UpdateSelfListener(
      Context context,
      File dir,
      Processor processor,
      SQLiteOpenHelper helper,
      Callback callback) {
    this.context = checkNotNull(context, "context");
    this.dir = checkNotNull(dir, "dir");
    this.helper = checkNotNull(helper, "helper");
    this.processor = checkNotNull(processor, "processor");
    this.callback = checkNotNull(callback, "callback");
    File parentFile = dir.getAbsoluteFile().getParentFile();
    this.parentLocation = parentFile == null ? null : getFileLocation(parentFile);
    this.parentContentUri = parentLocation == null
        ? null : buildFileUri(context, parentLocation);
  }

  @Override public void run() {
    SQLiteDatabase db = helper.getWritableDatabase();
    FilesDb.insertChildren(db, parentLocation, FileData.from(dir));
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
    final String location = getFileLocation(dir);
    final Uri uri = buildFileUri(context, location);
    processor.post(new Runnable() {
      @Override public void run() {
        SQLiteDatabase db = helper.getWritableDatabase();
        FilesDb.deleteChildren(db, location);
      }
    }, uri);
  }

  private void updateSelf() {
    if (parentLocation != null && callback.onPreUpdateSelf(parentLocation)) {
      processor.post(this, parentContentUri);
    }
  }

  static interface Callback {
    /**
     * Called before an update will be fired. This allows the callback to
     * specify whether the update should occur, for example, the update can be
     * skipped if the parent location is not currently monitored - updating will
     * be unnecessary in this case. This method will be called on the same
     * thread as the file observer's event thread.
     *
     * @param parentLocation the {@link FileInfo#LOCATION} of the parent file
     * @return true to continue with the update, false to skip it
     */
    boolean onPreUpdateSelf(String parentLocation);
  }
}
