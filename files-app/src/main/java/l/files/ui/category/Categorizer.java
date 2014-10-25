package l.files.ui.category;

import android.content.res.Resources;
import android.database.Cursor;

/**
 * Provides category information for items in cursors.
 */
public interface Categorizer {

  /**
   * Always return null categories.
   */
  Categorizer NULL = new Categorizer() {
    @Override public String getCategory(Resources res, Cursor cursor) {
      return null;
    }
  };

  /**
   * Gets the category of the item at the current cursor position.
   *
   * @return the category for displaying purposes, or null
   */
  String getCategory(Resources res, Cursor cursor);
}
