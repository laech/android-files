package l.files.common.database;

import android.database.AbstractCursor;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * This cursor class caches the indices of its columns, which will provide
 * slightly faster speed when looking up row values.
 */
public abstract class BaseCursor extends AbstractCursor {

  private final Map<String, Integer> columnIndices = newHashMap();

  @Override public int getColumnIndex(String columnName) {
    Integer index = columnIndices.get(columnName);
    if (index == null) {
      index = super.getColumnIndex(columnName);
      columnIndices.put(columnName, index);
    }
    return index;
  }

  @Override public int getColumnIndexOrThrow(String columnName) {
    int index = getColumnIndex(columnName);
    if (index < 0) {
      throw new IllegalArgumentException(
          "column '" + columnName + "' does not exist");
    }
    return index;
  }
}
