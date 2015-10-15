package l.files.ui.newdir;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import l.files.fs.File;
import l.files.ui.FileCreationFragment;
import l.files.ui.R;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.base.fs.FileIntents.broadcastFilesChanged;
import static l.files.ui.base.fs.IOExceptions.message;

public final class NewDirFragment extends FileCreationFragment {

    public static final String TAG = NewDirFragment.class.getSimpleName();

    static NewDirFragment create(File file) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_PARENT_FILE, file);

        NewDirFragment fragment = new NewDirFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private AsyncTask<?, ?, ?> suggestion;
    private AsyncTask<?, ?, ?> creation;

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (suggestion != null) {
            suggestion.cancel(true);
        }
        if (creation != null) {
            creation.cancel(true);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        suggestName();
    }

    private void suggestName() {
        String name = getString(R.string.untitled_dir);
        File base = parent().resolve(name);
        suggestion = new SuggestName().executeOnExecutor(THREAD_POOL_EXECUTOR, base);
    }

    private class SuggestName extends AsyncTask<File, Void, File> {

        @Override
        protected File doInBackground(File... params) {
            File base = params[0];
            String baseName = base.name().toString();
            File parent = base.parent();
            assert parent != null;
            File file = base;
            try {
                for (int i = 2; file.exists(NOFOLLOW); i++) {
                    if (isCancelled()) {
                        return null;
                    }
                    file = parent.resolve(baseName + " " + i);
                }
                return file;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            if (result == null) {
                set("");
            } else {
                set(result.name().toString());
            }
        }

        private void set(String name) {
            EditText field = getFilenameField();
            boolean notChanged = field.getText().length() == 0;
            if (notChanged) {
                field.setText(name);
                field.selectAll();
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        createDir(parent().resolve(getFilename()));
    }

    private void createDir(File dir) {
        creation = new CreateDir(dir).executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private class CreateDir extends AsyncTask<File, Void, IOException> {

        private final File dir;

        private CreateDir(File dir) {
            this.dir = requireNonNull(dir);
        }

        @Override
        protected IOException doInBackground(File... params) {
            try {
                dir.createDir();
                return null;
            } catch (IOException e) {
                return e;
            }
        }

        @Override
        protected void onCancelled(IOException e) {
            if (e == null) {
                broadcastChange();
            }
        }

        @Override
        protected void onPostExecute(IOException e) {
            super.onPostExecute(e);
            if (e != null) {
                toaster.apply(message(e));
            } else {
                broadcastChange();
            }
        }

        private void broadcastChange() {
            broadcastFilesChanged(singleton(dir), getActivity());
        }
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.new_dir;
    }
}