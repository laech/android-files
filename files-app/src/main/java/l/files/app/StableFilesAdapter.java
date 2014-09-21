package l.files.app;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static l.files.provider.FilesContract.Files;

abstract class StableFilesAdapter extends CursorAdapter {

  private static final Map<String, Long> ids = newHashMap();

  @Override public boolean hasStableIds() {
    return true;
  }

  @Override public long getItemId(int position) {
    String location = Files.id(getItem(position));
    Long id = ids.get(location);
    if (id == null) {
      id = ids.size() + 1L;
      ids.put(location, id);
    }
    return id;
  }
}
