package l.files.app.menu;

import android.support.v4.app.FragmentManager;
import com.squareup.otto.Bus;
import java.io.File;
import l.files.common.app.OptionsMenu;

public final class Menus {
  private Menus() {}

  public static OptionsMenu newBookmarkMenu(Bus bus, File dir) {
    return new BookmarkMenu(bus, dir);
  }

  public static OptionsMenu newSortMenu(FragmentManager manager) {
    return new SortMenu(manager);
  }

  public static OptionsMenu newDirMenu(File parent) {
    return new NewDirMenu(parent);
  }

  public static OptionsMenu newShowHiddenFilesMenu(Bus bus) {
    return new ShowHiddenFilesMenu(bus);
  }

  public static OptionsMenu newPasteMenu(Bus bus, File dir) {
    return new PasteMenu(bus, dir);
  }
}
