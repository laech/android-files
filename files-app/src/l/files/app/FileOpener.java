package l.files.app;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.app.Intents.viewFile;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.AsyncTask;
import com.google.common.net.MediaType;
import java.io.File;
import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.io.Detector;
import l.files.common.io.Detectors;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.Toaster;

final class FileOpener implements Consumer<File> {

  public static FileOpener get(Context context) {
    return new FileOpener(
        context, Detectors.get(), Toaster.get(), AsyncTaskExecutor.DEFAULT);
  }

  private final Context context;
  private final Detector detector;
  private final Toaster toaster;
  private final AsyncTaskExecutor executor;

  FileOpener(
      Context context,
      Detector detector,
      Toaster toaster,
      AsyncTaskExecutor executor) {
    this.context = checkNotNull(context, "context");
    this.detector = checkNotNull(detector, "detector");
    this.toaster = checkNotNull(toaster, "toaster");
    this.executor = checkNotNull(executor, "executor");
  }

  @Override public void apply(File file) {
    executor.execute(new ShowFileTask(file));
  }

  class ShowFileTask extends AsyncTask<Void, Void, MediaType> {
    private final File file;

    ShowFileTask(File file) {
      this.file = file;
    }

    @Override protected MediaType doInBackground(Void... params) {
      return detector.detect(file);
    }

    @Override protected void onPostExecute(MediaType type) {
      try {
        showFile(type);
      } catch (ActivityNotFoundException e) {
        showActivityNotFound();
      }
    }

    private void showActivityNotFound() {
      toaster.toast(context, R.string.no_app_to_open_file);
    }

    private void showFile(MediaType type) throws ActivityNotFoundException {
      context.startActivity(viewFile(file, type != null ? type : OCTET_STREAM));
    }
  }
}