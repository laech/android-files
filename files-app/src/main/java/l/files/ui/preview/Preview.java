package l.files.ui.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.View;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class Preview
{
    private static final int SIZE = 5000;
    static final LruCache<String, String> errors = new LruCache<>(SIZE);
    static final LruCache<String, ScaledSize> sizes = new LruCache<>(SIZE);

    final LruCache<String, Bitmap> memCache;

    final int maxWidth;
    final int maxHeight;

    @SuppressWarnings("unchecked")
    public Preview(
            final Context context,
            final LruCache<? super String, Bitmap> cache,
            final int maxWidth,
            final int maxHeight)
    {
        requireNonNull(cache, "cache");
        checkArgument(maxWidth > 0);
        checkArgument(maxHeight > 0);

        this.memCache = (LruCache<String, Bitmap>) cache;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void set(
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback)
    {
        DecodeChain.run(this, res, stat, view, callback);
    }

    String key(final Resource res, final Stat stat)
    {
        return res.scheme() + "://" + res.path()
                + "?bounds=" + maxWidth + "x" + maxHeight
                + "&mtime=" + stat.modified().to(MILLISECONDS);
    }

}
