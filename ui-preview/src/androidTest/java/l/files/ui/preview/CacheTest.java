package l.files.ui.preview;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Stat;

import static java.io.File.createTempFile;
import static l.files.fs.Files.setLastModifiedTime;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class CacheTest<V, C extends Cache<V>> extends TestCase {

    C cache;
    Random random;

    Path file;
    Stat stat;

    private File tempDir;

    private File createTempDir() throws IOException {
        File dir = File.createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        tempDir = createTempDir();

        cache = newCache();
        random = new Random();

        File localFile = createTempFile("123", null, tempDir);
        file = Paths.get(localFile);
        stat = Files.stat(file, FOLLOW);
    }

    public void test_gets_what_has_put_in() throws Exception {
        Rect constraint = newConstraint();
        V value = newValue();
        cache.put(file, stat, constraint, value);
        assertValueEquals(value, cache.get(file, stat, constraint, true));
    }

    public void test_gets_null_when_time_changes() throws Exception {
        Rect constraint = newConstraint();
        V value = newValue();
        cache.put(file, stat, constraint, value);

        setLastModifiedTime(file, NOFOLLOW, Instant.EPOCH);
        assertNull(cache.get(file, Files.stat(file, NOFOLLOW), constraint, true));
    }

    public void test_gets_old_value_if_stat_not_provided() throws Exception {

        Rect constraint = newConstraint();
        V value = newValue();
        cache.put(file, stat, constraint, value);

        assertValueEquals(value, cache.get(file, stat, constraint, false));
    }

    abstract C newCache();

    abstract V newValue();

    Rect newConstraint() {
        return Rect.of(
                random.nextInt(100) + 1,
                random.nextInt(100) + 1
        );
    }

    void assertValueEquals(V a, V b) {
        assertEquals(a, b);
    }

    Path mockCacheDir() {
        return Paths.get(tempDir);
    }
}
