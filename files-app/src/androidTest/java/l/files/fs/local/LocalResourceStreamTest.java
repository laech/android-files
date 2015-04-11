package l.files.fs.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class LocalResourceStreamTest extends FileBaseTest {

    public void testReturnCorrectEntries() throws Exception {
        File f1 = tmp().createFile("a");
        File f2 = tmp().createDir("b");
        File f3 = tmp().get("d");
        symlink(f1.getPath(), f3.getPath());

        try (LocalResourceStream stream = LocalResourceStream.open(tmpPath().getResource())) {
            List<LocalPathEntry> expected = asList(
                    LocalPathEntry.create(tmpPath().resolve("a").getResource(), lstat(f1.getPath()).getIno(), false),
                    LocalPathEntry.create(tmpPath().resolve("b").getResource(), lstat(f2.getPath()).getIno(), true),
                    LocalPathEntry.create(tmpPath().resolve("d").getResource(), lstat(f3.getPath()).getIno(), false)
            );
            List<LocalPathEntry> actual = new ArrayList<>();
            for (LocalPathEntry entry : stream) {
                actual.add(entry);
            }
            assertEquals(expected, actual);
        }
    }

    public void testIteratorReturnsFalseIfNoNextElement() throws Exception {
        try (LocalResourceStream stream = LocalResourceStream.open(tmpPath().getResource())) {
            Iterator<?> iterator = stream.iterator();
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }
    }

    public void testIteratorThrowsNoSuchElementExceptionOnEmpty() throws Exception {
        try (LocalResourceStream stream = LocalResourceStream.open(tmpPath().getResource())) {
            stream.iterator().next();
            fail();
        } catch (NoSuchElementException e) {
            // Pass
        }
    }

    public void testIteratorMethodCannotBeReused() throws Exception {
        try (LocalResourceStream stream = LocalResourceStream.open(tmpPath().getResource())) {
            stream.iterator();
            try {
                stream.iterator();
                fail();
            } catch (IllegalStateException e) {
                // Pass
            }
        }
    }

    private LocalPath tmpPath() {
        return LocalPath.of(tmp().get());
    }

}
