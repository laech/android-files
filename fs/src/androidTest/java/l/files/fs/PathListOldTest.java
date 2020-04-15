package l.files.fs;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class PathListOldTest extends PathBaseTest {

    @Test
    public void list_follows_symbolic_link() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path a = dir.concat("a").createFile();
        Path b = dir.concat("b").createDirectory();
        Path c = dir.concat("c").createSymbolicLink(a);
        Path link = dir1().concat("link").createSymbolicLink(dir);

        List<Path> expected = asList(
                a.rebase(dir, link),
                b.rebase(dir, link),
                c.rebase(dir, link)
        );

        List<Path> actual = sortByName(link.list(new ArrayList<>()));
        assertEquals(expected, actual);
    }

    @Test
    public void list_directory_content() throws Exception {
        Path a = dir1().concat("a").createFile();
        Path b = dir1().concat("b").createDirectory();
        List<Path> expected = asList(a, b);
        List<Path> actual = sortByName(dir1().list(new ArrayList<>()));
        assertEquals(expected, actual);
    }

    private List<Path> sortByName(List<Path> paths) throws IOException {
        Collections.sort(paths, (a, b) -> a.name().compareTo(b.name()));
        return paths;
    }

    @Test
    public void access_denied_failure_if_no_permission_to_read() throws Exception {
        Paths.removePermissions(dir1(), Permission.read());
        listWillFail(dir1(), AccessDenied.class);
    }

    @Test
    public void not_such_entry_if_directory_does_not_exist() throws Exception {
        listWillFail(dir1().concat("missing"), NoSuchEntry.class);
    }

    @Test
    public void not_such_entry_if_symbolic_link_to_directory_does_not_exist() throws Exception {
        Path missing = dir1().concat("missing");
        Path link = dir1().concat("link").createSymbolicLink(missing);
        listWillFail(link, NoSuchEntry.class);
    }

    @Test
    public void not_such_entry_if_path_is_empty() throws Exception {
        listWillFail(Path.of(""), NoSuchEntry.class);
    }

    @Test
    public void not_directory_failure_if_path_is_file() throws Exception {
        Path file = dir1().concat("file").createFile();
        listWillFail(file, NotDirectory.class);
    }

    @Test
    public void not_directory_failure_if_path_is_symbolic_link_to_file() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);
        listWillFail(link, NotDirectory.class);
    }

    private static void listWillFail(
            Path path,
            Class<? extends IOException> expected
    ) throws IOException {

        try {
            path.list(new ArrayList<>());
            fail("Expected: " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }
}
