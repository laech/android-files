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

  public static void onActivityStart(Activity activity) {
    get(activity).activityStart(activity);
  }

  public static void onActivityStop(Activity activity) {
    get(activity).activityStop(activity);
  }

  public static void onOptionsItemSelected(Context context, String action) {
    get(context).send(createEvent("menu", action, null, null).build());
  }
}
