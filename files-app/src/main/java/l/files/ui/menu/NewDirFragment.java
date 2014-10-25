package l.files.ui.menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import l.files.R;
import l.files.ui.FileCreationFragment;
import l.files.provider.FilesContract;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.ui.Fragments.setArgs;

public final class NewDirFragment extends FileCreationFragment {

  public static final String TAG = NewDirFragment.class.getSimpleName();

  static NewDirFragment create(String parentLocation) {
    return setArgs(new NewDirFragment(), ARG_PARENT_LOCATION, parentLocation);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getNameSuggestion();
  }

  private void getNameSuggestion() {
    final String location = getParentLocation();
    final String basename = getString(R.string.untitled_dir);
    final Activity context = getActivity();
    new AsyncTask<Object, Object, String>() {
      @Override protected String doInBackground(Object... params) {
        return FilesContract.getNameSuggestion(context, location, basename);
      }

      @Override protected void onPostExecute(String name) {
        super.onPostExecute(name);
        EditText field = getFilenameField();
        if (field.getText().length() == 0) {
          field.setText(name);
          field.selectAll();
        }
      }
    }.execute();
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    createDirectory();
  }

  private void createDirectory() {
    final Activity context = getActivity();
    final String parentLocation = getParentLocation();
    final String filename = getFilename();
    new AsyncTask<Void, Void, Boolean>() {
      @Override protected Boolean doInBackground(Void... params) {
        return FilesContract.createDirectory(context, parentLocation, filename);
      }

      @Override protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (!success) {
          makeText(context, R.string.mkdir_failed, LENGTH_SHORT).show();
        }
      }
    }.execute();
  }

  @Override protected int getTitleResourceId() {
    return R.string.new_dir;
  }
}
