package l.files.ui.preview;

import android.util.LruCache;

import com.google.common.base.Stopwatch;
import com.google.common.net.MediaType;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.MagicDetector;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class MediaTypes
{
    private static final Logger log = Logger.get(MediaTypes.class);

    private static final int SIZE = 5000;
    private static final LruCache<String, MediaType> cache = new LruCache<>(SIZE);
    private static final LruCache<String, String> errors = new LruCache<>(SIZE);

    private MediaTypes()
    {
    }

    @Nullable
    static MediaType cached(final Resource res, final Stat stat)
    {
        return cache.get(key(res, stat));
    }

    @Nullable
    static MediaType detect(final Resource res, final Stat stat)
    {
        final String key = key(res, stat);
        final MediaType cached = cache.get(key);
        if (cached != null)
        {
            return cached;
        }

        if (errors.get(key) != null)
        {
            return null;
        }

        try
        {
            final Stopwatch watch = Stopwatch.createStarted();
            final MediaType media = MagicDetector.INSTANCE.detect(res);
            cache.put(key, media);
            log.debug("decode %s took %s for %s", media, watch, res);
            return media;
        }
        catch (final IOException e)
        {
            errors.put(key, key);
            log.warn(e);
            return null;
        }
    }

    private static String key(final Resource res, final Stat stat)
    {
        return res.scheme() + "://" + res.path()
                + "?mtime=" + stat.modificationTime().to(MILLISECONDS);
    }
}
