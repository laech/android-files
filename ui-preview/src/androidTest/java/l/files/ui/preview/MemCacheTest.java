package l.files.ui.preview;

import org.junit.Test;

import l.files.ui.base.graphics.Rect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class MemCacheTest<K, V, C extends MemCache<K, V>>
        extends CacheTest<V, C> {

    @Test
    public void removed_item_no_longer_available() throws Exception {
        Rect constraint = newConstraint();
        V value = newValue();
        assertNull(cache.remove(file, stat, constraint));

        cache.put(file, stat, constraint, value);
        assertEquals(value, cache.remove(file, stat, constraint).get());
        assertNull(cache.remove(file, stat, constraint));
        assertNull(cache.get(file, stat, constraint, true));
    }

}
