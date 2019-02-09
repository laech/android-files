package linux;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotEquals;


public final class ErrnoTest {

    @Test
    public void constants_are_initialized() throws Exception {
        Field[] fields = Errno.class.getFields();
        assertNotEquals(0, fields.length);
        for (Field field : fields) {
            assertNotEquals(field.getName(), Errno.placeholder(), field.getInt(null));
        }
    }

}
