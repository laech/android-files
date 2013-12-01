package l.files.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFiles;
import static org.apache.commons.io.DirectoryWalker.CancelException;

public final class DeleteService extends ProgressService {

  private static final String EXTRA_FILE_PATHS = "l.files.intent.extra.FILE_PATHS";

  public static void delete(Context context, Set<File> files) {
    context.startService(new Intent(context, DeleteService.class)
        .putExtra(EXTRA_FILE_PATHS, toAbsolutePaths(files)));
  }

  @Override protected Task<?, ?, ?> newTask(Intent intent, int id) {
    return new DeleteTask(id, ImmutableSet.copyOf(getFiles(intent)));
  }

  private File[] getFiles(Intent intent) {
    return toFiles(newHashSet(intent.getStringArrayExtra(EXTRA_FILE_PATHS)));
  }

  private final class DeleteTask extends Task<Object, String, Void>
      implements FilesCounter.Listener, FilesDeleter.Listener {

    private final Set<File> files;

    DeleteTask(int id, Set<File> files) {
      super(id, DeleteService.this);
      this.files = files;
    }

    @Override protected int getNotificationSmallIcon() {
      return R.drawable.ic_stat_notify_delete;
    }

    @Override protected String getNotificationContentTitle() {
      return getString(R.string.preparing_to_delete);
    }

    @Override protected String getNotificationContentTitle(String progress) {
      return progress;
    }

    @Override protected Void doTask() {
      try {

        FilesCounter.Result count = new FilesCounter(this, files).execute();
        new FilesDeleter(this, files, count.count).execute();

      } catch (CancelException e) {
        return null;
      } catch (IOException e) {
        Log.e(DeleteService.class.getSimpleName(), e.getMessage(), e); // TODO
      }
      return null;
    }

    @Override public void onFileCounted(int count, long length) {
      if (setAndGetUpdateProgress()) {
        publishProgress(getResources().getQuantityString(
            R.plurals.preparing_delete_x_items, count, count));
      }
    }

    @Override public void onFileDeleted(int remaining) {
      if (setAndGetUpdateProgress()) {
        publishProgress(getResources().getQuantityString(
            R.plurals.deleting_x_items, remaining, remaining));
      }
    }
  }
}
