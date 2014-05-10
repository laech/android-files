package l.files.service;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import l.files.analytics.Analytics;
import l.files.io.file.operations.NoReadException;
import l.files.io.file.operations.NoWriteException;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

final class Util {

  public static void showErrorMessage(Context context, IOException e) {
    String msg = getErrorMessage(context, e);
    makeText(context, msg, LENGTH_LONG).show();
    Analytics.onException(context, e);
    Log.e(context.getClass().getSimpleName(), e.getMessage(), e);
  }

  public static String getErrorMessage(Context context, IOException e) {
    if (e instanceof NoReadException) {
      return getNoReadMessage(context, (NoReadException) e);
    } else if (e instanceof NoWriteException) {
      return getNoWriteMessage(context, (NoWriteException) e);
    } else {
      return getUnknownErrorMessage(context);
    }
  }

  private static String getNoReadMessage(Context context, NoReadException e) {
    int msgId = R.string.failed_to_read_from_x_permission;
    return context.getString(msgId, e.file());
  }

  private static String getNoWriteMessage(Context context, NoWriteException e) {
    int msgId = R.string.failed_to_write_to_x_permission;
    return context.getString(msgId, e.file());
  }

  private static String getUnknownErrorMessage(Context context) {
    return context.getString(R.string.unknown_operation_error);
  }

  private Util() {}
}
