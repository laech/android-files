package l.files.fs.local;

import org.junit.Test;

import static l.files.fs.File.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class LocalPathTest {

    @Test
    public void returns_string_representation() throws Exception {
        assertEquals("/a/b/c", path("/a/b/c").toString());
    }

    @Test
    public void resolves_path_as_child_if_path_does_not_start_with_path_separator()
            throws Exception {

        assertEquals("/a/b", path("/a").resolve(bytes("b"), true).toString());
        assertEquals("/a/b", path("/a").resolve(bytes("b"), false).toString());
        assertEquals("/a/b", path("/a/b").resolve(bytes(""), true).toString());
        assertEquals("/a/b", path("/a/b").resolve(bytes(""), false).toString());
        assertEquals("/a/b", path("/a/").resolve(bytes("b"), true).toString());
        assertEquals("/a/b", path("/a/").resolve(bytes("b"), false).toString());
        assertEquals("a/b", path("a").resolve(bytes("b"), true).toString());
        assertEquals("a/b", path("a").resolve(bytes("b"), false).toString());
    }

    @Test
    public void resolves_absolute_path_relatively()
            throws Exception {

        assertEquals("a/b", path("a").resolve(bytes("/b"), true).toString());
        assertEquals("/a/b", path("/a").resolve(bytes("/b"), true).toString());
    }

    @Test
    public void resolves_path_as_absolute_if_path_starts_with_path_separator()
            throws Exception {

        assertEquals("/b", path("a").resolve(bytes("/b"), false).toString());
        assertEquals("/b", path("/a").resolve(bytes("/b"), false).toString());
    }

    @Test
    public void returns_parent_path() throws Exception {
        assertEquals("/a", path("/a/b").parent().toString());
    }

    @Test
    public void returns_no_parent_path_if_none() throws Exception {
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
