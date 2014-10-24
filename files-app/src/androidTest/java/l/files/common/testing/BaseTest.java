package l.files.common.testing;

import android.test.AndroidTestCase;

public abstract class BaseTest extends AndroidTestCase {

  @Override protected void setUp() throws Exception {
    super.setUp();
    Dexmaker.setup(this);
  }
}
