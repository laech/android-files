package l.files.ui.browser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.local.ResourceBaseTest;

import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import static l.files.fs.LinkOption.NOFOLLOW;

abstract class FileSortTest extends ResourceBaseTest {

    protected final void testSortMatches(
            Comparator<FileListItem.File> comparator, Resource... expectedOrder) throws IOException {
        List<FileListItem.File> expected = mapData(expectedOrder);
        ArrayList<FileListItem.File> actual = new ArrayList<>(expected);
        shuffle(actual);
        sort(actual, comparator);
        assertEquals(expected, actual);
    }

    private List<FileListItem.File> mapData(Resource... resources) throws IOException {
        List<FileListItem.File> expected = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            ResourceStatus stat = resource.readStatus(NOFOLLOW);
            expected.add(FileListItem.File.create(resource, stat, stat));
        }
        return expected;
    }
}
