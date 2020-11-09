package l.files.fs;

import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;
import org.junit.Test;

import java.io.IOException;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static linux.Limits.NAME_MAX;
import static org.junit.Assert.fail;

public final class PathStatTest extends PathBaseTest {

    @Test
    public void too_many_symbolic_links_failure() throws Exception {
        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        statWillFail(loop.concat("a"), TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure() throws Exception {
        Path path = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        statWillFail(path, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_if_parent_does_not_exist()
        throws Exception {
        Path path = dir1().concat("a/b");
        statWillFail(path, NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_self_does_not_exist()
        throws Exception {
        Path path = dir1().concat("a");
        statWillFail(path, NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_path_is_empty() throws Exception {
        statWillFail(Path.of(""), NoSuchEntry.class);
    }

    @Test
    public void not_directory_failure_if_parent_is_file() throws Exception {
        Path file = dir1().concat("file").createFile();
        statWillFail(file.concat("invalid"), NotDirectory.class);
    }

    @Test
    public void not_directory_failure_if_parent_is_symbolic_link_to_file()
        throws Exception {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);
        statWillFail(link.concat("invalid"), NotDirectory.class);
    }

    private static void statWillFail(
        Path path,
        Class<? extends IOException> expected
    ) throws IOException {
        statWillFail(path, NOFOLLOW, expected);
        statWillFail(path, FOLLOW, expected);
    }

    private static void statWillFail(
        Path path,
        LinkOption option,
        Class<? extends IOException> expected
    ) throws IOException {

        try {
            path.stat(option);
            fail("Expected: " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }
}
