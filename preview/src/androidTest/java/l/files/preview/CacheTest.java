package l.files.preview;

import java.util.Random;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.testing.fs.FileBaseTest;

import static java.lang.System.currentTimeMillis;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class CacheTest<V, C extends Cache<V>>
        extends FileBaseTest {

    C cache;
    Random random;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cache = newCache();
        random = new Random();
    }

    public void test_gets_what_has_put_in() throws Exception {
        File res = dir1();
        Stat stat = res.stat(NOFOLLOW);
        Rect constraint = newConstraint();
        V value = newValue();
        cache.put(res, stat, constraint, value);
        assertValueEquals(value, cache.get(res, stat, constraint));
    }

    public void test_gets_null_when_time_changes() throws Exception {
        File res = dir1().resolve("a").createFile();
        Stat stat = res.stat(NOFOLLOW);
        Rect constraint = newConstraint();
        V value = newValue();
        cache.put(res, stat, constraint, value);

        res.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis() + 9999));
        assertNull(cache.get(res, res.stat(NOFOLLOW), constraint));
    }

    abstract C newCache();

    abstract V newValue();

    Rect newConstraint() {
        return Rect.of(
                random.nextInt(100) + 1,
                random.nextInt(100) + 1);
    }

    void assertValueEquals(V a, V b) {
        assertEquals(a, b);
    }

}
