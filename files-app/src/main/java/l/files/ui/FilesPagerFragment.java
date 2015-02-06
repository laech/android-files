package l.files.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;
import l.files.fs.Path;
import l.files.fs.ResourceStatus;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static l.files.ui.Fragments.setArgs;

public final class FilesPagerFragment extends Fragment {

  private static final String ARG_INITIAL_PATH = "path";
  private static final String ARG_INITIAL_TITLE = "title";

  public static FilesPagerFragment create(Path path, String title) {
    Bundle args = new Bundle(2);
    args.putParcelable(ARG_INITIAL_PATH, path);
    args.putString(ARG_INITIAL_TITLE, title);
    return setArgs(new FilesPagerFragment(), args);
  }

  private Toaster toaster;
  private Consumer<Path> fileOpener;
  private FragmentManager manager;

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    FrameLayout layout = new FrameLayout(getActivity());
    layout.setId(android.R.id.content);
    return layout;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    toaster = Toaster.get();
    fileOpener = FileOpener.get(getActivity());
    manager = getChildFragmentManager();
    if (savedInstanceState == null) {
      FilesFragment fragment = FilesFragment.create(getInitialPath());
      manager
          .beginTransaction()
          .replace(android.R.id.content, fragment, FilesFragment.TAG)
          .commit();
    }
  }

  @Override public void setHasOptionsMenu(boolean hasMenu) {
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

  public Path getCurrentPath() {
    FilesFragment fragment = findCurrentFragment();
    if (fragment == null) {
      return getInitialPath();
    }
    return fragment.getPath();
  }

  private Path getInitialPath() {
    return getArguments().getParcelable(ARG_INITIAL_PATH);
  }

  public void show(final OpenFileRequest request) {
    new AsyncTask<Void, Void, ResourceStatus>() {
      @Override protected ResourceStatus doInBackground(Void... params) {
        try {
          return request.getPath().getResource().readStatus(true);
        } catch (IOException e) {
          return null;
        }
      }

      @Override protected void onPostExecute(ResourceStatus status) {
        super.onPostExecute(status);
        Activity activity = getActivity();
        if (activity != null) {
          if (status != null) {
            show(status);
          } else {
            toaster.toast(activity, R.string.failed_to_get_file_info);
          }
        }
      }
    }.execute();
  }

  private void show(ResourceStatus status) {
    if (getActivity() == null) {
      return;
    }
    if (!status.getIsReadable()) {
      showPermissionDenied();
    } else if (status.getIsDirectory()) {
      showDirectory(status.getPath());
    } else {
      showFile(status.getPath());
    }
  }

  private void showPermissionDenied() {
    toaster.toast(getActivity(), R.string.permission_denied);
  }

  private void showDirectory(Path path) {
    FilesFragment current = findCurrentFragment();
    if (current != null && current.getPath().equals(path)) {
      return;
    }
    FilesFragment fragment = FilesFragment.create(path);
    manager
        .beginTransaction()
        .replace(android.R.id.content, fragment, FilesFragment.TAG)
        .addToBackStack(null)
        .setBreadCrumbTitle(path.getName())
        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .commit();
  }

  private void showFile(Path path) {
    fileOpener.apply(path);
  }

  private FilesFragment findCurrentFragment() {
    return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
  }
}
