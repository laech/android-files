package l.files.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFilesSet;
import static l.files.service.PasteType.CUT;

public final class MoveService extends ProgressService {

  private static final String EXTRA_SOURCES = "l.files.intent.extra.SOURCES";
  private static final String EXTRA_DESTINATION = "l.files.intent.extra.DESTINATION";

  public static void start(Context context, Set<File> sources, File destination) {
    context.startService(new Intent(context, MoveService.class)
        .putExtra(EXTRA_SOURCES, toAbsolutePaths(sources))
        .putExtra(EXTRA_DESTINATION, destination.getAbsolutePath()));
  }

  @Override protected Task<?, ?, ?> newTask(Intent intent, int id) {
    return new CutTask(id, getSources(intent), getDestination(intent));
  }

  private File getDestination(Intent intent) {
    return new File(intent.getStringExtra(EXTRA_DESTINATION));
  }

  private Set<File> getSources(Intent intent) {
    return toFilesSet(intent.getStringArrayExtra(EXTRA_SOURCES));
  }


  private final class CutTask extends PasteTask implements Deleter.Listener {

    CutTask(int id, Set<File> src, File dst) {
      super(id, MoveService.this, CUT, src, dst);
    }

    @Override protected Void doTask() {
      try {
        Set<File> files = new Mover(this, src, dst).call();
        if (!files.isEmpty()) {
          Counter.Result result = new Counter(this, files).call();
          new Copier(this, files, dst, result.count, result.length).call();
          new Deleter(this, files, result.count).call();
        }
      } catch (IOException e) {
        Log.e(MoveService.class.getSimpleName(), e.getMessage(), e); // TODO
      }
      return null;
    }

    @Override public void onFileDeleted(int total, int remaining) {
      if (setAndGetUpdateProgress()) {
        publishProgress(new CleanProgress(total, remaining));
      }
    }
  }

  private final class CleanProgress extends DeleteBaseProgress {
    CleanProgress(int total, int remaining) {
      super(total, remaining);
    }

    @Override String getNotificationContentTitle() {
      return getResources().getString(R.string.cleaning_after_move);
    }
  }
}
