package l.files.common.testing;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

public class BaseActivityTest<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

    public BaseActivityTest(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Dexmaker.setup(this);
    }

}
