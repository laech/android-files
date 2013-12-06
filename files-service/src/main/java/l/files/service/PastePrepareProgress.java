package l.files.service;

import android.content.res.Resources;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static l.files.service.PasteType.COPY;
import static l.files.service.PasteType.CUT;

final class PastePrepareProgress extends Progress {

  private static final Map<PasteType, Integer> TITLES = ImmutableMap.of(
      CUT, R.string.preparing_to_move_to_x,
      COPY, R.string.preparing_to_copy_to_x
  );

  private static final Map<PasteType, Integer> TEXTS = ImmutableMap.of(
      CUT, R.plurals.preparing_to_move_x_items,
      COPY, R.plurals.preparing_to_copy_x_items
  );

  private final PasteType type;
  private final Resources res;
  private final String destination;
  private final int count;

  PastePrepareProgress(PasteType type, Resources res, String destination, int count) {
    this.type = type;
    this.res = res;
    this.destination = destination;
    this.count = count;
  }

  @Override String getNotificationContentTitle() {
    return res.getString(TITLES.get(type), destination);
  }

  @Override String getNotificationContentText() {
    return res.getQuantityString(TEXTS.get(type), count, count);
  }
}
