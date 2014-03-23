package l.files.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.google.common.base.Optional;

import java.io.File;
import java.util.Map;

import l.files.fse.FileEventService;
import l.files.fse.FileEventListener;
import l.files.os.Stat;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileLocation;

final class FilesDbSync implements FileEventListener {

  private final Context context;
  private final SQLiteOpenHelper helper;
  private final FileEventService manager;

  FilesDbSync(Context context, SQLiteOpenHelper helper) {
    this.context = checkNotNull(context, "context");
    this.helper = checkNotNull(helper, "helper");
    this.manager = FileEventService.create();
    this.manager.register(this);
  }

  @Override public void onFileAdded(String parent, String path) {
    onFileAddedOrChanged(parent, path);
  }

  @Override public void onFileChanged(String parent, String path) {
    onFileAddedOrChanged(parent, path);
  }

  private void onFileAddedOrChanged(final String parent, final String path) {
    final String parentLocation = getFileLocation(new File(parent));
    final Uri parentUri = buildFileUri(context, parentLocation);
    processor(parent).post(new Runnable() {
      @Override public void run() {
        SQLiteDatabase db = helper.getWritableDatabase();
        FilesDb.insertChildren(db, parentLocation, FileData.from(new File(parent, path))); // TODO use stat
      }
    }, parentUri);
  }

  @Override public void onFileRemoved(final String parent, final String path) {
    final String parentLocation = getFileLocation(new File(parent));
    final String childLocation = getFileLocation(new File(parent, path));
    final Uri parentUri = buildFileUri(context, parentLocation);
    final Uri childUri = buildFileUri(context, childLocation);
    processor(parent).post(new Runnable() {
      @Override public void run() {
        SQLiteDatabase db = helper.getWritableDatabase();
        FilesDb.deleteChild(db, childLocation);
        FilesDb.deleteChildren(db, childLocation); // TODO
      }
    }, childUri, parentUri);
  }

  private final Map<String, Processor> processors = newHashMap();

  private Processor processor(String path) {
    synchronized (this) {
      Processor processor = processors.get(path);
      if (processor == null) {
        processor = new Processor(helper, context.getContentResolver());
        processors.put(path, processor);
      }
      return processor;
    }
  }

  public boolean isStarted(File file) {
    return manager.isMonitored(file);
  }

  public Optional<Map<File, Stat>> start(File file) {
    return manager.monitor(file);
  }
}
