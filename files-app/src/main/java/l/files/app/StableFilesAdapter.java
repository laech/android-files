package l.files.app;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import static l.files.provider.FileCursors.getFileId;

abstract class StableFilesAdapter extends CursorAdapter {

  private static final TObjectLongMap<String> ids = new TObjectLongHashMap<>();

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public long getItemId(int position) {
    String fileId = getFileId(getItem(position));
    ids.putIfAbsent(fileId, ids.size() + 1);
    return ids.get(fileId);
  }
}
