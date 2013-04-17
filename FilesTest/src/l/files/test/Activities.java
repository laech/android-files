package l.files.test;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import android.app.Activity;

public final class Activities {

  public static void rotate(Activity activity) {
    int orientation = activity.getRequestedOrientation();
    activity.setRequestedOrientation(
        orientation == SCREEN_ORIENTATION_LANDSCAPE
            ? SCREEN_ORIENTATION_PORTRAIT
            : SCREEN_ORIENTATION_LANDSCAPE);
  }

  private Activities() {
  }
}
