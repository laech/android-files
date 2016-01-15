package l.files.ui.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import l.files.base.io.Closer;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeStream;
import static android.graphics.Color.TRANSPARENT;
import static java.lang.Boolean.TRUE;
import static l.files.base.Objects.requireNonNull;

public final class Preview {

    /**
     * Increasing this also increases the chance that the palette would
     * contain a color. e.g. {@link Palette#getDarkVibrantColor(int)}
     * would return a color.
     * <p/>
     * TODO make this part of the cache key
     */
    static final int PALETTE_MAX_COLOR_COUNT = 1024;

    private static Preview instance;

    public static Preview get(Context context) {
        synchronized (Preview.class) {
            if (instance == null) {
                java.io.File dir = context.getExternalCacheDir();
                if (dir == null) {
                    dir = context.getCacheDir();
                }
                Path cacheDir = Paths.get(dir);
                instance = new Preview(context.getApplicationContext(), cacheDir);
                instance.cleanupAsync();
            }
        }
        return instance;
    }

    private final PersistenceCache<Rect> sizeCache;
    private final PersistenceCache<Integer> paletteCache;
    private final PersistenceCache<String> mediaTypeCache;
    private final PersistenceCache<Boolean> noPreviewCache;
    private final ThumbnailMemCache thumbnailMemCache;
    private final ThumbnailDiskCache thumbnailDiskCache;

    final DisplayMetrics displayMetrics;
    final Path cacheDir;
    final Context context;

    Preview(Context context, Path cacheDir) {
        this.context = requireNonNull(context);
        this.cacheDir = requireNonNull(cacheDir);
        this.displayMetrics = requireNonNull(context).getResources().getDisplayMetrics();
        this.sizeCache = new RectCache(cacheDir);
        this.paletteCache = new PaletteCache(cacheDir);
        this.mediaTypeCache = new MediaTypeCache(cacheDir);
        this.noPreviewCache = new NoPreviewCache(cacheDir);
        this.thumbnailMemCache = new ThumbnailMemCache(context, 0.3f);
        this.thumbnailDiskCache = new ThumbnailDiskCache(cacheDir);
    }

    public void writeCacheAsyncIfNeeded() {
        sizeCache.writeAsyncIfNeeded();
        paletteCache.writeAsyncIfNeeded();
        mediaTypeCache.writeAsyncIfNeeded();
        noPreviewCache.writeAsyncIfNeeded();
    }

    public void readCacheAsyncIfNeeded() {
        sizeCache.readAsyncIfNeeded();
        paletteCache.readAsyncIfNeeded();
        mediaTypeCache.readAsyncIfNeeded();
        noPreviewCache.readAsyncIfNeeded();
    }

    private void cleanupAsync() {
        thumbnailDiskCache.cleanupAsync();
    }

    @Nullable
    public Bitmap getThumbnail(Path path, Stat stat, Rect constraint, boolean matchTime) {
        return thumbnailMemCache.get(path, stat, constraint, matchTime);
    }

    void putThumbnail(Path path, Stat stat, Rect constraint, Bitmap thumbnail) {
        thumbnailMemCache.put(path, stat, constraint, thumbnail);
    }

    @Nullable
    Bitmap getThumbnailFromDisk(
            Path path,
            Stat stat,
            Rect constraint,
            boolean matchTime) throws IOException {
        return thumbnailDiskCache.get(path, stat, constraint, matchTime);
    }

    Future<?> putThumbnailToDiskAsync(
            Path path,
            Stat stat,
            Rect constraint,
            Bitmap thumbnail) {
        return thumbnailDiskCache.putAsync(path, stat, constraint, thumbnail);
    }

    @Nullable
    public Rect getSize(Path path, Stat stat, Rect constraint, boolean matchTime) {
        return sizeCache.get(path, stat, constraint, matchTime);
    }

    void putSize(Path path, Stat stat, Rect constraint, Rect size) {
        sizeCache.put(path, stat, constraint, size);
    }

    @Nullable
    public Integer getPaletteColor(Path path, Stat stat, Rect constraint, boolean matchTime) {
        return paletteCache.get(path, stat, constraint, matchTime);
    }

    void putPaletteColor(Path path, Stat stat, Rect constraint, int color) {
        paletteCache.put(path, stat, constraint, color);
    }

    @Nullable
    String getMediaType(Path path, Stat stat, Rect constraint, boolean matchTime) {
        return mediaTypeCache.get(path, stat, constraint, matchTime);
    }

    void putMediaType(Path path, Stat stat, Rect constraint, String media) {
        mediaTypeCache.put(path, stat, constraint, media);
    }

    public boolean isPreviewable(Path path, Stat stat, Rect constraint) {
        return stat.isRegularFile()
                && isReadable(path)
                && !TRUE.equals(noPreviewCache.get(path, stat, constraint, true));
    }

    void putPreviewable(Path path, Stat stat, Rect constraint, boolean previewable) {
        if (previewable) {
            noPreviewCache.remove(path, stat, constraint);
        } else {
            noPreviewCache.put(path, stat, constraint, true);
        }
    }

    private static boolean isReadable(Path path) {
        try {
            return Files.isReadable(path);
        } catch (IOException e) {
            return false;
        }
    }

    @Nullable
    public Decode get(
            Path path,
            Stat stat,
            Rect constraint,
            Callback callback,
            Using using) {

        return DecodeChain.run(
                path,
                stat,
                constraint,
                callback,
                using,
                this);
    }

    Rect decodeSize(Path path) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Closer closer = Closer.create();
        try {

            InputStream in = closer.register(Files.newBufferedInputStream(path));
            decodeStream(in, null, options);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

        if (options.outWidth > 0 && options.outHeight > 0) {
            return Rect.of(options.outWidth, options.outHeight);
        }
        return null;
    }

    @Nullable
    static Integer decodePaletteColor(Bitmap bitmap) {
        Palette palette = new Palette.Builder(bitmap)
                .maximumColorCount(PALETTE_MAX_COLOR_COUNT)
                .generate();

        int color = palette.getDarkVibrantColor(TRANSPARENT);
        if (color == TRANSPARENT) {
            color = palette.getDarkMutedColor(TRANSPARENT);
        }
        if (color == TRANSPARENT) {
            return null;
        }
        return color;
    }

    public void clearBitmapMemCache() {
        thumbnailMemCache.clear();
    }

    public enum Using {
        FILE_EXTENSION,
        MEDIA_TYPE
    }

    public interface Callback {

        void onSizeAvailable(Path path, Stat stat, Rect size);

        void onPaletteColorAvailable(Path path, Stat stat, int color);

        void onPreviewAvailable(Path path, Stat stat, Bitmap thumbnail);

        void onPreviewFailed(Path path, Stat stat, Using used);

    }

}
