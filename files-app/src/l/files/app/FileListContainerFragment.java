package l.files.app;

import static android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.View.OnKeyListener;
import static l.files.app.FilesApp.getBus;
import static l.files.app.Fragments.setArgs;
import static l.files.app.format.Formats.label;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;

public final class FileListContainerFragment extends Fragment
    implements OnKeyListener, OnBackStackChangedListener {

  public static interface DrawableToggleActivity {
    ActionBarDrawerToggle getActionBarDrawerToggle();
  }

  public static final String ARG_DIRECTORY = FilesFragment.ARG_DIRECTORY;

  public static FileListContainerFragment create(File dir) {
    return setArgs(new FileListContainerFragment(), ARG_DIRECTORY, dir.getAbsolutePath());
  }

  Bus bus;
  Toaster toaster;
  Consumer<File> fileOpener;
  FragmentManager manager;

  private boolean inActionMode;

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    FrameLayout layout = new FrameLayout(getActivity());
    layout.setId(android.R.id.content);
    return layout;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bus = getBus(this);
    toaster = Toaster.get();
    fileOpener = FileOpener.get(getActivity());
    manager = getChildFragmentManager();
    manager.addOnBackStackChangedListener(this);
    if (savedInstanceState == null) {
      File dir = new File(getArguments().getString(ARG_DIRECTORY));
      FilesFragment fragment = FilesFragment.create(dir);
      manager
          .beginTransaction()
          .add(android.R.id.content, fragment, FilesFragment.TAG)
          .commit();
    }
  }

  @Override public void onResume() {
    super.onResume();
    bus.register(this);
    getView().setFocusableInTouchMode(true);
    getView().requestFocus();
    getView().setOnKeyListener(this);
    updateActivityActionBar();
  }

  @Override public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override public void onBackStackChanged() {
    updateActivityActionBar();
  }

  @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (keyCode == KEYCODE_BACK
        && event.getAction() == ACTION_UP
        && manager.getBackStackEntryCount() > 0
        && !inActionMode) {
      manager.popBackStack();
      return true;
    }
    return false;
  }

  @Subscribe public void handle(ActionModeEvent event) {
    inActionMode = event == ActionModeEvent.START;
  }

  @Subscribe public void handle(OpenFileRequest request) {
    File file = request.value();
    if (!file.canRead()) {
      showPermissionDenied();
    } else if (file.isDirectory()) {
      showDirectory(file);
    } else {
      showFile(file);
    }
  }

  @Subscribe public void handle(OnHomePressedEvent event) {
    manager.popBackStack();
  }

  private void updateActivityActionBar() {
    FilesFragment fragment = findCurrentFragment();
    File dir = fragment.directory();
    ActionBar actionBar = getActivity().getActionBar();
    actionBar.setTitle(label(getResources()).apply(dir));
    if (getActivity() instanceof DrawableToggleActivity) {
      ((DrawableToggleActivity) getActivity())
          .getActionBarDrawerToggle()
          .setDrawerIndicatorEnabled(manager.getBackStackEntryCount() == 0);
    }
  }

  private void showPermissionDenied() {
    toaster.toast(getActivity(), R.string.permission_denied);
  }

  private void showDirectory(File dir) {
    FilesFragment current = findCurrentFragment();
    if (current != null && current.directory().equals(dir)) {
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
        .setTransition(TRANSIT_FRAGMENT_OPEN)
        .commit();
  }

  private void showFile(File file) {
    fileOpener.apply(file);
  }

  private FilesFragment findCurrentFragment() {
    return (FilesFragment) manager.findFragmentByTag(FilesFragment.TAG);
  }
}
