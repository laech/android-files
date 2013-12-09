package l.files.app;

import com.google.analytics.tracking.android.EasyTracker;

import l.files.common.app.BaseFragmentActivity;

public class AnalyticsActivity extends BaseFragmentActivity {

  @Override protected void onStart() {
    super.onStart();
    EasyTracker.getInstance(this).activityStart(this);
  }

  @Override protected void onStop() {
    super.onStop();
    EasyTracker.getInstance(this).activityStop(this);
  }
}
