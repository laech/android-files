package l.files.ui.browser;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Pair;
import android.widget.EditText;

import java.io.IOException;

import l.files.fs.Files;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.base.fs.IOExceptions.message;

public final class RenameFragment extends FileCreationFragment {

    public static final String TAG = RenameFragment.class.getSimpleName();

    private static final String ARG_FILE = "file";

    static RenameFragment create(Path directory, Name file) {
        Bundle args = new Bundle(3);
        args.putParcelable(ARG_PARENT_PATH, directory);
        args.putParcelable(ARG_FILE, file);

        RenameFragment fragment = new RenameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private AsyncTask<?, ?, ?> highlight;
    private AsyncTask<?, ?, ?> rename;
    private Path path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Name file = getArguments().getParcelable(ARG_FILE);
        path = parent().resolve(file);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // TODO cancel on dialog cancel
        if (highlight != null) {
            highlight.cancel(true);
        }
        if (rename != null) {
            rename.cancel(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        highlight();
    }

    @Override
    void restartChecker() {

        String oldName = path.name().toString();
        String newName = getFilename();
        if (!oldName.equals(newName) &&
                oldName.equalsIgnoreCase(newName)) {
            getOkButton().setEnabled(true);
            return;
        }

        super.restartChecker();
    }

    private void highlight() {
        if (getFilename().isEmpty()) {
            highlight = new Highlight()
                    .executeOnExecutor(THREAD_POOL_EXECUTOR, path);
        }
    }

    private class Highlight extends AsyncTask<Path, Void, Pair<Path, Stat>> {

        @Override
        protected Pair<Path, Stat> doInBackground(Path... params) {
            Path path = params[0];
            try {
                return Pair.create(path, Files.stat(path, NOFOLLOW));
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Pair<Path, Stat> pair) {
            super.onPostExecute(pair);
            if (pair != null) {
                Path path = pair.first;
                Stat stat = pair.second;
                EditText field = getFilenameField();
                if (!getFilename().isEmpty()) {
                    return;
                }
                field.setText(path.name().toString());
                if (stat.isDirectory()) {
                    field.selectAll();
                } else {
                    field.setSelection(0, path.name().base().length());
                }
            }
        }
    }

    @Override
    protected CharSequence getError(Path target) {
        if (path.equals(target)) {
            return null;
        }
        return super.getError(target);
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.rename;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        rename();
    }

    private void rename() {
        Path dst = parent().resolve(getFilename());
        rename = new Rename(path, dst)
                .executeOnExecutor(THREAD_POOL_EXECUTOR);

        ActionMode mode = ((BaseActivity) getActivity()).currentActionMode();
        if (mode != null) {
            mode.finish();
        }
    }

    private class Rename extends AsyncTask<Path, Void, IOException> {

        private final Path src;
        private final Path dst;

        private Rename(Path src, Path dst) {
            this.src = requireNonNull(src);
            this.dst = requireNonNull(dst);
        }

        @Override
        protected IOException doInBackground(Path... params) {
            try {
                Files.move(src, dst);
                return null;
            } catch (IOException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(IOException e) {
            super.onPostExecute(e);
            if (e != null) {
                toaster.apply(message(e));
            }
        }

    }

}
