package l.files.ui;

import android.content.res.Resources;

import l.files.fs.FileStatus;

/**
 * Provides category information for items in cursors.
 */
interface Categorizer {

  /**
   * Always return null categories.
   */
  Categorizer NULL = new Categorizer() {
    @Override public String get(Resources res, FileStatus file) {
      return null;
    }
  };

  /**
   * @return the category for displaying purposes, or null
   */
  String get(Resources res, FileStatus file);
}
