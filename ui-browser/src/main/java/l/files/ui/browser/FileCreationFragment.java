package l.files.ui.browser;

import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.IOException;

import l.files.fs.Files;
import l.files.fs.Path;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static java.lang.System.identityHashCode;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class FileCreationFragment extends DialogFragment
        implements OnClickListener {

    public static final String ARG_PARENT_PATH = "parent";

    private static final int LOADER_CHECKER =
            identityHashCode(FileCreationFragment.class);

    private final LoaderCallbacks<Existence> checkerCallback =
            new CheckerCallback();

    private TextInputLayout layout;
    private EditText editText;

    public Consumer<String> toaster;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toaster = new Toaster(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (layout == null) {
            layout = (TextInputLayout) getDialog().findViewById(R.id.text_layout);
            layout.setHint(getString(getTitleResourceId()));
        }

        if (editText == null) {
            editText = (EditText) getDialog().findViewById(R.id.file_name);
            editText.setFilters(new InputFilter[]{new LengthFilter(255)});
            editText.addTextChangedListener(new FileTextWatcher());
            editText.setOnEditorActionListener(new OkActionListener());
        }

        if (getFilename().isEmpty()) {
            getOkButton().setEnabled(false);
        } else {
            restartChecker();
        }

        getDialog().getWindow()
                .setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.file_name, (ViewGroup) getView(), false);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    protected CharSequence getError(Path target) {
        return getString(R.string.name_exists);
    }

    protected abstract int getTitleResourceId();

    @Override
    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    void restartChecker() {
        getLoaderManager().restartLoader(LOADER_CHECKER, null, checkerCallback);
    }

    protected Path parent() {
        return getArguments().getParcelable(ARG_PARENT_PATH);
    }

    protected String getFilename() {
        return editText.getText().toString();
    }

    protected EditText getFilenameField() {
        return editText;
    }

    Button getOkButton() {
        return getDialog().getButton(BUTTON_POSITIVE);
    }

    class CheckerCallback implements LoaderCallbacks<Existence> {

        @Override
        public Loader<Existence> onCreateLoader(int id, Bundle bundle) {
            if (id == LOADER_CHECKER) {
                return newChecker();
            }
            return null;
        }

        private Loader<Existence> newChecker() {
            final Path file = parent().resolve(getFilename());
            return new AsyncTaskLoader<Existence>(getActivity()) {
                @Override
                public Existence loadInBackground() {
                    try {
                        boolean exists = Files.exists(file, NOFOLLOW);
                        return new Existence(file, exists);
                    } catch (IOException e) {
                        return null;
                    }
                }

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    forceLoad();
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Existence> loader, Existence existence) {
            if (loader.getId() == LOADER_CHECKER) {
                onCheckFinished(existence);
            }
        }

        @Override
        public void onLoaderReset(Loader<Existence> loader) {
        }

        private void onCheckFinished(@Nullable Existence existence) {
            if (existence == null) {
                return;
            }
            Button ok = getOkButton();
            if (existence.exists) {
                layout.setError(getError(existence.file));
                ok.setEnabled(false);
            } else {
                layout.setError(null);
                ok.setEnabled(true);
            }
        }
    }

    private static class Existence {
        final Path file;
        final boolean exists;

        Existence(Path file, boolean exists) {
            this.file = file;
            this.exists = exists;
        }
    }

    class FileTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (getFilename().isEmpty()) {
                getOkButton().setEnabled(false);
            } else {
                restartChecker();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private class OkActionListener implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(
                TextView v, int actionId, KeyEvent event) {

            if (actionId == IME_ACTION_DONE) {
                AlertDialog dialog = getDialog();
                onClick(dialog, BUTTON_POSITIVE);
                dialog.dismiss();
                return true;
            }

            return false;
        }

    }

}
