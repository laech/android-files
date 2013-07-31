package l.files.ui.app.files.menu;

import android.support.v4.app.FragmentManager;
import com.squareup.otto.Bus;
import l.files.common.app.OptionsMenu;

import java.io.File;

public final class Menus {
  private Menus() {}

  public static OptionsMenu newBookmarkMenu(Bus bus, File dir) {
    return new BookmarkMenu(bus, dir);
  }

  public static OptionsMenu newSortMenu(FragmentManager manager) {
    return new SortMenu(manager, SortDialog.CREATOR);
  }

  public static OptionsMenu newDirMenu(File parent) {
    return new NewDirMenu(parent);
  }

  public static OptionsMenu newShowHiddenFilesMenu(Bus bus) {
    return new ShowHiddenFilesMenu(bus);
  }
}
