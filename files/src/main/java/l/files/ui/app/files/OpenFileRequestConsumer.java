package l.files.ui.app.files;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.AsyncTask;
import com.google.common.base.Function;
import com.google.common.net.MediaType;
import java.io.File;
import l.files.R;
import l.files.base.Consumer;
import l.files.event.OpenFileRequest;
import l.files.io.Detectors;
import l.files.ui.util.Toaster;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.ui.app.files.Intents.viewDir;
import static l.files.ui.app.files.Intents.viewFile;

final class OpenFileRequestConsumer implements Consumer<OpenFileRequest> {

  public static OpenFileRequestConsumer get(Context context) {
    return new OpenFileRequestConsumer(
        context, Detectors.get(), Toaster.get(), AsyncTaskExecutor.DEFAULT);
  }

  private final Context context;
  private final Function<File, MediaType> detector;
  private final Toaster toaster;
  private final AsyncTaskExecutor executor;

  OpenFileRequestConsumer(
      Context context,
      Function<File, MediaType> detector,
      Toaster toaster,
      AsyncTaskExecutor executor) {
    this.context = checkNotNull(context, "context");
    this.detector = checkNotNull(detector, "detector");
    this.toaster = checkNotNull(toaster, "toaster");
    this.executor = checkNotNull(executor, "executor");
  }

  @Override public void take(OpenFileRequest request) {
    File file = request.file();
    if (!file.canRead()) {
      showPermissionDenied();
    } else if (file.isDirectory()) {
      showDir(file);
    } else {
      showFile(file);
    }
  }

  private void showPermissionDenied() {
    toaster.toast(context, R.string.permission_denied);
  }

  private void showDir(File dir) {
    context.startActivity(viewDir(dir, context));
  }

  private void showFile(final File file) {
    executor.execute(new ShowFileTask(file));
  }

  class ShowFileTask extends AsyncTask<Void, Void, MediaType> {
    private final File file;

    ShowFileTask(File file) {
      this.file = file;
    }

    @Override protected MediaType doInBackground(Void... params) {
      return detector.apply(file);
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
