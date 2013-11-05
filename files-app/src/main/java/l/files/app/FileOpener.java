package l.files.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.io.Detector;
import l.files.common.io.Detectors;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.Toaster;

import static android.content.Intent.ACTION_VIEW;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.IOUtils.closeQuietly;

final class FileOpener implements Consumer<String> {

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

  @Override public void apply(String fileUri) {
    executor.execute(new ShowFileTask(fileUri));
  }

  class ShowFileTask extends AsyncTask<Void, Void, String> {
    private final String fileUri;

    ShowFileTask(String fileUri) {
      this.fileUri = fileUri;
    }

    @Override protected String doInBackground(Void... params) {
      InputStream stream = null;
      try {
        stream = new FileInputStream(new File(URI.create(fileUri)));
        return detector.detect(stream);
      } catch (IOException e) {
        return null;
      } finally {
        closeQuietly(stream);
      }
    }

    @Override protected void onPostExecute(String media) {
      if (media == null) {
        showFailedToGetFileInfo();
        return;
      }
      try {
        showFile(media);
      } catch (ActivityNotFoundException e) {
        showActivityNotFound();
      }
    }

    public void showFailedToGetFileInfo() {
      toaster.toast(context, R.string.failed_to_get_file_info);
    }

    private void showActivityNotFound() {
      toaster.toast(context, R.string.no_app_to_open_file);
    }

    private void showFile(String media) throws ActivityNotFoundException {
      context.startActivity(
          new Intent(ACTION_VIEW)
              .setDataAndType(Uri.parse(fileUri), media));
    }
  }
}
