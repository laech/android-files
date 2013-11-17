package l.files.app;

import android.database.Cursor;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;

abstract class StableFilesAdapter extends CursorAdapter {

  private static final TObjectLongMap<String> ids = new TObjectLongHashMap<>();

  private int columnId = -1;

  @Override public void setCursor(Cursor cursor) {
    super.setCursor(cursor);
    if (cursor != null) {
      columnId = cursor.getColumnIndexOrThrow(COLUMN_ID);
    }
  }

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public long getItemId(int position) {
    String fileId = getItem(position).getString(columnId);
    ids.putIfAbsent(fileId, ids.size() + 1);
    return ids.get(fileId);
  }
}
