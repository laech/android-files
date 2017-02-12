package l.files.fs;

import com.google.common.base.Strings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static java.util.Arrays.asList;
import static linux.Limits.NAME_MAX;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public final class PathDeleteFailureTest extends PathBaseTest {

    private final PathCreation creation;

    public PathDeleteFailureTest(PathCreation creation) {
        this.creation = creation;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {PathCreation.FILE},
                {PathCreation.DIRECTORY},
                {PathCreation.SYMBOLIC_LINK},
        });
    }

    @Test
    public void access_denied_failure_if_parent_is_not_writable()
            throws Exception {
        deleteWillFailWithAccessDeniedDueToParentPermission(Permission.write());
    }

    @Test
    public void access_denied_failure_if_parent_is_not_searchable()
            throws Exception {
        deleteWillFailWithAccessDeniedDueToParentPermission(Permission.execute());
    }

    private void deleteWillFailWithAccessDeniedDueToParentPermission(
            Set<Permission> permissionsToRemoveFromParent
    ) throws Exception {

        Path path = dir1().concat("a");
        creation.createUsingOurCodeAssertResult(path);
        Paths.removePermissions(path.parent(), permissionsToRemoveFromParent);
        deleteWillFail(path, AccessDenied.class);
    }

    @Test
    public void too_many_symbolic_links_failure_due_to_loop_in_parent()
            throws Exception {

        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        Path invalid = loop.concat("file");
        deleteWillFail(invalid, TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure() throws Exception {
        Path path = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        deleteWillFail(path, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_due_to_path_does_not_exist()
            throws Exception {

        Path path = dir1().concat("a");
        deleteWillFail(path, NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_due_to_parent_symbolic_link_target_does_not_exist()
            throws Exception {

        Path target = dir2().concat("missing");
        Path link = dir1().concat("link").createSymbolicLink(target);
        deleteWillFail(link.concat("invalid"), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_due_to_path_is_empty() throws Exception {
        deleteWillFail(Path.of(""), NoSuchEntry.class);
    }

    @Test
    public void not_directory_failure_if_parent_is_not_directory()
            throws Exception {

        Path path = dir1().concat("parent").createFile().concat("a");
        deleteWillFail(path, NotDirectory.class);
    }

    private static void deleteWillFail(
            Path path,
            Class<? extends IOException> expected
    ) throws IOException {

        try {
            path.delete();
            fail("Expected: " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }
}
