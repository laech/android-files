package l.files.ui.browser.action;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import l.files.base.Consumer;
import l.files.ui.base.app.BaseActivity;
import l.files.ui.browser.FileCreationFragment;
import l.files.ui.browser.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static java.nio.file.Files.move;
import static java.nio.file.Files.readAttributes;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Objects.requireNonNull;
import static l.files.fs.PathKt.getBaseName;

public final class RenameFragment extends FileCreationFragment {

    public static final String TAG = RenameFragment.class.getSimpleName();

    private static final String ARG_PATH = "path";

    static RenameFragment create(Path path) {
        Bundle args = new Bundle(2);
        args.putString(ARG_PARENT_PATH, path.getParent().toString());
        args.putString(ARG_PATH, path.toString());

        RenameFragment fragment = new RenameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private AsyncTask<?, ?, ?> highlight;

    // TODO use AbsolutePath

    private Path path;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        path = Paths.get(args.getString(ARG_PATH));
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
        String oldName = Optional.ofNullable(path.getFileName())
            .map(Path::toString)
            .orElse("");
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
            highlight = new Highlight(this)
                .executeOnExecutor(THREAD_POOL_EXECUTOR, path());
        }
    }

    private static final class Highlight
        extends AsyncTask<Path, Void, Pair<Path, BasicFileAttributes>> {

        private final WeakReference<RenameFragment> fragmentRef;

        Highlight(RenameFragment fragment) {
            this.fragmentRef = new WeakReference<>(fragment);
        }

        @Nullable
        @Override
        protected Pair<Path, BasicFileAttributes> doInBackground(Path... params) {
            Path path = params[0];
            try {
                return Pair.create(
                    path,
                    readAttributes(
                        path,
                        BasicFileAttributes.class,
                        NOFOLLOW_LINKS
                    )
                );
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(
            @Nullable Pair<Path, BasicFileAttributes> pair
        ) {
            super.onPostExecute(pair);
            if (isCancelled()) {
                return;
            }
            RenameFragment fragment = fragmentRef.get();
            if (fragment == null) {
                return;
            }
            if (pair != null) {
                Path path = pair.first;
                BasicFileAttributes attrs = pair.second;
                EditText field = fragment.getFilenameField();
                if (!fragment.getFilename().isEmpty()) {
                    return;
                }
                field.setText(Optional.ofNullable(path.getFileName())
                    .map(Path::toString)
                    .orElse(""));
                if (attrs.isDirectory()) {
                    field.selectAll();
                } else {
                    field.setSelection(
                        0,
                        Optional.ofNullable(getBaseName(path))
                            .orElse("")
                            .length()
                    );
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
        Path dst = Paths.get(parent().toString(), getFilename());
        new Rename(
            toaster,
            path(),
            dst
        ).executeOnExecutor(THREAD_POOL_EXECUTOR);

        BaseActivity activity = (BaseActivity) getActivity();
        assert activity != null;
        ActionMode mode = activity.currentActionMode();
        if (mode != null) {
            mode.finish();
        }
    }

    private static final class Rename
        extends AsyncTask<Path, Void, IOException> {

        private final Consumer<String> toaster;
        private final Path src;
        private final Path dst;

        private Rename(Consumer<String> toaster, Path src, Path dst) {
            this.toaster = requireNonNull(toaster);
            this.src = requireNonNull(src);
            this.dst = requireNonNull(dst);
        }

        @Nullable
        @Override
        protected IOException doInBackground(Path... params) {
            try {
                move(src, dst);
                return null;
            } catch (IOException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(@Nullable IOException e) {
            super.onPostExecute(e);
            if (e != null) {
                Log.d(TAG, "", e);
                toaster.accept(e.toString());
            }
        }

    }

}
