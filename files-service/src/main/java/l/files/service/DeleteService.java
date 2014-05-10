package l.files.service;

import android.content.Context;
import android.content.Intent;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import l.files.io.file.operations.Counter;
import l.files.io.file.operations.Deleter;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.io.file.Files.toAbsolutePaths;
import static l.files.io.file.Files.toFiles;
import static l.files.service.Util.showErrorMessage;

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

  private final class DeleteTask extends Task<Object, Progress, IOException>
      implements Counter.Listener, Deleter.Listener {

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

    @Override protected String getNotificationContentTitle(Progress progress) {
      return progress.getNotificationContentTitle();
    }

    @Override
    protected float getNotificationProgressPercentage(Progress progress) {
      return progress.getNotificationProgressPercentage();
    }

    @Override protected IOException doTask() {
      try {

        Counter.Result count = new Counter(this, files, this).call();
        new Deleter(this, files, this, count.count).call();

      } catch (IOException e) {
        return e;
      }
      return null;
    }

    @Override protected void onPostExecute(IOException e) {
      super.onPostExecute(e);
      if (e != null) {
        showErrorMessage(service, e);
      }
    }

    @Override public void onFileCounted(int count, long length) {
      if (setAndGetUpdateProgress()) {
        publishProgress(new PrepareProgress(count));
      }
    }

    @Override public void onFileDeleted(int total, int remaining) {
      if (setAndGetUpdateProgress()) {
        publishProgress(new DeleteProgress(total, remaining));
      }
    }
  }

  final class PrepareProgress extends Progress {
    private final int count;

    PrepareProgress(int count) {
      this.count = count;
    }

    @Override String getNotificationContentTitle() {
      return getResources().getQuantityString(
          R.plurals.preparing_delete_x_items, count, count);
    }
  }

  final class DeleteProgress extends DeleteBaseProgress {

    DeleteProgress(int total, int remaining) {
      super(total, remaining);
    }

    @Override String getNotificationContentTitle() {
      return getResources().getQuantityString(
          R.plurals.deleting_x_items, remaining, remaining);
    }
  }
}
