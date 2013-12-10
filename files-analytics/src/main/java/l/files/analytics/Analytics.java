package l.files.analytics;

import android.app.Activity;
import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;

import static com.google.analytics.tracking.android.MapBuilder.createEvent;

public final class Analytics {
  private Analytics() {}

  private static EasyTracker get(Context context) {
    return EasyTracker.getInstance(context);
  }

  /**
   * Tracks {@link Activity#onStart()}.
   */
  public static void onActivityStart(Activity activity) {
    get(activity).activityStart(activity);
  }

  /**
   * Tracks {@link Activity#onStop()}.
   */
  public static void onActivityStop(Activity activity) {
    get(activity).activityStop(activity);
  }

  /**
   * Tracks a menu item select event.
   */
  public static void onMenuItemSelected(Context context, String action) {
    get(context).send(createEvent("menu", action, null, null).build());
  }
}
