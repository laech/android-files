package l.files.fs.local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import l.files.fs.NotDirectoryException;

import static java.util.Arrays.asList;

public final class LocalResourceStreamTest extends ResourceBaseTest {

    public void testReturnCorrectEntries() throws Exception {
        LocalResource a = dir1().resolve("a").createFile();
        LocalResource b = dir1().resolve("b").createDirectory();
        LocalResource c = dir1().resolve("c").createSymbolicLink(a);

        try (LocalResourceStream stream = LocalResourceStream.open(dir1())) {
            List<LocalPathEntry> expected = asList(
                    LocalPathEntry.create(a, a.readStatus(false).getInode(), false),
                    LocalPathEntry.create(b, b.readStatus(false).getInode(), true),
                    LocalPathEntry.create(c, c.readStatus(false).getInode(), false)
            );
            List<LocalPathEntry> actual = new ArrayList<>();
            for (LocalPathEntry entry : stream) {
                actual.add(entry);
            }
            assertEquals(expected, actual);
        }
    }

    public void testIteratorReturnsFalseIfNoNextElement() throws Exception {
        try (LocalResourceStream stream = LocalResourceStream.open(dir1())) {
            Iterator<?> iterator = stream.iterator();
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }
    }

    public void testIteratorThrowsNoSuchElementExceptionOnEmpty() throws Exception {
        try (LocalResourceStream stream = LocalResourceStream.open(dir1())) {
            stream.iterator().next();
            fail();
        } catch (NoSuchElementException e) {
            // Pass
        }
    }

    public void testIteratorMethodCannotBeReused() throws Exception {
        try (LocalResourceStream stream = LocalResourceStream.open(dir1())) {
            stream.iterator();
            try {
                stream.iterator();
                fail();
            } catch (IllegalStateException e) {
                // Pass
            }
        }
    }

    public void testFailIfNotDirectorxyIsSymbolicLink() throws Exception {
        LocalResource dir = dir1().resolve("dir").createDirectory();
        LocalResource link = dir1().resolve("link").createSymbolicLink(dir);
        try (LocalResourceStream ignore = LocalResourceStream.open(link)) {
            fail();
        } catch (NotDirectoryException e) {
            // Pass
        }
    }

    public void testFailIfNotDirectoryIsFile() throws Exception {
        LocalResource file = dir1().resolve("file").createFile();
        try (LocalResourceStream ignore = LocalResourceStream.open(file)) {
            fail();
        } catch (NotDirectoryException e) {
            // Pass
        }
    }

}
