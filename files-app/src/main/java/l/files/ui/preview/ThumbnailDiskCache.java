package l.files.ui.preview;

import android.graphics.Bitmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

import l.files.common.graphics.Rect;
import l.files.fs.DirectoryNotEmpty;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.fs.Visitor;
import l.files.logging.Logger;

import static android.graphics.Bitmap.CompressFormat.WEBP;
import static android.graphics.BitmapFactory.decodeStream;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;

final class ThumbnailDiskCache extends Cache<Bitmap> {

    private static final Logger log = Logger.get(ThumbnailDiskCache.class);

    // No need to set UncaughtExceptionHandler to terminate
    // on exception already set by Android
    private static final Executor executor = newFixedThreadPool(2);

    /**
     * Place a dummy byte at the beginning of the cache files,
     * make them unrecognizable as image files, as to make them
     * not previewable, so won't get into the situation of previewing
     * the cache, save the thumbnail of the cache, preview the cache
     * of the cache...
     */
    private static final int DUMMY_BYTE = 0;

    final File cacheDir;

    ThumbnailDiskCache(File cacheDir) {
        this.cacheDir = cacheDir.resolve("thumbnails");
    }

    public void cleanupAsync() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    cleanup();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        });
    }

    void cleanup() throws IOException {
        if (!cacheDir.exists(FOLLOW)) {
            log.verbose("cache dir does not exists, nothing to cleanup");
            return;
        }

        log.verbose("cleanup");

        cacheDir.traverse(NOFOLLOW, new Visitor.Base() {

            final long now = currentTimeMillis();

            @Override
            public Result onPostVisit(File res) throws IOException {

                Stat stat = res.stat(NOFOLLOW);
                if (stat.isDirectory()) {

                    try {
                        res.delete();
                        log.debug("Deleted empty cache directory %s", res);
                    } catch (DirectoryNotEmpty ignore) {
                    }

                } else {

                    long lastAccessedMillis = stat.lastAccessedTime().to(MILLISECONDS);
                    if (MILLISECONDS.toDays(now - lastAccessedMillis) > 30) {
                        res.delete();
                        log.debug("Deleted old cache file %s", res);
                    }

                }

                return CONTINUE;
            }

        });
    }

    File cacheFile(File res, Stat stat, Rect constraint) {
        return cacheDir.resolve(res.scheme()
                + "/" + res.path()
                + "_" + stat.lastModifiedTime().seconds()
                + "_" + stat.lastModifiedTime().nanos()
                + "_" + constraint.width()
                + "_" + constraint.height());
    }

    @Override
    Bitmap get(
            File res,
            Stat stat,
            Rect constraint) throws IOException {

        log.verbose("read bitmap %s", res);
        File cache = cacheFile(res, stat, constraint);
        try (InputStream in = new BufferedInputStream(cache.input())) {
            in.read(); // read DUMMY_BYTE

            Bitmap bitmap = decodeStream(in);

            if (bitmap == null) {
                return null;
            }

            if (bitmap.getWidth() > constraint.width() ||
                    bitmap.getHeight() > constraint.height()) {
                bitmap.recycle();
                return null;
            }

            try {
                cache.setLastAccessedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
            } catch (IOException ignore) {
            }

            return bitmap;

        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    Snapshot<Bitmap> put(
            File res,
            Stat stat,
            Rect constraint,
            Bitmap bitmap) throws IOException {

        File cache = cacheFile(res, stat, constraint);
        cache.createFiles();
        try (OutputStream out = new BufferedOutputStream(cache.output())) {
            out.write(DUMMY_BYTE);
            bitmap.compress(WEBP, 100, out);
            log.verbose("write %s", res);
        }
        return null;
    }

    public void putAsync(File res, Stat stat, Rect constraint, Bitmap bitmap) {
        executor.execute(new WriteBitmap(
                res, stat, constraint, new WeakReference<>(bitmap)));
    }

    private final class WriteBitmap implements Runnable {
        private final File res;
        private final Stat stat;
        private final Rect constraint;
        private final WeakReference<Bitmap> ref;

        private WriteBitmap(
                File res,
                Stat stat,
                Rect constraint,
                WeakReference<Bitmap> ref) {
            this.res = requireNonNull(res);
            this.stat = requireNonNull(stat);
            this.constraint = requireNonNull(constraint);
            this.ref = requireNonNull(ref);
        }

        @Override
        public void run() {
            Bitmap bitmap = ref.get();
            if (bitmap != null) {
                try {
                    put(res, stat, constraint, bitmap);
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
    }

}
