package l.files.ui;

import android.content.res.Resources;

/**
 * Provides category information for items in cursors.
 */
interface Categorizer {

  /**
   * Always return null categories.
   */
  Categorizer NULL = new Categorizer() {
    @Override public String get(Resources res, FileListItem.File file) {
      return null;
    }
  };

  /**
   * @return the category for displaying purposes, or null
   */
  String get(Resources res, FileListItem.File file);
}
