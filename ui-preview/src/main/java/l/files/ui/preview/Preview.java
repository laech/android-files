package l.files.ui.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.lang.Boolean.TRUE;
import static l.files.base.Objects.requireNonNull;

public final class Preview {

    @Nullable
    private static Preview instance;

    public static Preview get(Context context) {
        synchronized (Preview.class) {
            if (instance == null) {
                java.io.File dir = context.getExternalCacheDir();
                if (dir == null) {
                    dir = context.getCacheDir();
                }
                Path cacheDir = Path.create(dir);
                instance = new Preview(context.getApplicationContext(), cacheDir);
                instance.cleanupAsync();
            }
        }
        return instance;
    }

    private final PersistenceCache<Rect> sizeCache;
    private final PersistenceCache<String> mediaTypeCache;
    private final PersistenceCache<Boolean> noPreviewCache;
    private final ThumbnailMemCache blurredThumbnailMemCache;
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
        this.mediaTypeCache = new MediaTypeCache(cacheDir);
        this.noPreviewCache = new NoPreviewCache(cacheDir);
        // TODO refactor so that size here and size in decoders are in same place
        this.blurredThumbnailMemCache = new ThumbnailMemCache(context, false, 0.05f);
        this.thumbnailMemCache = new ThumbnailMemCache(context, true, 0.20f);
        this.thumbnailDiskCache = new ThumbnailDiskCache(cacheDir);
    }

    public void writeCacheAsyncIfNeeded() {
        sizeCache.writeAsyncIfNeeded();
        mediaTypeCache.writeAsyncIfNeeded();
        noPreviewCache.writeAsyncIfNeeded();
    }

    public void readCacheAsyncIfNeeded() {
        sizeCache.readAsyncIfNeeded();
        mediaTypeCache.readAsyncIfNeeded();
        noPreviewCache.readAsyncIfNeeded();
    }

    private void cleanupAsync() {
        thumbnailDiskCache.cleanupAsync();
    }

    @Nullable
    public Bitmap getBlurredThumbnail(Path path, Stat stat, Rect constraint, boolean matchTime) {
        return blurredThumbnailMemCache.get(path, stat, constraint, matchTime);
    }

    void putBlurredThumbnail(Path path, Stat stat, Rect constraint, Bitmap thumbnail) {
        blurredThumbnailMemCache.put(path, stat, constraint, thumbnail);
    }

    @Nullable
    public Bitmap getThumbnail(Path path, Stat stat, Rect constraint, boolean matchTime) {
        return thumbnailMemCache.get(path, stat, constraint, matchTime);
    }

    void putThumbnail(Path path, Stat stat, Rect constraint, Bitmap thumbnail) {
        thumbnailMemCache.put(path, stat, constraint, thumbnail);
    }

    @Nullable
    ScaledBitmap getThumbnailFromDisk(
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
            ScaledBitmap thumbnail) {
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
    String getMediaType(Path path, Stat stat, Rect constraint, boolean matchTime) {
        return mediaTypeCache.get(path, stat, constraint, matchTime);
    }

    void putMediaType(Path path, Stat stat, Rect constraint, String media) {
        mediaTypeCache.put(path, stat, constraint, media);
    }

    public NoPreview getNoPreviewReason(Path path, Stat stat, Rect constraint) {

        if (!stat.isRegularFile()) {
            return NoPreview.NOT_REGULAR_FILE;
        }

        try {
            if (!path.isReadable()) {
                return NoPreview.FILE_UNREADABLE;
            }
        } catch (IOException e) {
            return new NoPreview(e);
        }

        if (TRUE.equals(noPreviewCache.get(path, stat, constraint, true))) {
            return NoPreview.IN_NO_PREVIEW_CACHE;
        }

        return null;
    }

    public boolean isPreviewable(Path path, Stat stat, Rect constraint) {
        return getNoPreviewReason(path, stat, constraint) == null;
    }

    void putPreviewable(Path path, Stat stat, Rect constraint, boolean previewable) {
        if (previewable) {
            noPreviewCache.remove(path, stat, constraint);
        } else {
            noPreviewCache.put(path, stat, constraint, true);
        }
    }

    @Nullable
    public Decode get(
            Path path,
            Stat stat,
            Rect constraint,
            Callback callback) {

        if (!isPreviewable(path, stat, constraint)) {
            return null;
        }

        return new Decode(path, stat, constraint, callback, this)
                .executeOnPreferredExecutor();
    }

    public void clearThumbnailCache() {
        thumbnailMemCache.clear();
    }

    public void clearBlurredThumbnailCache() {
        blurredThumbnailMemCache.clear();
    }

    public interface Callback {

        void onPreviewAvailable(Path path, Stat stat, Bitmap thumbnail);

        void onBlurredThumbnailAvailable(Path path, Stat stat, Bitmap thumbnail);

        void onPreviewFailed(Path path, Stat stat, Object cause);

    }

}
