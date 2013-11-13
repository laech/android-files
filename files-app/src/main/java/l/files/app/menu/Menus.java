package l.files.app.menu;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;

import com.squareup.otto.Bus;

import java.io.File;

import l.files.app.TabHandler;
import l.files.common.app.OptionsMenu;

public final class Menus {
  private Menus() {}

  public static OptionsMenu newBookmarkMenu(
      Context context, LoaderManager loaders,
      ContentResolver resolver, String fileId) {
    return new BookmarkMenu(context, loaders, resolver, fileId);
  }

  public static OptionsMenu newSortMenu(FragmentManager manager) {
    return new SortMenu(manager);
  }

  public static OptionsMenu newDirMenu(FragmentManager manager, String parentId) {
    return new NewDirMenu(manager, parentId);
  }

  public static OptionsMenu newShowHiddenFilesMenu(Context context) {
    return new ShowHiddenFilesMenu(context);
  }

  public static OptionsMenu newPasteMenu(Bus bus, File dir) {
    return new PasteMenu(bus, dir);
  }

  public static OptionsMenu newTabMenu(TabHandler handler) {
    return new NewTabMenu(handler);
  }

  public static OptionsMenu newCloseTabMenu(TabHandler handler) {
    return new CloseTabMenu(handler);
  }
}
