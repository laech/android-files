package l.files.ui.browser.action;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Pair;
import android.widget.EditText;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.app.BaseActivity;
import l.files.ui.browser.FileCreationFragment;
import l.files.ui.browser.R;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.base.fs.IOExceptions.message;

public final class RenameFragment extends FileCreationFragment {

    public static final String TAG = RenameFragment.class.getSimpleName();

    private static final String ARG_PATH = "path";

    static RenameFragment create(Path path) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_PARENT_PATH, path.parent());
        args.putParcelable(ARG_PATH, path);

        RenameFragment fragment = new RenameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private AsyncTask<?, ?, ?> highlight;

    // TODO use AbsolutePath

    @Nullable
    private Path path;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getArguments().getParcelable(ARG_PATH);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (highlight != null) {
            highlight.cancel(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        highlight();
    }

    @Override
    protected void restartChecker() {

        assert path != null;
        String oldName = path.getName().or("");
        String newName = getFilename();
        if (!oldName.equals(newName) &&
                oldName.equalsIgnoreCase(newName)) {
            getOkButton().setEnabled(true);
            return;
        }

        // TODO check name does not contain invalid characters '/', '\u0000'

        super.restartChecker();
    }

    private Path path() {
        assert path != null;
        return path;
    }

    private void highlight() {
        if (getFilename().isEmpty()) {
            highlight = new Highlight()
                    .executeOnExecutor(THREAD_POOL_EXECUTOR, path());
        }
    }

    private class Highlight extends AsyncTask<Path, Void, Pair<Path, Stat>> {

        @Nullable
        @Override
        protected Pair<Path, Stat> doInBackground(Path... params) {
            Path path = params[0];
            try {
                return Pair.create(path, path.stat(NOFOLLOW));
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable Pair<Path, Stat> pair) {
            super.onPostExecute(pair);
            if (isCancelled()) {
                return;
            }
            if (pair != null) {
                Path path = pair.first;
                Stat stat = pair.second;
                EditText field = getFilenameField();
                if (!getFilename().isEmpty()) {
                    return;
                }
                field.setText(path.getName().or(""));
                if (stat.isDirectory()) {
                    field.selectAll();
                } else {
                    field.setSelection(0, path.getBaseName().or("").length());
                }
            }
        }
    }

    @Override
    protected CharSequence getError(Path target) {
        if (path().equals(target)) {
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
        Path dst = parent().concat(getFilename());
        new Rename(path(), dst).executeOnExecutor(THREAD_POOL_EXECUTOR);

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

        @Nullable
        @Override
        protected IOException doInBackground(Path... params) {
            try {
                src.rename(dst);
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

}
