package l.files.ui.app.files.menu;

import l.files.setting.SetSetting;
import l.files.ui.menu.OptionsMenuAction;

import java.io.File;

public final class Menus {

  public static OptionsMenuAction newBookmarkAction(File dir, SetSetting<File> setting) {
    return new BookmarkAction(dir, setting);
  }

  private Menus() {}
}
