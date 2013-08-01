package l.files.sort;

import android.content.res.Resources;

import java.io.File;
import java.util.List;

/**
 * A sorter for sorting files to be displayed in a list view, optionally can
 * inject extra elements such as headers into the resulting list.
 */
public interface Sorter {

  /**
   * Gets the ID of this sorter.
   */
  String id();

  /**
   * Gets the name of this sorter for display.
   */
  String name(Resources res);

  /**
   * Applies transformation to the given files.
   */
  List<Object> apply(Resources res, File... files);
}
