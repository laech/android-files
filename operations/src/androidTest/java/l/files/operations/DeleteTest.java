package l.files.operations;

import l.files.testing.fs.PathBaseTest;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public final class DeleteTest extends PathBaseTest {

    @Test
    public void notifiesListener() throws Exception {
        Path a = createDirectory(dir1().resolve("a"));
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
        assertFalse(exists(file, NOFOLLOW_LINKS));
    }

    @Test
    public void deletesNonEmptyDirectory() throws Exception {
        Path dir = createDirectory(dir1().resolve("a"));
        Path file = createFile(dir1().resolve("a/child.txt"));
        delete(dir);
        assertFalse(exists(file, NOFOLLOW_LINKS));
        assertFalse(exists(dir, NOFOLLOW_LINKS));
    }

    @Test
    public void deletesEmptyDirectory() throws Exception {
        Path dir = createDirectory(dir1().resolve("a"));
        delete(dir);
        assertFalse(exists(dir, NOFOLLOW_LINKS));
    }

    @Test
    public void deletesSymbolicLinkButNotLinkedFile() throws Exception {
        Path a = createFile(dir1().resolve("a"));
        Path b = createSymbolicLink(dir1().resolve("b"), a);
        assertTrue(exists(a, NOFOLLOW_LINKS));
        assertTrue(exists(b, NOFOLLOW_LINKS));
        delete(b);
        assertFalse(exists(b, NOFOLLOW_LINKS));
        assertTrue(exists(a, NOFOLLOW_LINKS));
    }

    private void delete(Path file) throws Exception {
        create(singleton(file)).execute();
    }

    private Delete create(Collection<? extends Path> resources) {
        return new Delete(resources);
    }

}
