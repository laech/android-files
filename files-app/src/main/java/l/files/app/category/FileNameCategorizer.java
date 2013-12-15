package l.files.app.category;

import android.content.res.Resources;
import android.database.Cursor;

import l.files.R;
import l.files.provider.FilesContract;

/**
 * Categories files by their names.
 *
 * @see FilesContract.FileInfo
 */
public enum FileNameCategorizer implements Categorizer {

  INSTANCE;

  @Override public String getCategory(Resources res, Cursor cursor) {
    return res.getString(R.string.name);
  }
}
