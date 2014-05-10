package l.files.service;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import l.files.io.file.operations.Copy;
import l.files.io.file.operations.Count;

import static android.os.SystemClock.elapsedRealtime;
import static l.files.service.PasteType.COPY;
import static l.files.service.PasteType.CUT;
import static l.files.service.Util.showErrorMessage;

abstract class PasteTask
    extends ProgressService.Task<Object, Progress, IOException>
    implements Count.Listener, Copy.Listener {

  private static final Map<PasteType, Integer> ICONS = ImmutableMap.of(
      CUT, R.drawable.ic_stat_notify_cut,
      COPY, R.drawable.ic_stat_notify_copy
  );

  private static final Map<PasteType, Integer> TITLES = ImmutableMap.of(
      CUT, R.string.preparing_to_move_to_x,
      COPY, R.string.preparing_to_copy_to_x
  );

  final PasteType type;
  final Set<File> src;
  final File dst;
  final String dstName;

  private long pasteStartTime;

  PasteTask(int id, ProgressService service, PasteType type, Set<File> src, File dst) {
    super(id, service);
    this.type = type;
    this.src = src;
    this.dst = dst;
    this.dstName = dst.getName();
  }

  @Override protected void onPostExecute(IOException e) {
    super.onPostExecute(e);
    if (e != null) {
      showErrorMessage(service, e);
    }
  }

  @Override protected int getNotificationSmallIcon() {
    return ICONS.get(type);
  }

  @Override protected String getNotificationContentTitle() {
    return service.getString(TITLES.get(type), dst.getName());
  }

  @Override protected String getNotificationContentTitle(Progress progress) {
    return progress.getNotificationContentTitle();
  }

  @Override protected String getNotificationContentText(Progress progress) {
    return progress.getNotificationContentText();
  }

  @Override protected String getNotificationContentInfo(Progress progress) {
    return progress.getNotificationContentInfo();
  }

  @Override
  protected float getNotificationProgressPercentage(Progress progress) {
    return progress.getNotificationProgressPercentage();
  }

  @Override public void onFileCounted(int count, long length) {
    if (setAndGetUpdateProgress()) {
      publishProgress(
          new PastePrepareProgress(type, service.getResources(), dstName, count));
    }
  }

  @Override
  public void onCopied(int remaining, long bytesCopied, long bytesTotal) {
    if (pasteStartTime <= 0) {
      pasteStartTime = elapsedRealtime();
    }
    if (setAndGetUpdateProgress()) {
      long currentTime = elapsedRealtime();
      float speed = bytesCopied / (float) (currentTime - pasteStartTime);
      long timeLeft = (long) ((bytesTotal - bytesCopied) / speed);
      publishProgress(new PasteProgress(type, service, dstName,
          remaining, bytesCopied, bytesTotal, timeLeft));
    }
  }
}
