package l.files.ui.browser.menu;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.ui.browser.FileCreationFragment;
import l.files.ui.browser.R;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.base.fs.IOExceptions.message;

public final class NewDirFragment extends FileCreationFragment {

    public static final String TAG = NewDirFragment.class.getSimpleName();

    static NewDirFragment create(Path file) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_PARENT_PATH, file);

        NewDirFragment fragment = new NewDirFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    private AsyncTask<?, ?, ?> suggestion;

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (suggestion != null) {
            suggestion.cancel(true);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        suggestName();
    }

    private void suggestName() {
        String name = getString(R.string.untitled_dir);
        Path base = parent().concat(name);
        suggestion = new SuggestName().executeOnExecutor(THREAD_POOL_EXECUTOR, base);
    }

    private class SuggestName extends AsyncTask<Path, Void, Path> {

        @Nullable
        @Override
        protected Path doInBackground(Path... params) {
            // TODO use AbsolutePath
            Path base = params[0];
            String baseName = base.getName().or(""); // TODO deal with ""
            Path parent = base.parent();
            assert parent != null;
            Path file = base;
            try {
                for (int i = 2; file.exists(NOFOLLOW); i++) {
                    if (isCancelled()) {
                        return null;
                    }
                    file = parent.concat(baseName + " " + i);
                }
                return file;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable Path result) {
            super.onPostExecute(result);
            if (result == null) {
                set("");
            } else {
                set(result.getName().or("")); // TODO deal with ""
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
        createDir(parent().concat(getFilename()));
    }

    private void createDir(Path dir) {
        // Don't cancel this onDestroy for directory to be created
        new CreateDir(dir).executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private class CreateDir extends AsyncTask<Path, Void, IOException> {

        private final Path dir;

        private CreateDir(Path dir) {
            this.dir = requireNonNull(dir);
        }

        @Nullable
        @Override
        protected IOException doInBackground(Path... params) {
            try {
                dir.createDirectory();
                return null;
            } catch (IOException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(@Nullable IOException e) {
            super.onPostExecute(e);
            if (e != null) {
                assert toaster != null;
                toaster.accept(message(e));
            }
        }

    }

    @Override
    protected int getTitleResourceId() {
        return R.string.new_dir;
    }
}
