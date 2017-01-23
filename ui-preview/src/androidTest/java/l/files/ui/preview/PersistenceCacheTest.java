package l.files.ui.preview;

import org.junit.Test;

import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;

import static android.test.MoreAsserts.assertNotEqual;
import static org.junit.Assert.assertNull;

public abstract class PersistenceCacheTest<V, C extends PersistenceCache<V>>
        extends MemCacheTest<Path, V, C> {

    @Test
    public void removed_item_will_not_be_persisted() throws Exception {

        Rect constraint = newConstraint();
        V value = newValue();

        C c1 = newCache();
        c1.put(file, stat, constraint, value);
        c1.writeIfNeeded();

        C c2 = newCache();
        c2.readIfNeeded();
        assertValueEquals(value, c2.get(file, stat, constraint, true));
        c2.remove(file, stat, constraint);
        c2.writeIfNeeded();

        C c3 = newCache();
        c3.readIfNeeded();
        assertNull(c3.get(file, stat, constraint, true));
    }

    @Test
    public void reads_persisted_cache_from_put() throws Exception {

        Rect constraint = newConstraint();
        V value = newValue();

        C c1 = newCache();
        c1.put(file, stat, constraint, value);
        c1.writeIfNeeded();

        C c2 = newCache();
        assertNull(c2.get(file, stat, constraint, true));
        c2.readIfNeeded();
        assertValueEquals(value, c2.get(file, stat, constraint, true));
    }

    @Test
    public void constraint_is_not_used_as_part_of_key() throws Exception {

        V value = newValue();
        cache.put(file, stat, newConstraint(), value);
        assertValueEquals(value, cache.get(file, stat, newConstraint(), true));
        assertValueEquals(value, cache.get(file, stat, newConstraint(), true));
        assertValueEquals(value, cache.get(file, stat, newConstraint(), true));
        assertNotEqual(newConstraint(), newConstraint());
    }

}
