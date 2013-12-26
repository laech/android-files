package l.files.app;

import android.database.Cursor;
import android.widget.AbsListView;

import java.util.List;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static l.files.common.widget.ListViews.getCheckedItemPositions;
import static l.files.provider.FileCursors.getLocation;

public final class ListViews {
  private ListViews() {}

  public static List<String> getCheckedFileLocations(AbsListView list) {
    List<Integer> positions = getCheckedItemPositions(list);
    List<String> ids = newArrayListWithCapacity(positions.size());
    for (int position : positions) {
      Cursor cursor = (Cursor) list.getItemAtPosition(position);
      ids.add(getLocation(cursor));
    }
    return ids;
  }
}
