package l.files.ui.browser.menu;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.annotation.Nullable;
import l.files.base.Consumer;
import l.files.ui.browser.FileCreationFragment;
import l.files.ui.browser.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static l.files.base.Objects.requireNonNull;

public final class NewDirFragment extends FileCreationFragment {

    public static final String TAG = NewDirFragment.class.getSimpleName();

    static NewDirFragment create(Path file) {
        Bundle bundle = new Bundle(1);
        bundle.putString(ARG_PARENT_PATH, file.toString());

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
        Path base = Paths.get(parent().toString(), name);
        suggestion =
            new SuggestName(this).executeOnExecutor(THREAD_POOL_EXECUTOR, base);
    }

    private static final class SuggestName extends AsyncTask<Path, Void, Path> {

        private final WeakReference<NewDirFragment> fragmentRef;

        SuggestName(NewDirFragment fragment) {
            this.fragmentRef = new WeakReference<>(fragment);
        }


        @Override
        protected Path doInBackground(Path... params) {
            // TODO use AbsolutePath
            Path base = params[0];
            String baseName = Optional.ofNullable(base.getFileName())
                .map(Path::toString)
                .orElse(""); // TODO deal with ""
            Path parent = base.getParent();
            Path file = base;
            for (int i = 2; exists(file, NOFOLLOW_LINKS); i++) {
                if (isCancelled()) {
                    return null;
                }
                file = parent.resolve(baseName + " " + i);
            }
            return file;
        }

        @Override
        protected void onPostExecute(@Nullable Path result) {
            super.onPostExecute(result);
            if (result == null) {
                set("");
            } else {
                set(Optional.ofNullable(result.getFileName())
                    .map(Path::toString)
                    .orElse("")); // TODO deal with ""
            }
        }

        private void set(String name) {
            NewDirFragment fragment = fragmentRef.get();
            if (fragment == null) {
                return;
            }
            EditText field = fragment.getFilenameField();
            boolean notChanged = field.getText().length() == 0;
            if (notChanged) {
                field.setText(name);
                field.selectAll();
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        createDir(Paths.get(parent().toString(), getFilename()));
    }

    private void createDir(Path dir) {
        // Don't cancel this onDestroy for directory to be created
        new CreateDir(toaster, dir).executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private static final class CreateDir
        extends AsyncTask<Path, Void, IOException> {

        private final Consumer<String> toaster;
        private final Path dir;

        CreateDir(Consumer<String> toaster, Path dir) {
            this.toaster = requireNonNull(toaster);
            this.dir = requireNonNull(dir);
        }

        @Nullable
        @Override
        protected IOException doInBackground(Path... params) {
            try {
                createDirectory(dir);
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

    @Override
    protected int getTitleResourceId() {
        return R.string.new_dir;
    }
}
