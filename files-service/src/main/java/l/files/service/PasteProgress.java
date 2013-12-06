package l.files.service;

import android.content.Context;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static android.text.format.DateUtils.formatElapsedTime;
import static android.text.format.Formatter.formatFileSize;
import static l.files.service.PasteType.COPY;
import static l.files.service.PasteType.CUT;

final class PasteProgress extends Progress {

  private static final Map<PasteType, Integer> TITLES = ImmutableMap.of(
      CUT, R.plurals.moving_x_items_to_x,
      COPY, R.plurals.copying_x_items_to_x
  );

  private final PasteType type;
  private final Context context;
  private final String destination;
  private final int remaining;
  private final long bytesCopied;
  private final long bytesTotal;
  private final long timeLeftMillis;

  PasteProgress(
      PasteType type,
      Context context,
      String destination,
      int remaining,
      long bytesCopied,
      long bytesTotal,
      long timeLeftMillis) {
    this.type = type;
    this.context = context;
    this.destination = destination;
    this.remaining = remaining;
    this.bytesCopied = bytesCopied;
    this.bytesTotal = bytesTotal;
    this.timeLeftMillis = timeLeftMillis;
  }

  @Override String getNotificationContentTitle() {
    return context.getResources().getQuantityString(
        TITLES.get(type), remaining, remaining, destination);
  }

  @Override String getNotificationContentText() {
    String copied = formatFileSize(context, bytesCopied);
    String total = formatFileSize(context, bytesTotal);
    return context.getString(R.string.x_of_x_size, copied, total);
  }

  @Override String getNotificationContentInfo() {
    if (timeLeftMillis > 0) {
      return formatTimeLeft();
    }
    return super.getNotificationContentInfo();
  }

  @Override float getNotificationProgressPercentage() {
    return bytesCopied / (float) bytesTotal;
  }

  private String formatTimeLeft() {
    long seconds = timeLeftMillis / 1000;
    String formatted = formatElapsedTime(seconds);
    if (formatted.charAt(0) == '0') {
      formatted = formatted.substring(1);
    }
    return context.getString(R.string.x_countdown, formatted);
  }
}
