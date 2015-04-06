package l.files.ui.browser;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.common.net.MediaType;

import java.io.IOException;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.Toaster;
import l.files.fs.Path;
import l.files.logging.Logger;

import static android.content.Intent.ACTION_VIEW;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.util.Objects.requireNonNull;
import static l.files.BuildConfig.DEBUG;

final class FileOpener implements Consumer<Path> {

  private static final Logger log = Logger.get(FileOpener.class);

  public static FileOpener get(Context context) {
    return new FileOpener(
        context, Toaster.get(), AsyncTaskExecutor.DEFAULT);
  }

  private final Context context;
  private final Toaster toaster;
  private final AsyncTaskExecutor executor;

  FileOpener(
      Context context,
      Toaster toaster,
      AsyncTaskExecutor executor) {
    this.context = requireNonNull(context, "context");
    this.toaster = requireNonNull(toaster, "toaster");
    this.executor = requireNonNull(executor, "executor");
  }

  @Override public void apply(Path path) {
    executor.execute(new ShowFileTask(path));
  }

  class ShowFileTask extends AsyncTask<Void, Void, MediaType> {
    private final Path path;

    ShowFileTask(Path path) {
      this.path = path;
    }

    @Override protected MediaType doInBackground(Void... params) {
      try {
        return path.getResource().detectMediaType();
      } catch (IOException e) {
        log.warn(e);
        return null;
      }
    }

    @Override protected void onPostExecute(MediaType media) {
      if (media == null) {
        showFailedToGetFileInfo();
        return;
      }
      try {
        showFile(media);
      } catch (ActivityNotFoundException e) {
        showActivityNotFound();
      }
      debug(media);
    }

    public void showFailedToGetFileInfo() {
      toaster.toast(context, R.string.failed_to_get_file_info);
    }

    private void showActivityNotFound() {
      toaster.toast(context, R.string.no_app_to_open_file);
    }

    private void showFile(MediaType media) throws ActivityNotFoundException {
      context.startActivity(new Intent(ACTION_VIEW)
          .setDataAndType(Uri.parse(path.getUri().toString()), media.toString()));
    }

    private void debug(MediaType media) {
      if (DEBUG) makeText(context, "[DEBUG] " + media, LENGTH_SHORT).show();
    }
  }
}
