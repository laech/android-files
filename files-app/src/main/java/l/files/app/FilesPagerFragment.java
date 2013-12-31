package l.files.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import l.files.R;
import l.files.analytics.Analytics;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;

import static android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static l.files.app.Fragments.setArgs;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.buildFileUri;

public final class FilesPagerFragment extends Fragment {

  private static final String ARG_INITIAL_DIRECTORY_LOCATION = "initial_directory_location";
  private static final String ARG_INITIAL_DIRECTORY_NAME = "initial_directory_name";

  /**
   * @param initialDirectoryLocation the {@link FileInfo#LOCATION} of the
   * initial directory to show
   * @param initialDirectoryName the name of the initial directory to show
   */
  public static FilesPagerFragment create(
      String initialDirectoryLocation, String initialDirectoryName) {
    Bundle args = new Bundle(2);
    args.putString(ARG_INITIAL_DIRECTORY_LOCATION, initialDirectoryLocation);
    args.putString(ARG_INITIAL_DIRECTORY_NAME, initialDirectoryName);
    return setArgs(new FilesPagerFragment(), args);
  }

  Toaster toaster;
  Consumer<Uri> fileOpener;
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
      FilesFragment fragment = FilesFragment.create(getInitialDirectoryLocation());
      manager
          .beginTransaction()
          .add(android.R.id.content, fragment, FilesFragment.TAG)
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

  /**
   * Gets the {@link FileInfo#LOCATION} of the directory that is currently
   * showing.
   */
  public String getCurrentDirectoryLocation() {
    FilesFragment fragment = findCurrentFragment();
    if (fragment == null) {
      return getInitialDirectoryLocation();
    }
    return fragment.getDirectoryLocation();
  }

  public String getCurrentDirectoryName() {
    if (manager.getBackStackEntryCount() == 0) {
      return getInitialDirectoryName();
    }
    return manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1)
        .getBreadCrumbTitle().toString();
  }

  /**
   * Gets the {@link FileInfo#LOCATION} of the initial showing directory of this
   * fragment.
   */
  private String getInitialDirectoryLocation() {
    return getArguments().getString(ARG_INITIAL_DIRECTORY_LOCATION);
  }

  public String getInitialDirectoryName() {
    return getArguments().getString(ARG_INITIAL_DIRECTORY_NAME);
  }

  public void show(OpenFileRequest request) {
    if (getActivity() == null) {
      return;
    }
    if (!request.canRead()) {
      showPermissionDenied();
    } else if (request.isDirectory()) {
      showDirectory(request);
    } else {
      showFile(request);
    }
  }

  private void showPermissionDenied() {
    toaster.toast(getActivity(), R.string.permission_denied);
  }

  private void showDirectory(OpenFileRequest request) {
    String location = request.fileLocation();
    FilesFragment current = findCurrentFragment();
    if (current != null && current.getDirectoryLocation().equalsIgnoreCase(location)) {
      return;
    }
    FilesFragment fragment = FilesFragment.create(location);
    manager
        .beginTransaction()
        .replace(android.R.id.content, fragment, FilesFragment.TAG)
        .addToBackStack(null)
        .setBreadCrumbTitle(request.filename())
        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .commit();

    Analytics.onEvent(getActivity(), "files", "open_directory");
  }

  private void showFile(OpenFileRequest request) {
    fileOpener.apply(buildFileUri(request.fileLocation()));
  }

  private FilesFragment findCurrentFragment() {
    return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
  }
}
