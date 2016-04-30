package linux;

import junit.framework.TestCase;

import java.lang.reflect.Field;

import static android.test.MoreAsserts.assertNotEqual;

public final class LimitsTest extends TestCase {

    public void test_constants_are_initialized() throws Exception {
        Field[] fields = Limits.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertNotEqual(field.getName(), Limits.placeholder(), field.getInt(null));
        }
    }

}
