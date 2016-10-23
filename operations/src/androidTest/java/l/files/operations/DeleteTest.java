package l.files.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.exists;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class DeleteTest extends PathBaseTest {

    public void test_notifiesListener() throws Exception {
        Path a = createDir(dir1().concat("a"));
        Path b = createFile(dir1().concat("a/b"));

        Set<Path> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    public void test_deletesFile() throws Exception {
        Path file = createFile(dir1().concat("a"));
        delete(file);
        assertFalse(exists(file, NOFOLLOW));
    }

    public void test_deletesNonEmptyDirectory() throws Exception {
        Path dir = createDir(dir1().concat("a"));
        Path file = createFile(dir1().concat("a/child.txt"));
        delete(dir);
        assertFalse(exists(file, NOFOLLOW));
        assertFalse(exists(dir, NOFOLLOW));
    }

    public void test_deletesEmptyDirectory() throws Exception {
        Path dir = createDir(dir1().concat("a"));
        delete(dir);
        assertFalse(exists(dir, NOFOLLOW));
    }

    public void test_deletesSymbolicLinkButNotLinkedFile() throws Exception {
        Path a = createFile(dir1().concat("a"));
        Path b = createSymbolicLink(dir1().concat("b"), a);
        assertTrue(exists(a, NOFOLLOW));
        assertTrue(exists(b, NOFOLLOW));
        delete(b);
        assertFalse(exists(b, NOFOLLOW));
        assertTrue(exists(a, NOFOLLOW));
    }

    private void delete(Path file) throws Exception {
        create(singleton(file)).execute();
    }

    private Delete create(Collection<? extends Path> resources) {
        return new Delete(resources);
    }

}
