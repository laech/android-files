package l.files.ui.menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.Resource;
import l.files.ui.FileCreationFragment;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.IOExceptions.message;

public final class NewDirFragment extends FileCreationFragment
{

    public static final String TAG = NewDirFragment.class.getSimpleName();

    static NewDirFragment create(final Resource resource)
    {
        final Bundle bundle = new Bundle(1);
        bundle.putParcelable(ARG_PARENT_RESOURCE, resource);

        final NewDirFragment fragment = new NewDirFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getNameSuggestion();
    }

    private void getNameSuggestion()
    {
        final Resource parent = getParent();
        final String basename = getString(R.string.untitled_dir);
        new AsyncTask<Object, Object, String>()
        {
            @Override
            protected String doInBackground(final Object... params)
            {
                Resource resource = parent.resolve(basename);
                try
                {
                    for (int i = 2; resource.exists(NOFOLLOW); i++)
                    {
                        resource = parent.resolve(basename + " " + i);
                    }
                }
                catch (final IOException e)
                {
                    return "";
                }
                return resource.name();
            }

            @Override
            protected void onPostExecute(final String name)
            {
                super.onPostExecute(name);
                final EditText field = getFilenameField();
                if (field.getText().length() == 0)
                {
                    field.setText(name);
                    field.selectAll();
                }
            }
        }.execute();
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which)
    {
        createDirectory();
    }

    private void createDirectory()
    {
        final Activity context = getActivity();
        final Resource resource = getParent().resolve(getFilename());
        new AsyncTask<Void, Void, IOException>()
        {
            @Override
            protected IOException doInBackground(final Void... params)
            {
                try
                {
                    resource.createDirectory();
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
                    makeText(context, message(e), LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    protected int getTitleResourceId()
    {
        return R.string.new_dir;
    }
}
