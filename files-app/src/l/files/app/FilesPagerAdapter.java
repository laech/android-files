package l.files.app;

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.io.File;

final class FilesPagerAdapter extends FragmentPagerAdapter {

  private final File dir;

  public FilesPagerAdapter(FragmentManager fm, File dir) {
    super(fm);
    this.dir = checkNotNull(dir, "dir");
  }

  @Override public Fragment getItem(int position) {
    return FileListContainerFragment.create(dir);
  }

  @Override public int getCount() {
    return 1;
  }
}
