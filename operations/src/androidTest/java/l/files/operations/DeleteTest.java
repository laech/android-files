package l.files.operations;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class DeleteTest extends PathBaseTest {

    public void test_notifiesListener() throws Exception {
        Path a = dir1().concat("a").createDir();
        Path b = dir1().concat("a/b").createFile();

        Set<Path> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    public void test_deletesFile() throws Exception {
        Path file = dir1().concat("a").createFile();
        delete(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    public void test_deletesNonEmptyDirectory() throws Exception {
        Path dir = dir1().concat("a").createDir();
        Path file = dir1().concat("a/child.txt").createFile();
        delete(dir);
        assertFalse(file.exists(NOFOLLOW));
        assertFalse(dir.exists(NOFOLLOW));
    }

    public void test_deletesEmptyDirectory() throws Exception {
        Path dir = dir1().concat("a").createDir();
        delete(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    public void test_deletesSymbolicLinkButNotLinkedFile() throws Exception {
        Path a = dir1().concat("a").createFile();
        Path b = dir1().concat("b").createSymbolicLink(a);
        assertTrue(a.exists(NOFOLLOW));
        assertTrue(b.exists(NOFOLLOW));
        delete(b);
        assertFalse(b.exists(NOFOLLOW));
        assertTrue(a.exists(NOFOLLOW));
    }

    private void delete(Path file) throws Exception {
        create(singleton(file)).execute();
    }

    private Delete create(Collection<? extends Path> resources) {
        return new Delete(resources);
    }

}
