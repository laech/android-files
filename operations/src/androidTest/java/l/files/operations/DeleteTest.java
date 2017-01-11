package l.files.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class DeleteTest extends PathBaseTest {

    public DeleteTest() {
        super(LocalFileSystem.INSTANCE);
    }

    public void test_notifiesListener() throws Exception {
        Path a = fs.createDir(dir1().concat("a"));
        Path b = fs.createFile(dir1().concat("a/b"));

        Set<Path> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    public void test_deletesFile() throws Exception {
        Path file = fs.createFile(dir1().concat("a"));
        delete(file);
        assertFalse(fs.exists(file, NOFOLLOW));
    }

    public void test_deletesNonEmptyDirectory() throws Exception {
        Path dir = fs.createDir(dir1().concat("a"));
        Path file = fs.createFile(dir1().concat("a/child.txt"));
        delete(dir);
        assertFalse(fs.exists(file, NOFOLLOW));
        assertFalse(fs.exists(dir, NOFOLLOW));
    }

    public void test_deletesEmptyDirectory() throws Exception {
        Path dir = fs.createDir(dir1().concat("a"));
        delete(dir);
        assertFalse(fs.exists(dir, NOFOLLOW));
    }

    public void test_deletesSymbolicLinkButNotLinkedFile() throws Exception {
        Path a = fs.createFile(dir1().concat("a"));
        Path b = fs.createSymbolicLink(dir1().concat("b"), a);
        assertTrue(fs.exists(a, NOFOLLOW));
        assertTrue(fs.exists(b, NOFOLLOW));
        delete(b);
        assertFalse(fs.exists(b, NOFOLLOW));
        assertTrue(fs.exists(a, NOFOLLOW));
    }

    private void delete(Path file) throws Exception {
        create(singleton(file)).execute();
    }

    private Delete create(Collection<? extends Path> resources) {
        return new Delete(resources);
    }

}
