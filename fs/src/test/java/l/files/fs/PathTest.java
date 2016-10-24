package l.files.fs;

import org.junit.Test;

import java.io.IOException;

import static l.files.fs.Files.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PathTest {

    @Test
    public void toByteArray_returns_correct_byte_representation_of_path() throws Exception {
        testToByteArray("");
        testToByteArray("/");
        testToByteArray("/a");
        testToByteArray("/a/b");
        testToByteArray("a/b");
        testToByteArray("/aa/bb");
        testToByteArray("/aaa/bbb/cccccc");
        testToByteArray("/你好吗/我很好/谢谢");
    }

    private void testToByteArray(String path) throws IOException {
        byte[] expected = path.getBytes(UTF_8);
        byte[] actual = path(path).toByteArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void isHidden_is_true_if_name_starts_with_dot() throws Exception {
        assertTrue(path("/a/.b").isHidden());
        assertTrue(path(".b").isHidden());
    }

    @Test
    public void isHidden_is_false_if_name_does_not_start_with_dot() throws Exception {
        assertFalse(path("/a/b").isHidden());
        assertFalse(path("/.a/b").isHidden());
        assertFalse(path("b").isHidden());
    }

    @Test
    public void rebase_self_to_new_ancestor() throws Exception {
        assertEquals(path("/a/b/c"), path("/c").rebase(path("/"), path("/a/b")));
        assertEquals(path("/a/b/c"), path("/c").rebase(path("/"), path("/a/b")));
        assertEquals(path("/a/b/c"), path("/1/d/b/c").rebase(path("/1/d"), path("/a")));
        assertEquals(path("/a/b"), path("/a/b").rebase(path("/a/b"), path("/a/b")));
        assertEquals(path("/a/b/file"), path("/a/file").rebase(path("/a"), path("/a/b")));
    }

    @Test
    public void rebase_from_self_returns_dst_directly() throws Exception {
        assertEquals(path("/a/b/c"), path("/a/b").rebase(path("/a/b"), path("/a/b/c")));
    }

    private Path path(String path) {
        return Path.fromString(path);
    }

}
