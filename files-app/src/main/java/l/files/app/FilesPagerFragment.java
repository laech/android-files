package l.files.app;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;
import java.net.URI;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static l.files.app.Fragments.setArgs;
import static l.files.provider.FilesContract.buildFileUri;
import static l.files.provider.FilesContract.getFileId;

public final class FilesPagerFragment extends Fragment {

  public static final String ARG_DIRECTORY = FilesFragment.ARG_DIRECTORY_ID;

  public static FilesPagerFragment create(File dir) {
    return setArgs(new FilesPagerFragment(), ARG_DIRECTORY,
        dir.getAbsolutePath());
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
      FilesFragment fragment = FilesFragment.create(
          getFileId(getInitialDirectory()));
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

  public File getCurrentDirectory() {
    FilesFragment fragment = findCurrentFragment();
    if (fragment == null) {
      return getInitialDirectory();
    }
    return new File(URI.create(fragment.getDirectoryId()));
  }

  private File getInitialDirectory() {
    return new File(getArguments().getString(ARG_DIRECTORY));
  }

  public void show(File file) {
    if (getActivity() == null) {
      return;
    }
    if (!file.canRead()) {
      showPermissionDenied();
    } else if (file.isDirectory()) {
      showDirectory(file);
    } else {
      showFile(file);
    }
  }

  private void showPermissionDenied() {
    toaster.toast(getActivity(), R.string.permission_denied);
  }

  private void showDirectory(File dir) {
    FilesFragment current = findCurrentFragment();
    if (current != null && current.getDirectoryId().equals(getFileId(dir))) {
      return;
    }
    FilesFragment fragment = FilesFragment.create(getFileId(dir));
    manager
        .beginTransaction()
        .replace(android.R.id.content, fragment, FilesFragment.TAG)
        .addToBackStack(null)
        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .commit();
  }

  private void showFile(File file) {
    fileOpener.apply(buildFileUri(getFileId(file)));
  }

  private FilesFragment findCurrentFragment() {
    return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
  }
}
