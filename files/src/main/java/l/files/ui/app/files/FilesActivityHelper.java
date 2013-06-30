package l.files.ui.app.files;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static l.files.ui.app.files.FilesActivity.EXTRA_DIRECTORY;

import java.io.File;

import l.files.R;
import l.files.io.MediaTypeDetector;
import l.files.io.MediaTypeDetectors;
import l.files.ui.event.FileSelectedEvent;
import l.files.ui.event.MediaDetectedEvent;
import l.files.ui.util.Toaster;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.common.net.MediaType;

public class FilesActivityHelper {

  public static final FilesActivityHelper INSTANCE = new FilesActivityHelper();

  private final Toaster toaster;
  private final MediaTypeDetector detector;

  FilesActivityHelper() {
    this(MediaTypeDetectors.getDefault(), Toaster.INSTANCE);
  }

  FilesActivityHelper(MediaTypeDetector detector, Toaster toaster) {
    this.toaster = toaster;
    this.detector = detector;
  }

  public void handle(FileSelectedEvent event, FilesActivity activity) {
    File file = event.file();
    if (!file.canRead()) {
      showPermissionDenied(activity);
    } else if (file.isDirectory()) {
      showDirectory(file, activity);
    } else {
      showFile(file, activity);
    }
  }

  private void showPermissionDenied(FilesActivity activity) {
    toaster.toast(activity, R.string.permission_denied, LENGTH_SHORT);
  }

  private void showDirectory(File directory, FilesActivity activity) {
    int requestCode = 0;
    Intent intent = newShowDirectoryIntent(directory, activity);
    activity.startActivityForResult(intent, requestCode);
  }

  private Intent newShowDirectoryIntent(File dir, FilesActivity activity) {
    return new Intent(activity, FilesActivity.class)
        .putExtra(EXTRA_DIRECTORY, dir.getAbsolutePath());
  }

  private void showFile(final File file, final FilesActivity activity) {
    new AsyncTask<Void, Void, MediaType>() {
      @Override protected MediaType doInBackground(Void... params) {
        return detector.apply(file);
      }

      @Override protected void onPostExecute(MediaType result) {
        showFile(file, result.toString(), activity);
      };
    }.execute();
  }

  public void handle(MediaDetectedEvent event, FilesActivity activity) {
    if (!event.mediaType().isPresent()) {
      toaster.toast(activity, R.string.unknown_file_type, LENGTH_SHORT);
    } else {
      showFile(event.file(), event.mediaType().get(), activity);
    }
  }

  private void showFile(File file, String mediaType, FilesActivity activity) {
    try {
      activity.startActivity(newShowFileIntent(file, mediaType));
    } catch (ActivityNotFoundException e) {
      toaster.toast(activity, R.string.no_app_to_open_file, LENGTH_SHORT);
    }
  }

  private Intent newShowFileIntent(File file, String mediaType) {
    return new Intent(ACTION_VIEW).setDataAndType(fromFile(file), mediaType);
  }
}