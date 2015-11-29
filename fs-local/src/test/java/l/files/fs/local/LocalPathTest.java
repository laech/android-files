package l.files.fs.local;

import org.junit.Test;

import l.files.fs.Path;

import static l.files.fs.File.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class LocalPathTest {

    @Test
    public void hash_codes_are_the_same_if_bytes_are_the_same() throws Exception {
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
    public void hash_codes_are_the_same_ignoring_ignorable_separators() throws Exception {
        assertEquals(path("aa/").hashCode(), path("aa").hashCode());
        assertEquals(path("a//").hashCode(), path("a/").hashCode());
        assertEquals(path("//a").hashCode(), path("/a").hashCode());
        assertEquals(path("///a//b").hashCode(), path("/a/b").hashCode());
        assertEquals(path("///a///b////").hashCode(), path("/a/b/").hashCode());
        assertEquals(path("a/b//").hashCode(), path("a/b/").hashCode());
        assertEquals(path("/").hashCode(), path("/").hashCode());
        assertEquals(path("////").hashCode(), path("/").hashCode());
        assertEquals(path("").hashCode(), path("").hashCode());
    }

    @Test
    public void hash_codes_are_different_if_bytes_are_different() throws Exception {
        LocalPath p1 = path("aa");
        LocalPath p2 = path("ab");
        assertNotEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void equal_if_bytes_are_equal() throws Exception {
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
    public void equal_ignores_ignorable_separators() throws Exception {
        assertEquals(path("aa/"), path("aa"));
        assertEquals(path("a//"), path("a/"));
        assertEquals(path("//a"), path("/a"));
        assertEquals(path("/a//b"), path("/a/b"));
        assertEquals(path("////a//b//"), path("/a/b/"));
        assertEquals(path("/"), path("/"));
        assertEquals(path("///"), path("/"));
        assertEquals(path(""), path(""));
    }

    @Test
    public void not_equal_if_bytes_are_not_equal() throws Exception {
        LocalPath p1 = path("aa");
        LocalPath p2 = path("ab");
        assertNotEquals(p1, p2);
    }

    @Test
    public void returns_string_representation() throws Exception {
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
    public void resolves_child_paths() throws Exception {
        assertEquals("/a/b", path("/a").resolve(bytes("b")).toString());
        assertEquals("/a/b", path("/a").resolve(bytes("b///")).toString());
        assertEquals("/a/b", path("////a").resolve(bytes("b///")).toString());
        assertEquals("/a/b", path("/a/b").resolve(bytes("")).toString());
        assertEquals("/a/b", path("/a///b/").resolve(bytes("")).toString());
        assertEquals("/a/b", path("/a/").resolve(bytes("b")).toString());
        assertEquals("/a/b", path("///a//").resolve(bytes("b/")).toString());
        assertEquals("a/b", path("a").resolve(bytes("b")).toString());
        assertEquals("a/b", path("a").resolve(bytes("/b")).toString());
        assertEquals("a/b", path("a").resolve(bytes("/b/")).toString());
        assertEquals("a/b", path("a").resolve(bytes("///b///")).toString());
        assertEquals("/a/b", path("/a").resolve(bytes("/b")).toString());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void returns_parent_path() throws Exception {
        assertEquals("/a", path("/a/b").parent().toString());
        assertEquals("/a/b", path("/a/b/c").parent().toString());
        assertEquals("/a/b", path("/a/b/c//").parent().toString());
    }

    @Test
    public void returns_no_parent_path_if_none() throws Exception {
        assertNull(path("").parent());
        assertNull(path("a").parent());
        assertNull(path("/").parent());
    }

    @Test
    public void returns_name_component() throws Exception {
        assertEquals("b", path("/a/b").name().toString());
        assertEquals("b", path("/a/b/").name().toString());
        assertEquals("b", path("/a//b//").name().toString());
        assertEquals("b", path("b").name().toString());
        assertEquals("b", path("/b").name().toString());
        assertEquals("b", path("b/").name().toString());
    }

    @Test
    public void is_hidden_if_name_starts_with_dot() throws Exception {
        assertTrue(path("/a/.b").isHidden());
        assertTrue(path(".b").isHidden());
    }

    @Test
    public void is_not_hidden_if_name_does_not_start_with_dot() throws Exception {
        assertFalse(path("/a/b").isHidden());
        assertFalse(path("/.a/b").isHidden());
        assertFalse(path("b").isHidden());
    }

    @Test
    public void starts_with_returns_true_for_ancestors() throws Exception {
        assertTrue(path("/a/b").startsWith(path("/a")));
        assertTrue(path("/a/b").startsWith(path("/")));
    }

    @Test
    public void starts_with_returns_true_for_self() throws Exception {
        assertTrue(path("/a/b").startsWith(path("/a/b")));
    }

    @Test
    public void starts_with_returns_false_for_non_ancestor_non_self_paths() throws Exception {
        assertFalse(path("/a/b").startsWith(path("/b")));
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

    private LocalPath path(String path) {
        return LocalPath.of(bytes(path));
    }

    private byte[] bytes(String path) {
        return path.getBytes(UTF_8);
    }

}
