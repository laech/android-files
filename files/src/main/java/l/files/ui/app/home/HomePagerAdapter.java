package l.files.ui.app.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import l.files.ui.app.files.FilesFragment;
import l.files.ui.app.sidebar.SidebarFragment;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

public class HomePagerAdapter extends FragmentPagerAdapter {

  public static final int POSITION_SIDEBAR = 0;
  public static final int POSITION_FILES = 1;

  private final boolean portrait;
  private final File dir;

  public HomePagerAdapter(FragmentManager fm, File dir, boolean portrait) {
    super(fm);
    this.dir = checkNotNull(dir, "dir");
    this.portrait = portrait;
  }

  @Override public float getPageWidth(int position) {
    switch (position) {
      case POSITION_SIDEBAR:
        return portrait ? 0.618f : 0.382f;
      default:
        return super.getPageWidth(position);
    }
  }

  @Override public Fragment getItem(int position) {
    switch (position) {
      case POSITION_FILES:
        return FilesFragment.create(dir);
      default:
        return new SidebarFragment();
    }
  }

  @Override public int getCount() {
    return 2;
  }

}
