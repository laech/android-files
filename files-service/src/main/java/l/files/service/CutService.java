package l.files.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static java.util.Collections.emptySet;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFilesSet;

public final class CutService extends ProgressService {

  private static final String EXTRA_SOURCES = "l.files.intent.extra.SOURCES";
  private static final String EXTRA_DESTINATION = "l.files.intent.extra.DESTINATION";

  public static void start(Context context, Set<File> sources, File destination) {
    context.startService(new Intent(context, CutService.class)
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


  private final class CutTask extends Task<Object, Void, Set<File>> {

    private final Set<File> sources;
    private final File destination;

    CutTask(int id, Set<File> sources, File destination) {
      super(id, CutService.this);
      this.sources = sources;
      this.destination = destination;
    }

    @Override protected Set<File> doTask() {
      try {
        return new FilesCutter(this, sources, destination).executeAndGetFailures();
      } catch (IOException e) {
        Log.e(CutService.class.getSimpleName(), e.getMessage(), e); // TODO
        return emptySet();
      }
    }

    @Override protected void onPostExecute(Set<File> files) {
      super.onPostExecute(files);
      if (!files.isEmpty()) {
        CopyService.start(CutService.this, files, destination, true);
      }
    }

    @Override protected int getNotificationSmallIcon() {
      return R.drawable.ic_stat_notify_cut;
    }

    @Override protected String getNotificationContentTitle() {
      return getString(R.string.moving_to_x, destination.getName());
    }
  }
}
