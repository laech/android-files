package l.files.operations;

import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DeleteTest extends PathBaseTest {

    @Test
    public void notifiesListener() throws Exception {
        Path a = dir1().concat("a").createDir();
        Path b = dir1().concat("a/b").createFile();

        Set<Path> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    @Test
    public void deletesFile() throws Exception {
        Path file = dir1().concat("a").createFile();
        delete(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deletesNonEmptyDirectory() throws Exception {
        Path dir = dir1().concat("a").createDir();
        Path file = dir1().concat("a/child.txt").createFile();
        delete(dir);
        assertFalse(file.exists(NOFOLLOW));
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deletesEmptyDirectory() throws Exception {
        Path dir = dir1().concat("a").createDir();
        delete(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deletesSymbolicLinkButNotLinkedFile() throws Exception {
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
