package l.files.ui.preview;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

import l.files.common.graphics.ScaledSize;
import l.files.common.graphics.drawable.SizedColorDrawable;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Color.TRANSPARENT;
import static android.view.View.VISIBLE;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class Preview
{
    private static final int SIZE = 5000;
    static final LruCache<String, String> errors = new LruCache<>(SIZE);
    static final LruCache<String, ScaledSize> sizes = new LruCache<>(SIZE);

    private final LruCache<String, Bitmap> cache;

    final int maxWidth;
    final int maxHeight;

    @SuppressWarnings("unchecked")
    public Preview(
            final LruCache<? super String, Bitmap> cache,
            final int maxWidth,
            final int maxHeight)
    {
        requireNonNull(cache, "cache");
        checkArgument(maxWidth > 0);
        checkArgument(maxHeight > 0);

        this.cache = (LruCache<String, Bitmap>) cache;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void set(
            final ImageView view,
            final Resource res,
            final Stat stat)
    {
        DecodeChain.run(this, view, res, stat);
    }

    void cache(final String key, final Bitmap bitmap)
    {
        cache.put(key, bitmap);
    }

    boolean setCached(final String key, final ImageView image)
    {
        final Bitmap bitmap = cache.get(key);
        if (bitmap != null)
        {
            image.setImageBitmap(bitmap);
            image.setVisibility(VISIBLE);
            return true;
        }
        return false;
    }

    String key(final Resource res, final Stat stat)
    {
        return res.scheme() + "://" + res.path()
                + "?bounds=" + maxWidth + "x" + maxHeight
                + "&mtime=" + stat.modified().to(MILLISECONDS);
    }

    static SizedColorDrawable newPlaceholder(final ScaledSize size)
    {
        return new SizedColorDrawable(
                TRANSPARENT,
                size.scaledWidth(),
                size.scaledHeight());
    }
}
