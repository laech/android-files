package l.files.ui.app.files.sort;

import java.io.File;
import java.util.Collection;
import java.util.List;

import l.files.setting.SortBy;
import android.content.res.Resources;

import com.google.common.base.Function;

/**
 * A sorter for an associated {@link SortBy} for sorting files to be displayed
 * in a list view, optionally can inject extra elements such as headers into the
 * resulting list.
 */
public interface Sorter extends Function<Collection<File>, List<Object>> {

  /**
   * Gets the ID of this sorter.
   */
  SortBy id();

  /**
   * Gets the name of this sorter for display.
   */
  String name(Resources res);

}
