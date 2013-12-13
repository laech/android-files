package l.files.app;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import l.files.R;
import l.files.analytics.Analytics;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static l.files.app.Fragments.setArgs;
import static l.files.provider.FilesContract.buildFileUri;

public final class FilesPagerFragment extends Fragment {

  // TODO
  public static final String ARG_DIRECTORY = FilesFragment.ARG_DIRECTORY_ID;
  public static final String ARG_DIR_NAME = "dir_name";

  public static FilesPagerFragment create(String dirId, String dirName) {
    Bundle args = new Bundle(2);
    args.putString(ARG_DIRECTORY, dirId);
    args.putString(ARG_DIR_NAME, dirName);
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
      FilesFragment fragment = FilesFragment.create(getInitialDirectoryId());
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

  public String getCurrentDirectoryId() {
    FilesFragment fragment = findCurrentFragment();
    if (fragment == null) {
      return getInitialDirectoryId();
    }
    return fragment.getDirectoryId();
  }

  public String getCurrentDirectoryName() {
    if (manager.getBackStackEntryCount() == 0) {
      return getInitialDirectoryName();
    }
    return manager.getBackStackEntryAt(manager.getBackStackEntryCount() - 1)
        .getBreadCrumbTitle().toString();
  }

  private String getInitialDirectoryId() {
    return getArguments().getString(ARG_DIRECTORY);
  }

  public String getInitialDirectoryName() {
    return getArguments().getString(ARG_DIR_NAME);
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
    String fileId = request.fileId();
    FilesFragment current = findCurrentFragment();
    if (current != null && current.getDirectoryId().equals(fileId)) {
      return;
    }
    FilesFragment fragment = FilesFragment.create(fileId);
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
    fileOpener.apply(buildFileUri(request.fileId()));
  }

  private FilesFragment findCurrentFragment() {
    return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
  }
}
