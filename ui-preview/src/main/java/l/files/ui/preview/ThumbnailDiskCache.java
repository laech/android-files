package l.files.ui.preview;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.DirectoryNotEmpty;
import l.files.fs.FileSystem.Consumer;
import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.TraversalCallback;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.graphics.Bitmap.CompressFormat.WEBP;
import static android.graphics.BitmapFactory.decodeStream;
import static android.os.Process.THREAD_PRIORITY_LOWEST;
import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;
import static android.os.Process.setThreadPriority;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.base.Throwables.addSuppressed;
import static l.files.fs.Files.newBufferedDataInputStream;
import static l.files.fs.Files.newBufferedDataOutputStream;
import static l.files.fs.Files.setLastModifiedTime;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;

final class ThumbnailDiskCache extends Cache<ScaledBitmap> {

    private static final ExecutorService executor =
            newFixedThreadPool(2, new ThreadFactory() {

                private final AtomicInteger count =
                        new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    String prefix = "ThumbnailDiskCache #";
                    int num = count.getAndIncrement();
                    return new Thread(r, prefix + num);
                }

            });

    private static final int BACKGROUND_THREAD_PRIORITY =
            THREAD_PRIORITY_LOWEST + THREAD_PRIORITY_MORE_FAVORABLE;

    private static final byte VERSION = 5;

    final Path cacheDir;

    ThumbnailDiskCache(Path cacheDir) {
        this.cacheDir = cacheDir.resolve("thumbnails");
    }

    public void cleanupAsync() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                setThreadPriority(BACKGROUND_THREAD_PRIORITY);
                try {
                    cleanup();
                } catch (IOException e) {
                    Log.w(getClass().getSimpleName(),
                            "Failed to cleanup.", e);
                }
            }
        });
    }

    void cleanup() throws IOException {
        if (!Files.exists(cacheDir, FOLLOW)) {
            return;
        }

        Files.traverse(cacheDir, NOFOLLOW, new TraversalCallback.Base<Path>() {

            final long now = currentTimeMillis();

            @Override
            public Result onPostVisit(Path path) throws IOException {

                try {
                    Stat stat = Files.stat(path, NOFOLLOW);
                    if (stat.isDirectory()) {

                        try {
                            Files.delete(path);
                        } catch (DirectoryNotEmpty ignore) {
                        }

                    } else {

                        long lastModifiedMillis = stat.lastModifiedTime().to(MILLISECONDS);
                        if (MILLISECONDS.toDays(now - lastModifiedMillis) > 30) {
                            Files.delete(path);
                        }

                    }
                } catch (IOException e) {
                    Log.w(getClass().getSimpleName(),
                            "Failed to delete " + path, e);
                }

                return CONTINUE;
            }

        });
    }

    private Path cacheDir(Path path, Rect constraint) {
        return cacheDir.resolve(path
                + "_" + constraint.width()
                + "_" + constraint.height());
    }

    Path cacheFile(Path path, Stat stat, Rect constraint, boolean matchTime) throws IOException {
        if (!matchTime) {
            final Path[] result = {null};
            Files.list(cacheDir(path, constraint), NOFOLLOW, new Consumer<Path>() {
                @Override
                public boolean accept(Path path) {
                    result[0] = path;
                    return false;
                }
            });
            return result[0];
        }
        Instant time = stat.lastModifiedTime();
        return cacheDir(path, constraint).resolve(
                time.seconds() + "-" + time.nanos());
    }


    @Override
    public ScaledBitmap get(Path path, Stat stat, Rect constraint, boolean matchTime) throws IOException {
        Path cache = cacheFile(path, stat, constraint, matchTime);
        DataInputStream in = null;
        try {

            in = newBufferedDataInputStream(cache);
            byte version = in.readByte();
            if (version != VERSION) {
                return null;
            }

            int originalWidth = in.readInt();
            int originalHeight = in.readInt();
            Rect originalSize = Rect.of(originalWidth, originalHeight);

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
                setLastModifiedTime(cache, NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
            } catch (IOException ignore) {
            }

            return new ScaledBitmap(bitmap, originalSize);

        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    @Override
    Snapshot<ScaledBitmap> put(
            Path path,
            Stat stat,
            Rect constraint,
            ScaledBitmap thumbnail) throws IOException {

        purgeOldCacheFiles(path, constraint);

        Path cache = cacheFile(path, stat, constraint, true);
        Path parent = cache.parent();
        assert parent != null;
        Files.createDirs(parent);

        Path tmp = parent.resolve(cache.name() + "-" + nanoTime());
        DataOutputStream out = newBufferedDataOutputStream(tmp);
        try {

            out.writeByte(VERSION);
            out.writeInt(thumbnail.originalSize().width());
            out.writeInt(thumbnail.originalSize().height());
            thumbnail.bitmap().compress(WEBP, 100, out);

        } catch (Exception e) {
            try {
                Files.delete(tmp);
            } catch (Exception sup) {
                addSuppressed(e, sup);
            }
            throw e;
        } finally {
            out.close();
        }

        Files.move(tmp, cache);

        return null;
    }

    private void purgeOldCacheFiles(Path path, Rect constraint) throws IOException {
        try {

            Files.list(cacheDir(path, constraint), FOLLOW, new Consumer<Path>() {
                @Override
                public boolean accept(Path path) {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        Log.w(getClass().getSimpleName(),
                                "Failed to purge " + path, e);
                    }
                    return true;
                }
            });

        } catch (FileNotFoundException ignored) {
        }
    }

    public Future<?> putAsync(Path path, Stat stat, Rect constraint, ScaledBitmap thumbnail) {
        return executor.submit(new WriteThumbnail(
                path, stat, constraint, new WeakReference<>(thumbnail)));
    }

    private final class WriteThumbnail implements Runnable {
        private final Path path;
        private final Stat stat;
        private final Rect constraint;
        private final WeakReference<ScaledBitmap> ref;

        private WriteThumbnail(
                Path path,
                Stat stat,
                Rect constraint,
                WeakReference<ScaledBitmap> ref) {
            this.path = requireNonNull(path);
            this.stat = requireNonNull(stat);
            this.constraint = requireNonNull(constraint);
            this.ref = requireNonNull(ref);
        }

        @Override
        public void run() {
            setThreadPriority(BACKGROUND_THREAD_PRIORITY);

            ScaledBitmap thumbnail = ref.get();
            if (thumbnail != null) {
                try {
                    put(path, stat, constraint, thumbnail);
                } catch (IOException e) {
                    Log.w(getClass().getSimpleName(),
                            "Failed to put " + path, e);
                }
            }
        }
    }

}
