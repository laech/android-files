package l.files.ui.browser;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.logging.Logger;
import l.files.ui.OpenFileRequest;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.widget.Toast.LENGTH_SHORT;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.ui.Fragments.setArgs;
import static l.files.ui.browser.IOExceptions.getFailureMessage;

public final class FilesPagerFragment extends Fragment {

    private static final Logger log = Logger.get(FilesPagerFragment.class);

    private static final String ARG_INITIAL_DIRECTORY = "directory";
    private static final String ARG_INITIAL_TITLE = "title";

    public static FilesPagerFragment create(Resource directory, String title) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_INITIAL_DIRECTORY, directory);
        args.putString(ARG_INITIAL_TITLE, title);
        return setArgs(new FilesPagerFragment(), args);
    }

    private Consumer<Resource> fileOpener;
    private FragmentManager manager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout layout = new FrameLayout(getActivity());
        layout.setId(android.R.id.content);
        return layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileOpener = FileOpener.get(getActivity());
        manager = getChildFragmentManager();
        if (savedInstanceState == null) {
            FilesFragment fragment = FilesFragment.create(getInitialDirectory());
            manager
                    .beginTransaction()
                    .replace(android.R.id.content, fragment, FilesFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(hasMenu);
        FilesFragment fragment = findCurrentFragment();
        if (fragment != null) {
            fragment.setHasOptionsMenu(hasMenu);
        }
    }

    public boolean popBackStack() {
        try {
            return getChildFragmentManager().popBackStackImmediate();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public boolean hasBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    public Resource getCurrentDirectory() {
        FilesFragment fragment = findCurrentFragment();
        if (fragment == null) {
            return getInitialDirectory();
        }
        return fragment.getDirectory();
    }

    private Resource getInitialDirectory() {
        return getArguments().getParcelable(ARG_INITIAL_DIRECTORY);
    }

    public void show(final OpenFileRequest request) {
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... params) {
                try {
                    return request.getResource().readStatus(FOLLOW);
                } catch (IOException e) {
                    log.debug(e, "%s", request);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                Activity activity = getActivity();
                if (activity != null) {
                    if (result instanceof ResourceStatus) {
                        show(request.getResource(), (ResourceStatus) result);
                    } else {
                        String msg = getFailureMessage((IOException) result);
                        Toast.makeText(activity, msg, LENGTH_SHORT).show();
                    }
                }
            }
        }.execute();
    }

    private void show(Resource resource, ResourceStatus status) {
        if (getActivity() == null) {
            return;
        }
        if (!isReadable(resource)) { // TODO Check in background
            showPermissionDenied();
        } else if (status.isDirectory()) {
            showDirectory(resource);
        } else {
            showFile(resource);
        }
    }

    private boolean isReadable(Resource resource) {
        try {
            return resource.isReadable();
        } catch (IOException e) {
            return false;
        }
    }

    private void showPermissionDenied() {
        Toast.makeText(getActivity(), R.string.permission_denied, LENGTH_SHORT).show();
    }

    private void showDirectory(Resource resource) {
        FilesFragment current = findCurrentFragment();
        if (current != null && current.getDirectory().equals(resource)) {
            return;
        }
        FilesFragment fragment = FilesFragment.create(resource);
        manager
                .beginTransaction()
                .replace(android.R.id.content, fragment, FilesFragment.TAG)
                .addToBackStack(null)
                .setBreadCrumbTitle(resource.getName())
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void showFile(Resource resource) {
        fileOpener.apply(resource);
    }

    private FilesFragment findCurrentFragment() {
        return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
    }
}
