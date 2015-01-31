package l.files.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;
import l.files.fs.Path;

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

  Toaster toaster;
  Consumer<Path> fileOpener;
  FragmentManager manager;

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

  public String getCurrentTitle() {
    if (manager.getBackStackEntryCount() == 0) {
      return getInitialTitle();
    }
    return manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1)
        .getBreadCrumbTitle().toString();
  }

  private Path getInitialPath() {
    return getArguments().getParcelable(ARG_INITIAL_PATH);
  }

  public String getInitialTitle() {
    return getArguments().getString(ARG_INITIAL_TITLE);
  }

  public void show(OpenFileRequest request) {
    if (getActivity() == null) {
      return;
    }
    if (!request.file().isReadable()) {
      showPermissionDenied();
    } else if (request.file().isDirectory()) {
      showDirectory(request);
    } else {
      showFile(request);
    }
  }

  private void showPermissionDenied() {
    toaster.toast(getActivity(), R.string.permission_denied);
  }

  private void showDirectory(OpenFileRequest request) {
    Path path = request.file().path();
    FilesFragment current = findCurrentFragment();
    if (current != null && current.getPath().equals(path)) {
      return;
    }
    FilesFragment fragment = FilesFragment.create(path);
    manager
        .beginTransaction()
        .replace(android.R.id.content, fragment, FilesFragment.TAG)
        .addToBackStack(null)
        .setBreadCrumbTitle(request.file().name())
        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .commit();
  }

  private void showFile(OpenFileRequest request) {
    fileOpener.apply(request.file().path());
  }

  private FilesFragment findCurrentFragment() {
    return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
  }
}
