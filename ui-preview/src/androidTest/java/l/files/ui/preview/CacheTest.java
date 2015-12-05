package l.files.ui.preview;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Random;

import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.local.LocalPath;

import static l.files.fs.Files.setLastModifiedTime;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class CacheTest<V, C extends Cache<V>> {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    C cache;
    Random random;

    Path file;
    Stat stat;

    @Before
    public void setUp() throws Exception {
        cache = newCache();
        random = new Random();

        java.io.File localFile = folder.newFile("0");
        file = LocalPath.of(localFile);
        stat = Files.stat(file, FOLLOW);
    }

    @Test
    public void gets_what_has_put_in() throws Exception {
        Rect constraint = newConstraint();
        V value = newValue();
        cache.put(file, stat, constraint, value);
        assertValueEquals(value, cache.get(file, stat, constraint, true));
    }

    @Test
    public void gets_null_when_time_changes() throws Exception {
        Rect constraint = newConstraint();
        V value = newValue();
        cache.put(file, stat, constraint, value);

        setLastModifiedTime(file, NOFOLLOW, Instant.EPOCH);
        assertNull(cache.get(file, Files.stat(file, NOFOLLOW), constraint, true));
    }

    @Test
    public void gets_old_value_if_stat_not_provided() throws Exception {

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
        return LocalPath.of(folder.getRoot());
    }
}
