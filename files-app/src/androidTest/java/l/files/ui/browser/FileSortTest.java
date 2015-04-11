package l.files.ui.browser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import l.files.common.testing.FileBaseTest;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.local.LocalResource;
import l.files.fs.local.LocalResourceStatus;

import static java.util.Collections.shuffle;
import static java.util.Collections.sort;

abstract class FileSortTest extends FileBaseTest {

    protected final void testSortMatches(
            Comparator<FileListItem.File> comparator, File... expectedOrder) throws IOException {
        List<FileListItem.File> expected = mapData(expectedOrder);
        ArrayList<FileListItem.File> actual = new ArrayList<>(expected);
        shuffle(actual);
        sort(actual, comparator);
        assertEquals(expected, actual);
    }

    private List<FileListItem.File> mapData(File... files) throws IOException {
        List<FileListItem.File> expected = new ArrayList<>(files.length);
        for (File file : files) {
            LocalResource resource = LocalResource.create(file);
            LocalResourceStatus stat = LocalResourceStatus.stat(resource, false);
            expected.add(FileListItem.File.create(resource, stat, stat));
        }
        return expected;
    }
}
