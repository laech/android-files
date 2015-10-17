package l.files.ui.browser;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.ActionMode;
import android.widget.EditText;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.R;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.base.fs.IOExceptions.message;

public final class RenameFragment extends FileCreationFragment {

    public static final String TAG = RenameFragment.class.getSimpleName();

    private static final String ARG_FILE = "file";

    static RenameFragment create(File file) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_PARENT_FILE, file.parent());
        args.putParcelable(ARG_FILE, file);

        RenameFragment fragment = new RenameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private AsyncTask<?, ?, ?> highlight;
    private AsyncTask<?, ?, ?> rename;

    @Override
    public void onDestroy() {
        super.onDestroy();

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

    private File file() {
        return getArguments().getParcelable(ARG_FILE);
    }

    private void highlight() {
        if (getFilename().isEmpty()) {
            highlight = new Highlight()
                    .executeOnExecutor(THREAD_POOL_EXECUTOR, file());
        }
    }

    private class Highlight extends AsyncTask<File, Void, Pair<File, Stat>> {

        @Override
        protected Pair<File, Stat> doInBackground(File... params) {
            File file = params[0];
            try {
                return Pair.create(file, file.stat(NOFOLLOW));
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Pair<File, Stat> pair) {
            super.onPostExecute(pair);
            if (pair != null) {
                File file = pair.first;
                Stat stat = pair.second;
                EditText field = getFilenameField();
                if (!getFilename().isEmpty()) {
                    return;
                }
                field.setText(file.name());
                if (stat.isDirectory()) {
                    field.selectAll();
                } else {
                    field.setSelection(0, file.name().base().length());
                }
            }
        }
    }

    @Override
    protected CharSequence getError(File target) {
        if (file().equals(target)) {
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
        File dst = parent().resolve(getFilename());
        rename = new Rename(file(), dst)
                .executeOnExecutor(THREAD_POOL_EXECUTOR);

        ActionMode mode = ((BaseActivity) getActivity()).currentActionMode();
        if (mode != null) {
            mode.finish();
        }
    }

    private class Rename extends AsyncTask<File, Void, IOException> {

        private final File src;
        private final File dst;

        private Rename(File src, File dst) {
            this.src = requireNonNull(src);
            this.dst = requireNonNull(dst);
        }

        @Override
        protected IOException doInBackground(File... params) {
            try {
                src.moveTo(dst);
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
