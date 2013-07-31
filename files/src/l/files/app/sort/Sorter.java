package l.files.app.sort;

import android.content.res.Resources;
import com.google.common.base.Function;
import l.files.event.Sort;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * A sorter for an associated {@link Sort} for sorting files to be displayed in
 * a list view, optionally can inject extra elements such as headers into the
 * resulting list.
 */
public interface Sorter extends Function<Collection<File>, List<Object>> {

  /**
   * Gets the ID of this sorter.
   */
  Sort id();

  /**
   * Gets the name of this sorter for display.
   */
  String name(Resources res);
}
