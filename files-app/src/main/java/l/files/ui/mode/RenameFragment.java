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
import l.files.fs.Stat;
import l.files.operations.Events;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.FileCreationFragment;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.lang.System.identityHashCode;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class RenameFragment extends FileCreationFragment
{

    public static final String TAG = RenameFragment.class.getSimpleName();

    private static final String ARG_RESOURCE = "resource";

    private static final int LOADER_FILE = identityHashCode(RenameFragment.class);

    private final LoaderCallbacks<Stat> fileCallback = new NameHighlighter();

    static RenameFragment create(final Resource resource)
    {
        final Bundle args = new Bundle(2);
        args.putParcelable(ARG_PARENT_RESOURCE, resource.parent());
        args.putParcelable(ARG_RESOURCE, resource);
        final RenameFragment fragment = new RenameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (getFilename().isEmpty())
        {
            getLoaderManager().restartLoader(LOADER_FILE, null, fileCallback);
        }
    }

    @Override
    protected CharSequence getError(final Resource target)
    {
        if (getResource().equals(target))
        {
            return null;
        }
        return super.getError(target);
    }

    @Override
    protected int getTitleResourceId()
    {
        return R.string.rename;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which)
    {
        final Context context = getActivity();
        new AsyncTask<Void, Void, IOException>()
        {

            @Override
            protected IOException doInBackground(final Void... params)
            {
                try
                {
                    final Resource dst = parent().resolve(getFilename());
                    getResource().moveTo(dst);
                    return null;
                }
                catch (final IOException e)
                {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(final IOException e)
            {
                super.onPostExecute(e);
                if (e != null)
                {
                    final String message = context.getString(R.string.failed_to_rename_file_x, e.getMessage());
                    makeText(context, message, LENGTH_SHORT).show();
                }
            }
        }.execute();
        Events.get().post(CloseActionModeRequest.INSTANCE);
    }

    private Resource getResource()
    {
        return getArguments().getParcelable(ARG_RESOURCE);
    }

    class NameHighlighter implements LoaderCallbacks<Stat>
    {

        @Override
        public Loader<Stat> onCreateLoader(final int id, final Bundle bundle)
        {
            return onCreateFileLoader();
        }

        private Loader<Stat> onCreateFileLoader()
        {
            return new AsyncTaskLoader<Stat>(getActivity())
            {
                @Override
                public Stat loadInBackground()
                {
                    try
                    {
                        return getResource().stat(NOFOLLOW);
                    }
                    catch (final IOException e)
                    {
                        return null;
                    }
                }

                @Override
                protected void onStartLoading()
                {
                    super.onStartLoading();
                    forceLoad();
                }
            };
        }

        @Override
        public void onLoadFinished(final Loader<Stat> loader, final Stat stat)
        {
            onFileLoaded(stat);
        }

        @Override
        public void onLoaderReset(final Loader<Stat> loader)
        {
        }

        private void onFileLoaded(final Stat stat)
        {
            if (stat == null || !getFilename().isEmpty())
            {
                return;
            }
            final Resource resource = getResource();
            final EditText field = getFilenameField();
            field.setText(resource.name());
            if (stat.isDirectory())
            {
                field.selectAll();
            }
            else
            {
                field.setSelection(0, getNameWithoutExtension(resource.name()).length());
            }
        }
    }
}
