package l.files.preview;

import android.graphics.Bitmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

import l.files.fs.DirectoryNotEmpty;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.fs.Visitor;

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

    private static final int VERSION = 2;

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
                    e.printStackTrace();
                }
            }
        });
    }

    void cleanup() throws IOException {
        if (!cacheDir.exists(FOLLOW)) {
            return;
        }

        cacheDir.traverse(NOFOLLOW, new Visitor.Base() {

            final long now = currentTimeMillis();

            @Override
            public Result onPostVisit(File file) throws IOException {

                Stat stat = file.stat(NOFOLLOW);
                if (stat.isDirectory()) {

                    try {
                        file.delete();
                    } catch (DirectoryNotEmpty ignore) {
                    }

                } else {

                    long lastAccessedMillis = stat.lastAccessedTime().to(MILLISECONDS);
                    if (MILLISECONDS.toDays(now - lastAccessedMillis) > 30) {
                        file.delete();
                    }

                }

                return CONTINUE;
            }

        });
    }

    File cacheFile(File file, Stat stat, Rect constraint) {
        return cacheDir.resolve(file.scheme()
                + "/" + file.path()
                + "_" + stat.lastModifiedTime().seconds()
                + "_" + stat.lastModifiedTime().nanos()
                + "_" + constraint.width()
                + "_" + constraint.height());
    }

    @Override
    Bitmap get(File file, Stat stat, Rect constraint) throws IOException {

        File cache = cacheFile(file, stat, constraint);
        try (InputStream in = new BufferedInputStream(cache.input())) {
            in.read(); // read DUMMY_BYTE

            int version = in.read();
            if (version != VERSION) {
                return null;
            }

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
            File file,
            Stat stat,
            Rect constraint,
            Bitmap thumbnail) throws IOException {

        File cache = cacheFile(file, stat, constraint);
        cache.createFiles();
        try (OutputStream out = new BufferedOutputStream(cache.output())) {
            out.write(DUMMY_BYTE);
            out.write(VERSION);
            thumbnail.compress(WEBP, 100, out);
        }
        return null;
    }

    public void putAsync(File res, Stat stat, Rect constraint, Bitmap thumbnail) {
        executor.execute(new WriteThumbnail(
                res, stat, constraint, new WeakReference<>(thumbnail)));
    }

    private final class WriteThumbnail implements Runnable {
        private final File res;
        private final Stat stat;
        private final Rect constraint;
        private final WeakReference<Bitmap> ref;

        private WriteThumbnail(
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
            Bitmap thumbnail = ref.get();
            if (thumbnail != null) {
                try {
                    put(res, stat, constraint, thumbnail);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
