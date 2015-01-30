package l.files.ui.mode;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.FileStatus;
import l.files.fs.Path;
import l.files.operations.Events;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.FileCreationFragment;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.lang.System.identityHashCode;
import static org.apache.commons.io.FilenameUtils.getBaseName;

public final class RenameFragment extends FileCreationFragment {

  public static final String TAG = RenameFragment.class.getSimpleName();

  private static final String ARG_PATH = "path";

  private static final int LOADER_FILE = identityHashCode(RenameFragment.class);

  private LoaderCallbacks<FileStatus> fileCallback = new NameHighlighter();

  static RenameFragment create(Path path) {
    Bundle args = new Bundle(2);
    args.putParcelable(ARG_PARENT_PATH, path.getParent());
    args.putParcelable(ARG_PATH, path);
    RenameFragment fragment = new RenameFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onStart() {
    super.onStart();
    if (getFilename().isEmpty()) {
      getLoaderManager().restartLoader(LOADER_FILE, null, fileCallback);
    }
  }

  @Override protected CharSequence getError(Path target) {
    if (getPath().equals(target)) {
      return null;
    }
    return super.getError(target);
  }

  @Override protected int getTitleResourceId() {
    return R.string.rename;
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    final Context context = getActivity();
    new AsyncTask<Void, Void, IOException>() {

      @Override protected IOException doInBackground(Void... params) {
        try {
          getPath().getResource().move(getParentPath().resolve(getFilename()));
          return null;
        } catch (IOException e) {
          return e;
        }
      }

      @Override protected void onPostExecute(IOException e) {
        super.onPostExecute(e);
        if (e != null) {
          String message = context.getString(R.string.failed_to_rename_file_x, e.getMessage());
          makeText(context, message, LENGTH_SHORT).show();
        }
      }
    }.execute();
    Events.get().post(CloseActionModeRequest.INSTANCE);
  }

  private Path getPath() {
    return getArguments().getParcelable(ARG_PATH);
  }

  class NameHighlighter implements LoaderCallbacks<FileStatus> {

    @Override public Loader<FileStatus> onCreateLoader(int id, Bundle bundle) {
      return onCreateFileLoader();
    }

    private Loader<FileStatus> onCreateFileLoader() {
      return new AsyncTaskLoader<FileStatus>(getActivity()) {
        @Override public FileStatus loadInBackground() {
          try {
            return getPath().getResource().stat();
          } catch (IOException e) {
            return null;
          }
        }

        @Override protected void onStartLoading() {
          super.onStartLoading();
          forceLoad();
        }
      };
    }

    @Override public void onLoadFinished(Loader<FileStatus> loader, FileStatus stat) {
      onFileLoaded(stat);
    }

    @Override public void onLoaderReset(Loader<FileStatus> loader) {}

    private void onFileLoaded(FileStatus stat) {
      if (stat == null || !getFilename().isEmpty()) {
        return;
      }
      EditText field = getFilenameField();
      field.setText(stat.name());
      if (stat.isDirectory()) {
        field.selectAll();
      } else {
        field.setSelection(0, getBaseName(stat.name()).length());
      }
    }
  }
}
