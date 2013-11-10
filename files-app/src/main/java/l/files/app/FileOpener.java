package l.files.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.Toaster;

import static android.content.Intent.ACTION_VIEW;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.FilesContract.getFileSystemUri;

final class FileOpener implements Consumer<Uri> {

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
    this.context = checkNotNull(context, "context");
    this.toaster = checkNotNull(toaster, "toaster");
    this.executor = checkNotNull(executor, "executor");
  }

  @Override public void apply(Uri uri) {
    executor.execute(new ShowFileTask(uri));
  }

  class ShowFileTask extends AsyncTask<Void, Void, String> {
    private final Uri uri;

    ShowFileTask(Uri uri) {
      this.uri = uri;
    }

    @Override protected String doInBackground(Void... params) {
      return context.getContentResolver().getType(uri);
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
      context.startActivity(new Intent(ACTION_VIEW)
          .setDataAndType(getFileSystemUri(uri), media));
    }
  }
}
