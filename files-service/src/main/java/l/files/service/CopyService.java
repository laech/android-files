package l.files.service;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static android.text.format.DateUtils.formatElapsedTime;
import static android.text.format.Formatter.formatFileSize;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFilesSet;

public final class CopyService extends ProgressService {

  private static final String EXTRA_SOURCES = "sources";
  private static final String EXTRA_DESTINATION = "destination";
  private static final String EXTRA_DELETE_SOURCES = "delete_sources";

  public static void start(Context context, Set<File> sources, File destination) {
    start(context, sources, destination, false);
  }

  public static void start(
      Context context, Set<File> sources, File destination, boolean deleteSourcesOnComplete) {
    context.startService(new Intent(context, CopyService.class)
        .putExtra(EXTRA_SOURCES, toAbsolutePaths(sources))
        .putExtra(EXTRA_DESTINATION, destination.getAbsolutePath())
        .putExtra(EXTRA_DELETE_SOURCES, deleteSourcesOnComplete));
  }

  @Override protected Task<?, ?, ?> newTask(Intent intent, int id) {
    Set<File> sources = getSources(intent);
    File destination = getDestination(intent);
    boolean deleteSourcesOnComplete = getDeleteSourcesOnComplete(intent);
    return new CopyTask(id, sources, destination, deleteSourcesOnComplete);
  }

  private boolean getDeleteSourcesOnComplete(Intent intent) {
    return intent.getBooleanExtra(EXTRA_DELETE_SOURCES, false);
  }

  private File getDestination(Intent intent) {
    return new File(intent.getStringExtra(EXTRA_DESTINATION));
  }

  private Set<File> getSources(Intent intent) {
    return toFilesSet(intent.getStringArrayExtra(EXTRA_SOURCES));
  }

  private final class CopyTask extends Task<Object, Progress, Void>
      implements FilesCounter.Listener, FilesCopier.Listener {

    private final Set<File> sources;
    private final File destination;
    private final boolean deleteSourcesOnComplete;

    private long copyStartTime = -1;

    CopyTask(int id, Set<File> sources, File destination, boolean deleteSourcesOnComplete) {
      super(id, CopyService.this);
      this.sources = sources;
      this.destination = destination;
      this.deleteSourcesOnComplete = deleteSourcesOnComplete;
    }

    @Override protected int getNotificationSmallIcon() {
      return R.drawable.ic_stat_notify_copy;
    }

    @Override protected String getNotificationContentTitle() {
      return getString(R.string.preparing_to_copy_to_x, destination.getName());
    }

    @Override protected String getNotificationContentTitle(Progress progress) {
      return progress.getNotificationContentTitle();
    }

    @Override protected String getNotificationContentText(Progress progress) {
      return progress.getNotificationContentText();
    }

    @Override protected String getNotificationContentInfo(Progress value) {
      return value.getNotificationContentInfo();
    }

    @Override
    protected float getNotificationProgressPercentage(Progress progress) {
      return progress.getNotificationProgressPercentage();
    }

    @Override protected Void doTask() {
      try {

        FilesCounter.Result result = new FilesCounter(this, sources).call();
        new FilesCopier(this, sources, destination, result.count, result.length).call();

      } catch (IOException e) {
        Log.e(CopyService.class.getSimpleName(), e.getMessage(), e); // TODO
      }

      return null;
    }

    @Override protected void onPostExecute(Void none) {
      super.onPostExecute(none);
      if (deleteSourcesOnComplete) {
        DeleteService.delete(CopyService.this, sources);
      }
    }

    @Override public void onFileCounted(int count, long length) {
      if (setAndGetUpdateProgress()) {
        publishProgress(new PrepareProgress(destination, count));
      }
    }

    @Override
    public void onCopied(int remaining, long bytesCopied, long bytesTotal) {
      if (copyStartTime < 0) {
        copyStartTime = SystemClock.elapsedRealtime();
      }
      if (setAndGetUpdateProgress()) {
        long currentTime = SystemClock.elapsedRealtime();
        float speed = bytesCopied / (float) (currentTime - copyStartTime);
        long timeLeft = (long) ((bytesTotal - bytesCopied) / speed);
        publishProgress(new CopyProgress(destination, remaining, bytesCopied, bytesTotal, timeLeft));
      }
    }
  }

  private abstract class Progress {

    abstract String getNotificationContentTitle();

    abstract String getNotificationContentText();

    String getNotificationContentInfo() {
      return null;
    }

    float getNotificationProgressPercentage() {
      return 0;
    }
  }

  private final class PrepareProgress extends Progress {
    private final File destination;
    private final int count;

    PrepareProgress(File destination, int count) {
      this.destination = destination;
      this.count = count;
    }

    @Override String getNotificationContentTitle() {
      return getString(R.string.preparing_to_copy_to_x, destination.getName());
    }

    @Override String getNotificationContentText() {
      return getResources().getQuantityString(
          R.plurals.preparing_to_copy_x_items, count, count);
    }

    @Override float getNotificationProgressPercentage() {
      return 0;
    }
  }

  private final class CopyProgress extends Progress {
    private final File destination;
    private final int remaining;
    private final long bytesCopied;
    private final long bytesTotal;
    private final long timeLeftMillis;

    private CopyProgress(
        File destination,
        int remaining,
        long bytesCopied,
        long bytesTotal,
        long timeLeftMillis) {
      this.destination = destination;
      this.remaining = remaining;
      this.bytesCopied = bytesCopied;
      this.bytesTotal = bytesTotal;
      this.timeLeftMillis = timeLeftMillis;
    }

    @Override String getNotificationContentTitle() {
      return getResources().getQuantityString(R.plurals.copying_x_items_to_x,
          remaining, remaining, destination.getName());
    }

    @Override String getNotificationContentText() {
      String copied = formatFileSize(CopyService.this, bytesCopied);
      String total = formatFileSize(CopyService.this, bytesTotal);
      return getString(R.string.copying_x_of_x_size, copied, total);
    }

    @Override String getNotificationContentInfo() {
      if (timeLeftMillis > 0) {
        return getTimeLeftString();
      }
      return super.getNotificationContentInfo();
    }

    private String getTimeLeftString() {
      long timeLeftSeconds = timeLeftMillis / 1000;
      String formatted = formatElapsedTime(timeLeftSeconds);
      if (formatted.charAt(0) == '0') {
        formatted = formatted.substring(1);
      }
      return getString(R.string.x_countdown, formatted);
    }

    @Override float getNotificationProgressPercentage() {
      return bytesCopied / (float) bytesTotal;
    }
  }
}
