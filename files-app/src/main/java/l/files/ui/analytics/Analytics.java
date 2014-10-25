package l.files.ui.analytics;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionParser;
import com.google.analytics.tracking.android.StandardExceptionParser;

import static com.google.analytics.tracking.android.MapBuilder.createEvent;
import static com.google.analytics.tracking.android.MapBuilder.createException;
import static java.lang.Thread.currentThread;

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

  public static void onEvent(Context context, String category, String action) {
    onEvent(context, category, action, null);
  }

  /**
   * Tracks a custom event.
   *
   * @param label the optional, custom label for this event
   */
  public static void onEvent(
      Context context, String category, String action, String label) {
    onEvent(context, category, action, label, null);
  }

  /**
   * Tracks a custom event.
   *
   * @param label the optional, custom label for this event
   * @param value the optional, custom value for this event
   */
  public static void onEvent(
      Context ctx, String category, String action, String label, Long value) {
    get(ctx).send(createEvent(category, action, label, value).build());
  }

  /**
   * Tracks an {@link Exception}.
   */
  public static void onException(Context context, Exception e) {
    ExceptionParser parser = new StandardExceptionParser(context, null);
    String description = parser.getDescription(currentThread().getName(), e);
    get(context).send(createException(description, false).build());
  }

  /**
   * Tracks a preference changed event.
   *
   * @param key the preference key
   * @param value the preference value converted to string form
   */
  public static void onPreferenceChanged(
      Context context, String key, String value) {
    onEvent(context, "preference", key, value, null);
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
    onEvent(context, "menu", action, label, value);
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
