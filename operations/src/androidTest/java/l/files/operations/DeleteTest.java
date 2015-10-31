package l.files.operations;

import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;
import l.files.testing.fs.FileBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DeleteTest extends FileBaseTest {

    @Test
    public void notifiesListener() throws Exception {
        File a = dir1().resolve("a").createDir();
        File b = dir1().resolve("a/b").createFile();

        Set<File> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    @Test
    public void deletesFile() throws Exception {
        File file = dir1().resolve("a").createFile();
        delete(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    @Test
    public void deletesNonEmptyDirectory() throws Exception {
        File dir = dir1().resolve("a").createDir();
        File file = dir1().resolve("a/child.txt").createFile();
        delete(dir);
        assertFalse(file.exists(NOFOLLOW));
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deletesEmptyDirectory() throws Exception {
        File dir = dir1().resolve("a").createDir();
        delete(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    @Test
    public void deletesSymbolicLinkButNotLinkedFile() throws Exception {
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
