package l.files.ui.mode;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.operations.Events;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.FileCreationFragment;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.lang.System.identityHashCode;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class RenameFragment extends FileCreationFragment {

    public static final String TAG = RenameFragment.class.getSimpleName();

    private static final String ARG_RESOURCE = "resource";

    private static final int LOADER_FILE = identityHashCode(RenameFragment.class);

    private LoaderCallbacks<ResourceStatus> fileCallback = new NameHighlighter();

    static RenameFragment create(Resource resource) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_PARENT_RESOURCE, resource.getParent());
        args.putParcelable(ARG_RESOURCE, resource);
        RenameFragment fragment = new RenameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getFilename().isEmpty()) {
            getLoaderManager().restartLoader(LOADER_FILE, null, fileCallback);
        }
    }

    @Override
    protected CharSequence getError(Resource target) {
        if (getResource().equals(target)) {
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
        final Context context = getActivity();
        new AsyncTask<Void, Void, IOException>() {

            @Override
            protected IOException doInBackground(Void... params) {
                try {
                    Resource dst = getParent().resolve(getFilename());
                    getResource().moveTo(dst);
                    return null;
                } catch (IOException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(IOException e) {
                super.onPostExecute(e);
                if (e != null) {
                    String message = context.getString(R.string.failed_to_rename_file_x, e.getMessage());
                    makeText(context, message, LENGTH_SHORT).show();
                }
            }
        }.execute();
        Events.get().post(CloseActionModeRequest.INSTANCE);
    }

    private Resource getResource() {
        return getArguments().getParcelable(ARG_RESOURCE);
    }

    class NameHighlighter implements LoaderCallbacks<ResourceStatus> {

        @Override
        public Loader<ResourceStatus> onCreateLoader(int id, Bundle bundle) {
            return onCreateFileLoader();
        }

        private Loader<ResourceStatus> onCreateFileLoader() {
            return new AsyncTaskLoader<ResourceStatus>(getActivity()) {
                @Override
                public ResourceStatus loadInBackground() {
                    try {
                        return getResource().readStatus(NOFOLLOW);
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
        public void onLoadFinished(Loader<ResourceStatus> loader, ResourceStatus stat) {
            onFileLoaded(stat);
        }

        @Override
        public void onLoaderReset(Loader<ResourceStatus> loader) {
        }

        private void onFileLoaded(ResourceStatus stat) {
            if (stat == null || !getFilename().isEmpty()) {
                return;
            }
            EditText field = getFilenameField();
            field.setText(stat.getName());
            if (stat.isDirectory()) {
                field.selectAll();
            } else {
                field.setSelection(0, getNameWithoutExtension(stat.getName()).length());
            }
        }
    }
}
