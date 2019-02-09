package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.HashSet;

import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.base.io.Charsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public final class PathInvalidUtf8Test extends PathBaseTest {

    private final byte[] invalidUtf8;

    public PathInvalidUtf8Test(byte[] invalidUtf8) {
        this.invalidUtf8 = invalidUtf8;

        assertFalse(Arrays.equals(
                invalidUtf8.clone(),
                new String(invalidUtf8, UTF_8).getBytes(UTF_8)));
    }

    @Parameters
    public static Iterable<Object[]> data() {
        return asList(new Object[][]{
                {new byte[]{
                        0xffffffed,
                        0xffffffa0,
                        0xffffffbd,
                        0xffffffed,
                        0xffffffb0,
                        0xffffff8b,
                }},
                // The following from:
                // http://stackoverflow.com/a/3886015
                {new byte[]{
                        (byte) 0xe2,
                        (byte) 0x82,
                        (byte) 0x28,
                }},
                {new byte[]{
                        (byte) 0xf0,
                        (byte) 0x28,
                        (byte) 0x8c,
                        (byte) 0xbc,
                }},
                {new byte[]{
                        (byte) 0xf8,
                        (byte) 0xa1,
                        (byte) 0xa1,
                        (byte) 0xa1,
                        (byte) 0xa1,
                }},
        });
    }

    @Test
    public void can_handle_invalid_utf_8_path() throws Exception {

        Path dir = dir1().concat(invalidUtf8.clone()).createDirectory();
        Path file = dir.concat("a").createFile();

        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(file.exists(NOFOLLOW));
        assertEquals(singleton(file), dir.list(new HashSet<>()));

        Name name = dir.name();
        assertNotNull(name);
        assertArrayEquals(invalidUtf8.clone(), name.toByteArray());
        assertEquals(new String(invalidUtf8.clone(), UTF_8), name.toString());
        assertFalse(Arrays.equals(invalidUtf8.clone(), name.toString().getBytes(UTF_8)));
    }

    @Test
    public void can_read_write_invalid_utf8_path() throws Exception {
        Path dir = dir1().concat(invalidUtf8.clone()).createDirectory();
        Path file = dir.concat("file").createFile();
        Paths.writeUtf8(file, "hello");
        assertEquals("hello", Paths.readAllUtf8(file));
    }

}
