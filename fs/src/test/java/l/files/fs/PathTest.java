package l.files.fs;

import org.junit.Test;

import java.io.IOException;

import static l.files.fs.Files.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
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
    public void hashCode_are_the_same_if_bytes_are_the_same() throws Exception {
        assertEquals(path("aa").hashCode(), path("aa").hashCode());
        assertEquals(path("a/").hashCode(), path("a/").hashCode());
        assertEquals(path("/a").hashCode(), path("/a").hashCode());
        assertEquals(path("/a/b").hashCode(), path("/a/b").hashCode());
        assertEquals(path("/a/b/").hashCode(), path("/a/b/").hashCode());
        assertEquals(path("a/b/").hashCode(), path("a/b/").hashCode());
        assertEquals(path("a/./").hashCode(), path("a/./").hashCode());
        assertEquals(path("a/.").hashCode(), path("a/.").hashCode());
        assertEquals(path("a/../").hashCode(), path("a/../").hashCode());
        assertEquals(path("a/..").hashCode(), path("a/..").hashCode());
        assertEquals(path("/").hashCode(), path("/").hashCode());
        assertEquals(path("").hashCode(), path("").hashCode());
    }

    @Test
    public void hashCode_are_different_if_bytes_are_different() throws Exception {
        Path p1 = path("aa");
        Path p2 = path("ab");
        assertNotEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void equals_if_bytes_are_equal() throws Exception {
        assertEquals(path("aa"), path("aa"));
        assertEquals(path("a/"), path("a/"));
        assertEquals(path("/a"), path("/a"));
        assertEquals(path("/a/b"), path("/a/b"));
        assertEquals(path("/a/b/"), path("/a/b/"));
        assertEquals(path("a/b/"), path("a/b/"));
        assertEquals(path("a/./"), path("a/./"));
        assertEquals(path("a/."), path("a/."));
        assertEquals(path("a/../"), path("a/../"));
        assertEquals(path("a/.."), path("a/.."));
        assertEquals(path("/"), path("/"));
        assertEquals(path(""), path(""));
    }

    @Test
    public void equals_return_false_if_bytes_are_not_equal() throws Exception {
        Path p1 = path("aa");
        Path p2 = path("ab");
        assertNotEquals(p1, p2);
    }

    @Test
    public void toString_returns_string_representation() throws Exception {
        assertEquals("c", path("c").toString());
        assertEquals("c", path("c/").toString());
        assertEquals("c", path("c///").toString());
        assertEquals("/c", path("/c").toString());
        assertEquals("/c", path("/c/").toString());
        assertEquals("/c", path("/c///").toString());
        assertEquals("/", path("/").toString());
        assertEquals("", path("").toString());
        assertEquals("/a/b/c", path("/a/b/c").toString());
        assertEquals("/a/b/c", path("///a///b///c").toString());
        assertEquals("/a/./c", path("///a///.///c//").toString());
    }

    @Test
    public void resolve_from_path() throws Exception {
        assertEquals("/a/b", path("/a").concat(path("b")).toString());
        assertEquals("/a/b", path("/a/b").concat(path("")).toString());
        assertEquals("/a/b", path("/a///b/").concat(path("")).toString());
        assertEquals("/a/b", path("/a/").concat(path("b")).toString());
        assertEquals("a/b", path("a").concat(path("b")).toString());
    }

    @Test
    public void resolve_from_byte_paths() throws Exception {
        assertEquals("/a/b", path("/a").concat(bytes("b")).toString());
        assertEquals("/a/b", path("/a").concat(bytes("b///")).toString());
        assertEquals("/a/b", path("////a").concat(bytes("b///")).toString());
        assertEquals("/a/b", path("/a/b").concat(bytes("")).toString());
        assertEquals("/a/b", path("/a///b/").concat(bytes("")).toString());
        assertEquals("/a/b", path("/a/").concat(bytes("b")).toString());
        assertEquals("/a/b", path("///a//").concat(bytes("b/")).toString());
        assertEquals("a/b", path("a").concat(bytes("b")).toString());
        assertEquals("a/b", path("a").concat(bytes("/b")).toString());
        assertEquals("a/b", path("a").concat(bytes("/b/")).toString());
        assertEquals("a/b", path("a").concat(bytes("///b///")).toString());
        assertEquals("/a/b", path("/a").concat(bytes("/b")).toString());
    }

    @Test
    public void parent_is_not_null_if_path_has_parent_component() throws Exception {
        assertEquals("/", path("/a").parent().toString());
        assertEquals("/a", path("/a/b").parent().toString());
        assertEquals("/a/b", path("/a/b/c").parent().toString());
        assertEquals("/a/b", path("/a/b/c//").parent().toString());
    }

    @Test
    public void parent_is_null_if_path_has_no_parent_component() throws Exception {
        assertNull(path("").parent());
        assertNull(path("a").parent());
        assertNull(path("/").parent());
    }

    @Test
    public void name_returns_last_non_empty_path_component() throws Exception {
        assertEquals("b", path("/a/b").name().toString());
        assertEquals("b", path("/a/b/").name().toString());
        assertEquals("b", path("/a//b//").name().toString());
        assertEquals("b", path("b").name().toString());
        assertEquals("b", path("/b").name().toString());
        assertEquals("b", path("b/").name().toString());
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
    public void startsWith_returns_true_for_ancestors() throws Exception {
        assertTrue(path("/a/b").startsWith(path("/a")));
        assertTrue(path("/a/b").startsWith(path("/")));
    }

    @Test
    public void startsWith_returns_true_for_self() throws Exception {
        assertTrue(path("/a/b").startsWith(path("/a/b")));
    }

    @Test
    public void startsWith_returns_false_for_non_ancestor_non_self_paths() throws Exception {
        assertFalse(path("/a/b").startsWith(path("/b")));
        assertFalse(path("/aa/b").startsWith(path("/a")));
        assertFalse(path("/aa/b").startsWith(path("")));
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

    private byte[] bytes(String path) {
        return path.getBytes(UTF_8);
    }

}
