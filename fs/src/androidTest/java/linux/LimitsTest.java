package linux;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotEquals;

public final class LimitsTest {

    @Test
    public void constants_are_initialized() throws Exception {
        Field[] fields = Limits.class.getFields();
        assertNotEquals(0, fields.length);
        for (Field field : fields) {
            assertNotEquals(field.getName(), Limits.placeholder(), field.getInt(null));
        }
    }

}
