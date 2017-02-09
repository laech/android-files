package l.files.fs;

import com.google.common.base.Strings;

import org.junit.Test;

import java.io.IOException;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.InvalidArgument;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static junit.framework.Assert.fail;
import static linux.Limits.NAME_MAX;

public final class PathReadSymbolicLinkTest extends PathBaseTest {

    @Test
    public void access_denied_failure_when_parent_has_no_search_permission()
            throws Exception {

        Path parent = dir1();
        Path link = parent.concat("link");
        Paths.removePermissions(parent, Permission.execute());
        readSymbolicLinkWillFail(link, AccessDenied.class);
    }

    @Test
    public void invalid_argument_failure_when_path_is_directory()
            throws Exception {

        Path dir = dir1().concat("dir").createDirectory();
        readSymbolicLinkWillFail(dir, InvalidArgument.class);
    }

    @Test
    public void invalid_argument_failure_when_path_is_file()
            throws Exception {

        Path file = dir1().concat("file").createFile();
        readSymbolicLinkWillFail(file, InvalidArgument.class);
    }

    @Test
    public void too_many_symbolic_links_failure() throws Exception {
        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        Path inLoop = loop.concat("in-loop");
        readSymbolicLinkWillFail(inLoop, TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure() throws Exception {
        Path nameTooLong = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        readSymbolicLinkWillFail(nameTooLong, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_due_to_path_does_not_exist()
            throws Exception {

        Path missing = dir1().concat("missing");
        readSymbolicLinkWillFail(missing, NoSuchEntry.class);
    }

    @Test
    public void not_directory_failure_when_parent_is_not_directory()
            throws Exception {

        Path file = dir1().concat("file").createFile();
        Path invalid = file.concat("invalid");
        readSymbolicLinkWillFail(invalid, NotDirectory.class);
    }

    private void readSymbolicLinkWillFail(
            Path link,
            Class<? extends IOException> expected
    ) throws IOException {

        try {
            link.readSymbolicLink();
            fail("Expected " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }

}
