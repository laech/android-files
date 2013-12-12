package l.files.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CancellationException;

import l.files.analytics.Analytics;

import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFilesSet;
import static l.files.service.PasteType.COPY;

public final class CopyService extends ProgressService {

  private static final String EXTRA_SOURCES = "sources";
  private static final String EXTRA_DESTINATION = "destination";

  public static void start(
      Context context, Set<File> sources, File destination) {
    context.startService(new Intent(context, CopyService.class)
        .putExtra(EXTRA_SOURCES, toAbsolutePaths(sources))
        .putExtra(EXTRA_DESTINATION, destination.getAbsolutePath()));
  }

  @Override protected Task<?, ?, ?> newTask(Intent intent, int id) {
    Set<File> sources = getSources(intent);
    File destination = getDestination(intent);
    return new CopyTask(id, sources, destination);
  }

  private File getDestination(Intent intent) {
    return new File(intent.getStringExtra(EXTRA_DESTINATION));
  }

  private Set<File> getSources(Intent intent) {
    return toFilesSet(intent.getStringArrayExtra(EXTRA_SOURCES));
  }

  private final class CopyTask extends PasteTask {

    CopyTask(int id, Set<File> sources, File destination) {
      super(id, CopyService.this, COPY, sources, destination);
    }

    @Override protected Void doTask() {
      try {

        Counter.Result result = new Counter(this, src).call();
        new Copier(this, src, dst, result.count, result.length).call();
      } catch (CancellationException e) {
        return null;
      } catch (IOException e) {
        Analytics.onException(CopyService.this, e);
        Log.e(CopyService.class.getSimpleName(), e.getMessage(), e); // TODO
      }

      return null;
    }
  }
}
