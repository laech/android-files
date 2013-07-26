package l.files.ui.app.files.menu;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import com.squareup.otto.Bus;
import l.files.ui.menu.OptionsMenu;

import java.io.File;

public final class Menus {

  public static OptionsMenu newBookmarkMenu(Bus bus, File dir) {
    return new BookmarkMenu(bus, dir);
  }

  public static OptionsMenu newSettingsMenu(Context context) {
    return new SettingsMenu(context);
  }

  public static OptionsMenu newSortMenu(FragmentManager manager) {
    return new SortMenu(manager, SortDialog.CREATOR);
  }

  public static OptionsMenu newDirMenu(File parent) {
    return new NewDirMenu(parent);
  }

  private Menus() {}
}
