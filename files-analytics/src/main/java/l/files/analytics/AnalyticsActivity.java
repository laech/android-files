package l.files.analytics;

import l.files.common.app.BaseActivity;

public class AnalyticsActivity extends BaseActivity {

  @Override protected void onStart() {
    super.onStart();
    Analytics.onActivityStart(this);
  }

  @Override protected void onStop() {
    super.onStop();
    Analytics.onActivityStop(this);
  }
}
