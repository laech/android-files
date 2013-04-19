package l.files.trash;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.isExternalStorageEmulated;
import static java.util.Locale.ENGLISH;

import java.io.File;
import java.io.IOException;

import android.content.Context;

final class TrashHelper {

  private static final String TRASH_DIR_NAME = "Trash";
  private static final String EXTERNAL_DIRECTORY_PATH =
      getExternalStorageDirectory().getAbsolutePath().toLowerCase(ENGLISH);

  public static TrashHelper create(Context context) {
    return new TrashHelper(
        context.getDir(TRASH_DIR_NAME, MODE_PRIVATE),
        context.getExternalFilesDir(TRASH_DIR_NAME));
  }

  private final File internalTrashDir;
  private final File externalTrashDir;

  TrashHelper(File internalTrashDir, File externalTrashDir) {
    this.internalTrashDir = internalTrashDir;
    this.externalTrashDir = externalTrashDir;
  }

  public File moveToTrash(File file) throws IOException {
    File trashFile = getTrashFile(file);
    if (!file.renameTo(trashFile))
      throw new IOException("Failed to move to trash: " + file);
    return trashFile;
  }

  private File getTrashFile(File file) {
    File trashDir = getTrashDir(file);
    String name = file.getName();
    File trashFile = new File(trashDir, name);
    for (int i = 1; trashFile.exists(); i++)
      trashFile = new File(trashDir, name + " " + i);
    return trashFile;
  }

  private File getTrashDir(File file) {
    return isExternalFile(file) ? externalTrashDir : internalTrashDir;
  }

  private boolean isExternalFile(File file) {
    return isExternalStorageEmulated() ? false
        : file.getAbsolutePath().toLowerCase(ENGLISH).startsWith(EXTERNAL_DIRECTORY_PATH);
  }
}
