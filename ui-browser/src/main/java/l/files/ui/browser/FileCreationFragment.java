package l.files.ui.browser;

import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.IOException;

import l.files.fs.File;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static java.lang.System.identityHashCode;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class FileCreationFragment extends DialogFragment
        implements OnClickListener {

    public static final String ARG_PARENT_FILE = "parent";

    private static final int LOADER_CHECKER =
            identityHashCode(FileCreationFragment.class);

    private final LoaderCallbacks<Existence> checkerCallback =
            new CheckerCallback();

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

        if (editText == null) {
            editText = (EditText) getDialog().findViewById(android.R.id.text1);
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
                .inflate(R.layout.file_name, null, false);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getTitleResourceId())
                .setView(view)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    protected CharSequence getError(File target) {
        return getString(R.string.name_exists);
    }

    protected abstract int getTitleResourceId();

    @Override
    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    private void restartChecker() {
        getLoaderManager().restartLoader(LOADER_CHECKER, null, checkerCallback);
    }

    protected File parent() {
        return getArguments().getParcelable(ARG_PARENT_FILE);
    }

    protected String getFilename() {
        return editText.getText().toString();
    }

    protected EditText getFilenameField() {
        return editText;
    }

    private Button getOkButton() {
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
            final File file = parent().resolve(getFilename());
            return new AsyncTaskLoader<Existence>(getActivity()) {
                @Override
                public Existence loadInBackground() {
                    try {
                        boolean exists = file.exists(NOFOLLOW);
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
                editText.setError(getError(existence.file));
                ok.setEnabled(false);
            } else {
                editText.setError(null);
                ok.setEnabled(true);
            }
        }
    }

    private static class Existence {
        File file;
        boolean exists;

        Existence(File file, boolean exists) {
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
