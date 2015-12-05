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
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createLink;
import static l.files.fs.Files.exists;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DeleteTest extends PathBaseTest {

    @Test
    public void notifiesListener() throws Exception {
        Path a = createDir(dir1().resolve("a"));
        Path b = createFile(dir1().resolve("a/b"));

        Set<Path> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    @Test
    public void deletesFile() throws Exception {
        Path file = createFile(dir1().resolve("a"));
        delete(file);
        assertFalse(exists(file, NOFOLLOW));
    }

    @Test
    public void deletesNonEmptyDirectory() throws Exception {
        Path dir = createDir(dir1().resolve("a"));
        Path file = createFile(dir1().resolve("a/child.txt"));
        delete(dir);
        assertFalse(exists(file, NOFOLLOW));
        assertFalse(exists(dir, NOFOLLOW));
    }

    @Test
    public void deletesEmptyDirectory() throws Exception {
        Path dir = createDir(dir1().resolve("a"));
        delete(dir);
        assertFalse(exists(dir, NOFOLLOW));
    }

    @Test
    public void deletesSymbolicLinkButNotLinkedFile() throws Exception {
        Path a = createFile(dir1().resolve("a"));
        Path b = createLink(dir1().resolve("b"), a);
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
