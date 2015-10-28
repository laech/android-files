package l.files.ui.preview;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class MemCacheTest<V, C extends MemCache<V>>
        extends CacheTest<V, C> {

    @Test
    public void removed_item_no_longer_available() throws Exception {
        Rect constraint = newConstraint();
        V value = newValue();
        assertNull(cache.remove(res, stat, constraint));

        cache.put(res, stat, constraint, value);
        assertEquals(value, cache.remove(res, stat, constraint).get());
        assertNull(cache.remove(res, stat, constraint));
        assertNull(cache.get(res, stat, constraint));
    }

}
