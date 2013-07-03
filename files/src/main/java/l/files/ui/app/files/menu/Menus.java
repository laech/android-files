package l.files.ui.app.files.menu;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import l.files.setting.SetSetting;
import l.files.ui.menu.OptionsMenuAction;

import java.io.File;

public final class Menus {

  public static OptionsMenuAction newBookmarkAction(File dir, SetSetting<File> setting) {
    return new BookmarkAction(dir, setting);
  }

  public static OptionsMenuAction newSettingsAction(Context context) {
    return new SettingsAction(context);
  }

  public static OptionsMenuAction newSortAction(FragmentManager manager) {
    return new SortAction(manager, SortDialog.CREATOR);
  }

  public static OptionsMenuAction newDirAction(File parent) {
    return new NewDirAction(parent);
  }

  private Menus() {}
}
