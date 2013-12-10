package l.files.analytics;

import l.files.common.app.BaseFragmentActivity;

public class AnalyticsActivity extends BaseFragmentActivity {

  @Override protected void onStart() {
    super.onStart();
    Analytics.onActivityStart(this);
  }

  @Override protected void onStop() {
    super.onStop();
    Analytics.onActivityStop(this);
  }
}
