package l.files.service;

import android.content.Context;
import android.content.Intent;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.text.NumberFormat;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFiles;

public final class DeleteService extends ProgressService {

  private static final String EXTRA_FILE_PATHS = "l.files.intent.extra.FILE_PATHS";

  private NumberFormat numberFormat;

  public static void delete(Context context, Set<File> files) {
    context.startService(new Intent(context, DeleteService.class)
        .putExtra(EXTRA_FILE_PATHS, toAbsolutePaths(files)));
  }

  @Override public void onCreate() {
    super.onCreate();
    numberFormat = NumberFormat.getIntegerInstance();
  }

  @Override protected Task<?, ?, ?> newTask(Intent intent, int id) {
    return new Preparation(id, ImmutableSet.copyOf(getFiles(intent)));
  }

  private File[] getFiles(Intent intent) {
    return toFiles(newHashSet(intent.getStringArrayExtra(EXTRA_FILE_PATHS)));
  }

  private final class Preparation extends FileCounter {

    Preparation(int id, Set<File> files) {
      super(id, DeleteService.this, files);
    }

    @Override protected String getContentTitle() {
      return getString(R.string.preparing_to_delete);
    }

    @Override protected String getContentText(Result[] values) {
      return getString(R.string.preparing_to_delete_x_items,
          numberFormat.format(values[0].filesCount()));
    }
  }
}
