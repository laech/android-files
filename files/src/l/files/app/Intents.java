package l.files.app;

import android.content.Context;
import android.content.Intent;
import com.google.common.net.MediaType;

import java.io.File;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;

final class Intents {
  private Intents() {}

  public static Intent viewFile(File file, MediaType type) {
    return new Intent(ACTION_VIEW).setDataAndType(fromFile(file), type.toString());
  }

  public static Intent viewDir(File dir, Context context) {
    return new Intent(context, FilesActivity.class)
        .putExtra(FilesActivity.EXTRA_DIR, dir.getAbsolutePath());
  }
}
