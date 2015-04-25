package l.files.operations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.local.ResourceBaseTest;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class DeleteTest extends ResourceBaseTest {

    public void testNotifiesListener() throws Exception {
        Resource a = dir1().resolve("a").createDirectory();
        Resource b = dir1().resolve("a/b").createFile();

        Set<Resource> expected = new HashSet<>(asList(a, b));

        Delete delete = create(singletonList(a));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    public void testDeletesFile() throws Exception {
        Resource file = dir1().resolve("a").createFile();
        delete(file);
        assertFalse(file.exists(NOFOLLOW));
    }

    public void testDeletesNonEmptyDirectory() throws Exception {
        Resource dir = dir1().resolve("a").createDirectory();
        Resource file = dir1().resolve("a/child.txt").createFile();
        delete(dir);
        assertFalse(file.exists(NOFOLLOW));
        assertFalse(dir.exists(NOFOLLOW));
    }

    public void testDeletesEmptyDirectory() throws Exception {
        Resource dir = dir1().resolve("a").createDirectory();
        delete(dir);
        assertFalse(dir.exists(NOFOLLOW));
    }

    public void testDeletesSymbolicLinkButNotLinkedFile() throws Exception {
        Resource a = dir1().resolve("a").createFile();
        Resource b = dir1().resolve("b").createSymbolicLink(a);
        assertTrue(a.exists(NOFOLLOW));
        assertTrue(b.exists(NOFOLLOW));
        delete(b);
        assertFalse(b.exists(NOFOLLOW));
        assertTrue(a.exists(NOFOLLOW));
    }

    public void testReturnsFailures() throws Exception {
        Resource a = dir1().resolve("a").createFile();
        dir1().setPermissions(Collections.<Permission>emptySet());

        List<Failure> failures = null;
        try {
            delete(a);
            fail();
        } catch (FileException e) {
            failures = e.failures();
        }
        assertEquals(a, failures.get(0).getResource());
        assertEquals(1, failures.size());
    }

    private void delete(Resource resource) throws Exception {
        create(singleton(resource)).execute();
    }

    private Delete create(Iterable<? extends Resource> resources) {
        return new Delete(resources);
    }

}
