package l.files.ui.preview;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Random;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Stat;

import static java.lang.System.currentTimeMillis;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class CacheTest<V, C extends Cache<V>> {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    C cache;
    Random random;

    File file;
    Stat stat;

    @Before
    public void setUp() throws Exception {
        cache = newCache();
        random = new Random();

        java.io.File localFile = folder.newFile("0");
        file = new TestFile(localFile);
        stat = new TestStat(localFile);
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

        file.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis() + 9999));
        assertNull(cache.get(file, stat, constraint, true));
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

    File mockCacheDir() {
        return new TestFile(folder.getRoot());
    }
}
