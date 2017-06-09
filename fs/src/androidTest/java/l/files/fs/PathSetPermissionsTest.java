package l.files.fs;

import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static linux.Limits.NAME_MAX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class PathSetPermissionsTest extends PathBaseTest {

    @Test
    public void sets_correct_permissions() throws Exception {
        for (Set<Permission> permissions : asList(
                Permission.all(),
                Permission.read(),
                Permission.write(),
                Permission.execute()
        )) {
            dir1().setPermissions(permissions);
            assertEquals(permissions, dir1().stat(NOFOLLOW).permissions());
        }
    }

    @Test
    public void access_denied_if_parent_is_not_searchable() throws Exception {
        Paths.removePermissions(dir1(), Permission.execute());
        setPermissionWillFail(dir1().concat("a"), AccessDenied.class);
    }

    @Test
    public void too_many_symbolic_links_failure() throws Exception {
        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        setPermissionWillFail(loop.concat("a"), TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure() throws Exception {
        Path path = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        setPermissionWillFail(path, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_if_file_does_not_exist() throws Exception {
        Path path = dir1().concat("missing");
        setPermissionWillFail(path, NoSuchEntry.class);
    }


    @Test
    public void no_such_entry_failure_if_path_is_empty() throws Exception {
        setPermissionWillFail(Path.of(""), NoSuchEntry.class);
    }

    @Test
    public void not_directory_failure_if_parent_is_file() throws Exception {
        Path path = dir1().concat("file").createFile();
        setPermissionWillFail(path.concat("invalid"), NotDirectory.class);
    }

    private static void setPermissionWillFail(
            Path path,
            Class<? extends IOException> expected
    ) throws IOException {

        try {
            path.setPermissions(EnumSet.of(OWNER_READ, OWNER_WRITE));
            fail("Expected: " + expected);
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }
}