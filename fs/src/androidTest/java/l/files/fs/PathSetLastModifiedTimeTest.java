package l.files.fs;

import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static linux.Limits.NAME_MAX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public final class PathSetLastModifiedTimeTest extends PathBaseTest {

    @Test
    public void sets_correct_last_modified_time() throws Exception {
        Instant expect = newInstant();
        dir1().setLastModifiedTime(NOFOLLOW, expect);
        Instant actual = dir1().stat(NOFOLLOW).lastModifiedTime();
        assertEquals(expect, actual);
    }

    @Test
    public void set_last_modified_time_follow_symbolic_link() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);

        Instant fileTime = newInstant();
        Instant linkTime = link.stat(NOFOLLOW).lastModifiedTime();
        link.setLastModifiedTime(FOLLOW, fileTime);

        assertEquals(fileTime, file.stat(NOFOLLOW).lastModifiedTime());
        assertEquals(linkTime, link.stat(NOFOLLOW).lastModifiedTime());
        assertNotEquals(fileTime, linkTime);
    }

    @Test
    public void set_last_modified_time_no_follow_symbolic_link() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);

        Instant fileTime = file.stat(NOFOLLOW).lastModifiedTime();
        Instant linkTime = newInstant();

        link.setLastModifiedTime(NOFOLLOW, linkTime);

        assertEquals(fileTime, file.stat(NOFOLLOW).lastModifiedTime());
        assertEquals(linkTime, link.stat(NOFOLLOW).lastModifiedTime());
        assertNotEquals(fileTime, linkTime);
    }

    private Instant newInstant() {
        Random random = new Random();
        if (SDK_INT >= LOLLIPOP) {
            return Instant.of(random.nextInt(1_000_000), random.nextInt(999_999) + 1);
        } else {
            return Instant.of(random.nextInt(1_000_000), 0); // Nanos not supported
        }
    }

    @Test
    public void access_denied_failure_if_parent_is_not_searchable() throws Exception {
        Paths.removePermissions(dir1(), Permission.execute());
        setLastModifiedTimeWillFail(dir1().concat("a"), AccessDenied.class);
    }

    @Test
    public void too_many_symbolic_links_failure() throws Exception {
        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        setLastModifiedTimeWillFail(loop.concat("a"), TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure() throws Exception {
        Path path = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        setLastModifiedTimeWillFail(path, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_if_path_does_not_exist() throws Exception {
        setLastModifiedTimeWillFail(dir1().concat("a/b"), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_path_is_empty() throws Exception {
        setLastModifiedTimeWillFail(Path.of(""), NoSuchEntry.class);
    }

    @Test
    public void not_directory_failure_if_parent_is_file() throws Exception {
        Path path = dir1().concat("file").createFile().concat("a");
        setLastModifiedTimeWillFail(path, NotDirectory.class);
    }

    private static void setLastModifiedTimeWillFail(
            Path path,
            Class<? extends IOException> expected
    ) throws IOException {

        try {
            path.setLastModifiedTime(NOFOLLOW, EPOCH);
            fail("Expected: " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }

    }
}