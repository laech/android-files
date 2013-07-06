package l.files.ui.app.files.menu;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import l.files.setting.SetSetting;
import l.files.ui.menu.OptionsMenu;

import java.io.File;

public final class Menus {

  public static OptionsMenu newBookmarkMenu(File dir, SetSetting<File> setting) {
    return new BookmarkMenu(dir, setting);
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
