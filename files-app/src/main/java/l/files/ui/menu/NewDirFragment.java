package l.files.ui.menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.Resource;
import l.files.ui.FileCreationFragment;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public final class NewDirFragment extends FileCreationFragment {

    public static final String TAG = NewDirFragment.class.getSimpleName();

    static NewDirFragment create(Resource resource) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_PARENT_RESOURCE, resource);

        NewDirFragment fragment = new NewDirFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNameSuggestion();
    }

    private void getNameSuggestion() {
        final Resource parent = getParent();
        final String basename = getString(R.string.untitled_dir);
        new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object... params) {
                Resource resource = parent.resolve(basename);
                for (int i = 2; resource.exists(); i++) {
                    resource = parent.resolve(basename + " " + i);
                }
                return resource.getName();
            }

            @Override
            protected void onPostExecute(String name) {
                super.onPostExecute(name);
                EditText field = getFilenameField();
                if (field.getText().length() == 0) {
                    field.setText(name);
                    field.selectAll();
                }
            }
        }.execute();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        createDirectory();
    }

    private void createDirectory() {
        final Activity context = getActivity();
        final Resource resource = getParent().resolve(getFilename());
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    resource.createDirectory();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (!success) {
                    makeText(context, R.string.mkdir_failed, LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.new_dir;
    }
}
