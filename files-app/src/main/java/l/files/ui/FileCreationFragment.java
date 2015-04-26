package l.files.ui;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.R;
import l.files.fs.Resource;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static java.lang.System.identityHashCode;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class FileCreationFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    public static final String ARG_PARENT_RESOURCE = "parent";

    private static final int LOADER_CHECKER =
            identityHashCode(FileCreationFragment.class);

    private LoaderCallbacks<Existence> checkerCallback = new CheckerCallback();
    private EditText editText;

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getFilename().isEmpty()) {
            getOkButton().setEnabled(false);
        } else {
            restartChecker();
        }
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        editText = createEditText();
        return new AlertDialog.Builder(getActivity())
                .setTitle(getTitleResourceId())
                .setView(editText)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    protected CharSequence getError(Resource target) {
        return getString(R.string.name_exists);
    }

    protected abstract int getTitleResourceId();

    private EditText createEditText() {
        final EditText text = new EditText(getActivity());
        text.setId(android.R.id.text1);
        text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
        text.setSingleLine();
        text.addTextChangedListener(new FileTextWatcher());
        return text;
    }

    @Override
    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    private void restartChecker() {
        getLoaderManager().restartLoader(LOADER_CHECKER, null, checkerCallback);
    }

    protected Resource getParent() {
        return getArguments().getParcelable(ARG_PARENT_RESOURCE);
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
            final Resource resource = getParent().resolve(getFilename());
            return new AsyncTaskLoader<Existence>(getActivity()) {
                @Override
                public Existence loadInBackground() {
                    try {
                        return new Existence(resource, resource.exists(NOFOLLOW));
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
                editText.setError(getError(existence.resource));
                ok.setEnabled(false);
            } else {
                editText.setError(null);
                ok.setEnabled(true);
            }
        }
    }

    private static final class Existence {
        final Resource resource;
        final boolean exists;

        Existence(Resource resource, boolean exists) {
            this.resource = resource;
            this.exists = exists;
        }
    }

    class FileTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(
                CharSequence s, int start, int before, int count) {
            if (getFilename().isEmpty()) {
                getOkButton().setEnabled(false);
            } else {
                restartChecker();
            }
        }

        @Override
        public void beforeTextChanged(
                CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
