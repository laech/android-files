package linux;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;

import linux.Vfs.Statfs;

import static android.test.MoreAsserts.assertNotEqual;
import static linux.Vfs.statfs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class VfsTest {

    @Test
    public void constants_are_initialized() throws Exception {
        Field[] fields = Vfs.class.getFields();
        assertNotEqual(0, fields.length);
        for (Field field : fields) {
            assertNotEqual(field.getName(), Vfs.placeholder(), field.getLong(null));
        }
    }

    @Test
    public void statfs_throws_NullPointerException_on_null_path_arg() throws Exception {
        try {
            statfs(null, new Statfs());
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void statfs_throws_NullPointerException_on_null_statfs_arg() throws Exception {
        try {
            statfs("/".getBytes(), null);
            fail();
        } catch (NullPointerException e) {
            // Pass
        }
    }

    @Test
    public void statfs_fields_are_initialized() throws Exception {

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

    @Test
    public void statfs_returns_correct_information() throws Exception {
        File file = new File("/");
        Statfs statfs = new Statfs();
        statfs(file.getPath().getBytes(), statfs);
        assertEquals(file.getFreeSpace(), statfs.f_bfree);
        assertEquals(file.getUsableSpace(), statfs.f_bavail);
        assertEquals(file.getTotalSpace(), statfs.f_blocks * statfs.f_bsize);
    }
}
