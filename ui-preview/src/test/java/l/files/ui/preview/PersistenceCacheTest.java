package l.files.ui.preview;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public abstract class PersistenceCacheTest<V, C extends PersistenceCache<V>>
        extends MemCacheTest<V, C> {

    @Test
    public void removed_item_will_not_be_persisted() throws Exception {

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

    @Test
    public void reads_persisted_cache_from_put() throws Exception {

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

    @Test
    public void constraint_is_not_used_as_part_of_key() throws Exception {

        V value = newValue();
        cache.put(res, stat, newConstraint(), value);
        assertValueEquals(value, cache.get(res, stat, newConstraint()));
        assertValueEquals(value, cache.get(res, stat, newConstraint()));
        assertValueEquals(value, cache.get(res, stat, newConstraint()));
        assertNotEquals(newConstraint(), newConstraint());
    }

}
