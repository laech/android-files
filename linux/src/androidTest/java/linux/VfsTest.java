package linux;

import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.Field;

import linux.Vfs.Statfs;

import static android.test.MoreAsserts.assertNotEqual;
import static linux.Vfs.statfs;

public final class VfsTest extends TestCase {

    public void test_constants_are_initialized() throws Exception {
        Field[] fields = Vfs.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertNotEqual(field.getName(), Vfs.placeholder(), field.getLong(null));
        }
    }

    public void test_statfs_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            statfs(null, new Statfs());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_statfs_throws_NullPointerException_on_null_statfs_arg() throws Exception {
        try {
            statfs("/".getBytes(), null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    public void test_statfs_fields_are_initialized() throws Exception {

        Statfs statfs = new Statfs();
        Field[] fields = Statfs.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertEquals(Vfs.placeholder(), field.getLong(statfs));
        }

        statfs("/".getBytes(), statfs);
        for (Field field : fields) {
            assertNotEqual(Vfs.placeholder(), field.getLong(statfs));
        }
    }

    public void test_statfs_returns_correct_information() throws Exception {
        File file = new File("/");
        Statfs statfs = new Statfs();
        statfs(file.getPath().getBytes(), statfs);
        assertEquals(file.getFreeSpace(), statfs.f_bfree);
        assertEquals(file.getUsableSpace(), statfs.f_bavail);
        assertEquals(file.getTotalSpace(), statfs.f_blocks * statfs.f_bsize);
    }
}
