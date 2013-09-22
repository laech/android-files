package l.files.app;

import static l.files.app.Fragments.setArgs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.io.File;
import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;

public final class FilesPagerFragment extends Fragment {

  public static final String ARG_DIRECTORY = FilesFragment.ARG_DIRECTORY;

  public static FilesPagerFragment create(File dir) {
    return setArgs(new FilesPagerFragment(), ARG_DIRECTORY, dir.getAbsolutePath());
  }

  Toaster toaster;
  Consumer<File> fileOpener;
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
      FilesFragment fragment = FilesFragment.create(getInitialDirectory());
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
    return getChildFragmentManager().popBackStackImmediate();
  }

  public boolean hasBackStack() {
    return getChildFragmentManager().getBackStackEntryCount() > 0;
  }

  public File getCurrentDirectory() {
    FilesFragment fragment = findCurrentFragment();
    if (fragment == null) {
      return getInitialDirectory();
    }
    return fragment.getDirectory();
  }

  private File getInitialDirectory() {
    return new File(getArguments().getString(ARG_DIRECTORY));
  }

  public void show(File file) {
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
    if (current != null && current.getDirectory().equals(dir)) {
      return;
    }
    FilesFragment fragment = FilesFragment.create(dir);
    manager
        .beginTransaction()
        /*
         * Replacing the content meaning that on pressing back, the previous fragment
         * will be recreated showing the latest directory content and latest modified
         * timestamp for the files. If this approach is changed - i.e. on pressing back
         * no longer recreates the previous fragment, instead just show the previously
         * existed fragment with old contents, then those content may be out dated and
         * need to be refreshed/re-sorted.
         */
        .replace(android.R.id.content, fragment, FilesFragment.TAG)
        .addToBackStack(null)
//        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .commit();
  }

  private void showFile(File file) {
    fileOpener.apply(file);
  }

  private FilesFragment findCurrentFragment() {
    return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
  }
}
