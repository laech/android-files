package l.files.fs;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static org.junit.Assert.assertEquals;

public final class PathCreateDirectoryWithPermissionTest extends PathBaseTest {

    @Test
    public void create_directory_with_permissions() throws Exception {

        for (Set<Permission> permissions : asList(
                Permission.none(),
                EnumSet.of(OWNER_READ),
                EnumSet.of(OWNER_WRITE),
                EnumSet.of(OWNER_EXECUTE),
                EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))) {

            String name = String.valueOf(Math.random());
            Path dir = dir1().concat(name).createDirectory(permissions);
            Stat stat = dir.stat(NOFOLLOW);
            assertEquals(permissions, stat.permissions());
        }
    }

}
