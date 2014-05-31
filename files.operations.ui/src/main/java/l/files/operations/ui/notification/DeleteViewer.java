package l.files.operations.ui.notification;

import android.content.Intent;
import android.content.res.Resources;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.operations.Progress.Delete.getDeletedItemCount;
import static l.files.operations.Progress.Delete.getTotalItemCount;
import static l.files.operations.Progress.STATUS_PENDING;
import static l.files.operations.Progress.STATUS_PREPRARING;
import static l.files.operations.Progress.STATUS_PROCESSING;
import static l.files.operations.Progress.getTaskStatus;
import static l.files.operations.ui.R.drawable;
import static l.files.operations.ui.R.plurals.deleting_x_items;
import static l.files.operations.ui.R.plurals.preparing_delete_x_items;
import static l.files.operations.ui.R.string.preparing_to_delete;

final class DeleteViewer implements NotificationViewer {

  private final Resources res;

  DeleteViewer(Resources res) {
    this.res = checkNotNull(res, "res");
  }

  @Override public int getSmallIcon() {
    return drawable.ic_stat_notify_delete;
  }

  @Override public String getContentTitle(Intent intent) {
    switch (getTaskStatus(intent)) {
      case STATUS_PENDING: return getTitleForStatusPending();
      case STATUS_PREPRARING: return getTitleForStatusPreparing(intent);
      case STATUS_PROCESSING: return getTitleForStatusProcessing(intent);
      default: return null;
    }
  }

  private String getTitleForStatusPending() {
    return res.getString(preparing_to_delete);
  }

  private String getTitleForStatusPreparing(Intent intent) {
    int count = getTotalItemCount(intent);
    return res.getQuantityString(preparing_delete_x_items, count, count);
  }

  private String getTitleForStatusProcessing(Intent intent) {
    int count = getTotalItemCount(intent) - getDeletedItemCount(intent);
    return res.getQuantityString(deleting_x_items, count, count);
  }

  @Override public float getProgress(Intent intent) {
    return getDeletedItemCount(intent) / (float) getTotalItemCount(intent);
  }

  @Override public String getContentText(Intent intent) {
    return null;
  }

  @Override public String getContentInfo(Intent intent) {
    return null;
  }
}
