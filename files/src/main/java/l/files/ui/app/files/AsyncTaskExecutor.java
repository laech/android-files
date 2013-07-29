package l.files.ui.app.files;

import android.os.AsyncTask;

interface AsyncTaskExecutor {

  public static AsyncTaskExecutor DEFAULT = new AsyncTaskExecutor() {
    @Override public <Params> void execute(AsyncTask<Params, ?, ?> task, Params... params) {
      task.execute(params);
    }
  };

  <Params> void execute(AsyncTask<Params, ?, ?> task, Params... params);
}
