package l.files.operations;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.common.testing.FileBaseTest;
import l.files.fs.Path;
import l.files.fs.local.LocalPath;

import static java.util.Arrays.asList;

public final class DeleteTest extends FileBaseTest {

    public void testNotifiesListener() throws Exception {
        File src = tmp().createDir("a");
        tmp().createFile("a/b");

        Set<File> expected = new HashSet<>(asList(
                tmp().get("a"),
                tmp().get("a/b")
        ));

        Delete delete = create(asList(LocalPath.of(src)));
        delete.execute();

        assertEquals(delete.getDeletedItemCount(), expected.size());
    }

    public void testDeletesFile() throws Exception {
        File file = tmp().createFile("a");
        delete(file);
        assertFalse(file.exists());
    }

    public void testDeletesNonEmptyDirectory() throws Exception {
        File dir = tmp().createDir("a");
        File file = tmp().createFile("a/child.txt");
        delete(dir);
        assertFalse(file.exists());
        assertFalse(dir.exists());
    }

    public void testDeletesEmptyDirectory() throws Exception {
        File dir = tmp().createDir("a");
        delete(dir);
        assertFalse(dir.exists());
    }

    public void testDeletesSymbolicLinkButNotLinkedFile() throws Exception {
        File a = tmp().createFile("a");
        File b = tmp().get("b");
        LocalPath.of(b).getResource().createSymbolicLink(LocalPath.of(a).getResource());
        assertTrue(a.exists());
        assertTrue(b.exists());
        delete(b);
        assertFalse(b.exists());
        assertTrue(a.exists());
    }

    public void testReturnsFailures() throws Exception {
        File a = tmp().createFile("a");
        assertTrue(tmp().get().setWritable(false));

        List<Failure> failures = null;
        try {
            delete(a);
            fail();
        } catch (FileException e) {
            failures = e.failures();
        }
        assertEquals(LocalPath.of(a.getPath()), failures.get(0).getPath());
        assertEquals(1, failures.size());
    }

    private void delete(File file) throws Exception {
        create(asList(LocalPath.of(file))).execute();
    }

    private Delete create(Iterable<? extends Path> paths) {
        return new Delete(paths);
    }
}
