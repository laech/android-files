package l.files.ui.browser;

import java.io.IOException;
import java.util.Locale;

import l.files.fs.Instant;
import l.files.fs.Resource;

import static l.files.fs.LinkOption.NOFOLLOW;

public final class FileSortDateTest extends FileSortTest {

    public void testSortByDateDesc() throws Exception {
        testSortMatches(FileSort.MODIFIED.newComparator(Locale.getDefault()),
                createDirModified("b", Instant.of(1, 3)),
                createFileModified("a", Instant.of(1, 2)),
                createDirModified("c", Instant.of(1, 1)));
    }

    public void testSortByNameIfDatesEqual() throws Exception {
        testSortMatches(FileSort.MODIFIED.newComparator(Locale.getDefault()),
                createFileModified("a", Instant.of(1, 1)),
                createDirModified("b", Instant.of(1, 1)),
                createFileModified("c", Instant.of(1, 1)));
    }

    private Resource createFileModified(String name, Instant instant) throws IOException {
        Resource file = dir1().resolve(name).createFile();
        return setModified(file, instant);
    }

    private Resource createDirModified(String name, Instant instant) throws IOException {
        Resource dir = dir1().resolve(name).createDirectory();
        return setModified(dir, instant);
    }

    private Resource setModified(Resource resource, Instant instant) throws IOException {
        resource.setModificationTime(NOFOLLOW, instant);
        return resource;
    }

}
