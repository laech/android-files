package l.files.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;
import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class DeleteTest extends FileBaseTest {

    public void testNotifiesListener() throws Exception {
        File a = dir1().resolve("a").createDir();
        File b = dir1().resolve("a/b").createFile();

        Set<File> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    public void testDeletesFile() throws Exception {
        File file = dir1().resolve("a").createFile();
        delete(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    public void testDeletesNonEmptyDirectory() throws Exception {
        File dir = dir1().resolve("a").createDir();
        File file = dir1().resolve("a/child.txt").createFile();
        delete(dir);
        assertFalse(file.exists(NOFOLLOW));
        assertFalse(dir.exists(NOFOLLOW));
    }

    public void testDeletesEmptyDirectory() throws Exception {
        File dir = dir1().resolve("a").createDir();
        delete(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    public void testDeletesSymbolicLinkButNotLinkedFile() throws Exception {
        File a = dir1().resolve("a").createFile();
        File b = dir1().resolve("b").createLink(a);
        assertTrue(a.exists(NOFOLLOW));
        assertTrue(b.exists(NOFOLLOW));
        delete(b);
        assertFalse(b.exists(NOFOLLOW));
        assertTrue(a.exists(NOFOLLOW));
    }

    private void delete(File file) throws Exception {
        create(singleton(file)).execute();
    }

    private Delete create(Collection<? extends File> resources) {
        return new Delete(resources);
    }

}
