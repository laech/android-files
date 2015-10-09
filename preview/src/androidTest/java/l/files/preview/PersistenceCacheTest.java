package l.files.preview;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.test.MoreAsserts.assertNotEqual;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class PersistenceCacheTest<V, C extends PersistenceCache<V>>
        extends MemCacheTest<V, C> {

    public void test_removed_item_will_not_be_persisted() throws Exception {
        File res = dir1();
        Stat stat = res.stat(NOFOLLOW);
        Rect constraint = newConstraint();
        V value = newValue();

        C c1 = newCache();
        c1.put(res, stat, constraint, value);
        c1.writeIfNeeded();

        C c2 = newCache();
        c2.readIfNeeded();
        assertValueEquals(value, c2.get(res, stat, constraint));
        c2.remove(res, stat, constraint);
        c2.writeIfNeeded();

        C c3 = newCache();
        c3.readIfNeeded();
        assertNull(c3.get(res, stat, constraint));
    }

    public void test_reads_persisted_cache_from_put() throws Exception {
        File res = dir1();
        Stat stat = res.stat(NOFOLLOW);
        Rect constraint = newConstraint();
        V value = newValue();

        C c1 = newCache();
        c1.put(res, stat, constraint, value);
        c1.writeIfNeeded();

        C c2 = newCache();
        assertNull(c2.get(res, stat, constraint));
        c2.readIfNeeded();
        assertValueEquals(value, c2.get(res, stat, constraint));
    }

    public void test_constraint_is_not_used_as_part_of_key() throws Exception {
        File res = dir1();
        Stat stat = res.stat(NOFOLLOW);
        V value = newValue();
        cache.put(res, stat, newConstraint(), value);
        assertValueEquals(value, cache.get(res, stat, newConstraint()));
        assertValueEquals(value, cache.get(res, stat, newConstraint()));
        assertValueEquals(value, cache.get(res, stat, newConstraint()));
        assertNotEqual(newConstraint(), newConstraint());
    }

}
