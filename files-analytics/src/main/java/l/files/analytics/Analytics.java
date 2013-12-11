package l.files.analytics;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;

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
   * Tracks a preference changed event.
   *
   * @param key the preference key
   * @param value the preference value converted to string form
   */
  public static void onPreferenceChanged(
      Context context, String key, String value) {
    get(context).send(createEvent("preference", key, value, null).build());
  }

  /**
   * Tracks a menu item select event.
   *
   * @param action the string identifying the action
   * @param label the optional, custom label for this event
   * @param value the optional, custom value for this event
   */
  public static void onMenuItemSelected(
      Context context, String action, String label, Long value) {
    get(context).send(createEvent("menu", action, label, value).build());
  }

  /**
   * Provides optional event labels and values for a menu item click.
   */
  public static interface OnMenuItemSelectedEventProvider {

    /**
     * Gets the optional custom label for this menu item selected event.
     */
    String getEventLabel(MenuItem item);

    /**
     * Gets the optional custom label for this menu item selected event.
     */
    Long getEventValue(MenuItem item);
  }
}
