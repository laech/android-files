package l.files.trash;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.getExternalStorageDirectory;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.io.FileUtils.moveDirectory;
import static org.apache.commons.io.FileUtils.moveFile;

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
    if (file.isDirectory()) {
      moveDirectory(file, trashFile);
    } else {
      moveFile(file, trashFile);
    }
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
    return file.getAbsolutePath().toLowerCase(ENGLISH).startsWith(EXTERNAL_DIRECTORY_PATH);
  }
}
