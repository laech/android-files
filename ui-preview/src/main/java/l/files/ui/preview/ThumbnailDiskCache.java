package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

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
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.fs.Stream;
import l.files.fs.Visitor;

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
import static l.files.fs.Visitor.Result.CONTINUE;

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

    private static final int VERSION = 3;

    final File cacheDir;

    ThumbnailDiskCache(File cacheDir) {
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
        if (!cacheDir.exists(FOLLOW)) {
            return;
        }

        cacheDir.traverse(NOFOLLOW, new Visitor.Base() {

            final long now = currentTimeMillis();

            @Override
            public Result onPostVisit(File file) throws IOException {

                try {
                    Stat stat = file.stat(NOFOLLOW);
                    if (stat.isDirectory()) {

                        try {
                            file.delete();
                        } catch (DirectoryNotEmpty ignore) {
                        }

                    } else {

                        long lastModifiedMillis = stat.lastModifiedTime().to(MILLISECONDS);
                        if (MILLISECONDS.toDays(now - lastModifiedMillis) > 30) {
                            file.delete();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return CONTINUE;
            }

        });
    }

    private File cacheDir(File file, Rect constraint) {
        return cacheDir.resolve(file.scheme()
                + "/" + file.path()
                + "_" + constraint.width()
                + "_" + constraint.height());
    }

    File cacheFile(File file, @Nullable Stat stat, Rect constraint) throws IOException {
        if (stat == null) {
            Closer closer = Closer.create();
            try {

                Stream<File> children = closer.register(cacheDir(file, constraint).list(NOFOLLOW));
                return children.iterator().next();

            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }
        assert stat != null;
        Instant time = stat.lastModifiedTime();
        return cacheDir(file, constraint).resolve(
                time.seconds() + "_" + time.nanos());
    }

    @Override
    Bitmap get(File file, @Nullable Stat stat, Rect constraint) throws IOException {
        File cache = cacheFile(file, stat, constraint);
        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(cache.newBufferedInputStream());
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
                cache.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
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
            File file,
            Stat stat,
            Rect constraint,
            Bitmap thumbnail) throws IOException {

        purgeOldCacheFiles(file, constraint);

        File cache = cacheFile(file, stat, constraint);
        File parent = cache.parent();
        parent.createDirs();

        File tmp = parent.resolve(cache.name() + "-" + nanoTime());
        Closer closer = Closer.create();
        try {

            OutputStream out = closer.register(tmp.newBufferedOutputStream());
            out.write(VERSION);
            thumbnail.compress(WEBP, 100, out);

        } catch (Exception e) {
            try {
                tmp.delete();
            } catch (Exception sup) {
                addSuppressed(e, sup);
            }
            throw e;
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

        tmp.moveTo(cache);

        return null;
    }

    private void purgeOldCacheFiles(File file, Rect constraint) throws IOException {
        Closer closer = Closer.create();
        try {

            Stream<File> oldFiles = closer.register(cacheDir(file, constraint).list(FOLLOW));
            for (File oldFile : oldFiles) {
                try {
                    oldFile.delete();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }

        } catch (FileNotFoundException ignored) {
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
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
            setThreadPriority(BACKGROUND_THREAD_PRIORITY);

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
