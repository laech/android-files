package l.files.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

public abstract class BaseActivityTest<T extends Activity>
    extends ActivityInstrumentationTestCase2<T> {

  public BaseActivityTest(Class<T> activityClass) {
    super(activityClass);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    Dexmaker.setup(this);
  }

  protected final T activity() {
    return getActivity();
  }
}
