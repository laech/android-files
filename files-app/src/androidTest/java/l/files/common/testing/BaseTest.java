package l.files.common.testing;

import android.content.Context;
import android.test.InstrumentationTestCase;

public abstract class BaseTest extends InstrumentationTestCase {

  @Override protected void setUp() throws Exception {
    super.setUp();
    Dexmaker.setup(this);
  }

  protected Context getContext() {
    return getInstrumentation().getTargetContext();
  }

  protected Context getTestContext() {
    return getInstrumentation().getContext();
  }

}
