package l.files.service;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

import l.files.analytics.Analytics;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;
import static java.util.Arrays.asList;

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

  public static List<File> listDirectoryChildren(File dir) throws NoReadException {
    File[] children = dir.listFiles();
    if (children == null) {
      throw new NoReadException(dir);
    }
    return asList(children);
  }

  private Util() {}
}
