package l.files.ui.preview;

import android.graphics.Bitmap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.base.io.Closer;
import l.files.fs.DirectoryNotEmpty;
import l.files.fs.FileSystem.Consumer;
import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.TraversalCallback;

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
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;

final class ThumbnailDiskCache extends Cache<Bitmap> {

    private static final Executor executor =
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

    private static final int VERSION = 4;

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
                    e.printStackTrace();
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
                    e.printStackTrace();
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
    public Bitmap get(Path path, Stat stat, Rect constraint, boolean matchTime) throws IOException {
        Path cache = cacheFile(path, stat, constraint, matchTime);
        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(Files.newBufferedInputStream(cache));
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
                Files.setLastModifiedTime(cache, NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
            } catch (IOException ignore) {
            }

            return bitmap;

        } catch (FileNotFoundException e) {
            return null;
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Override
    Snapshot<Bitmap> put(
            Path path,
            Stat stat,
            Rect constraint,
            Bitmap thumbnail) throws IOException {

        purgeOldCacheFiles(path, constraint);

        Path cache = cacheFile(path, stat, constraint, true);
        Path parent = cache.parent();
        Files.createDirs(parent);

        Path tmp = parent.resolve(cache.name() + "-" + nanoTime());
        Closer closer = Closer.create();
        try {

            OutputStream out = closer.register(Files.newBufferedOutputStream(tmp));
            out.write(VERSION);
            thumbnail.compress(WEBP, 100, out);

        } catch (Exception e) {
            try {
                Files.delete(tmp);
            } catch (Exception sup) {
                addSuppressed(e, sup);
            }
            throw e;
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
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
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                    return true;
                }
            });

        } catch (FileNotFoundException ignored) {
        }
    }

    public void putAsync(Path path, Stat stat, Rect constraint, Bitmap thumbnail) {
        executor.execute(new WriteThumbnail(
                path, stat, constraint, new WeakReference<>(thumbnail)));
    }

    private final class WriteThumbnail implements Runnable {
        private final Path path;
        private final Stat stat;
        private final Rect constraint;
        private final WeakReference<Bitmap> ref;

        private WriteThumbnail(
                Path path,
                Stat stat,
                Rect constraint,
                WeakReference<Bitmap> ref) {
            this.path = requireNonNull(path);
            this.stat = requireNonNull(stat);
            this.constraint = requireNonNull(constraint);
            this.ref = requireNonNull(ref);
        }

        @Override
        public void run() {
            setThreadPriority(BACKGROUND_THREAD_PRIORITY);

            Bitmap thumbnail = ref.get();
            if (thumbnail != null) {
                try {
                    put(path, stat, constraint, thumbnail);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
