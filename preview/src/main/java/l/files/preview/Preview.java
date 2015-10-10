package l.files.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.fs.local.LocalFile;

import static android.graphics.BitmapFactory.decodeStream;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNull;

public final class Preview {

    static final int PALETTE_MAX_COLOR_COUNT = 24;

    private static Preview instance;

    public static Preview get(Context context) {
        synchronized (Preview.class) {
            if (instance == null) {
                File cacheDir = LocalFile.create(context.getExternalCacheDir());
                instance = new Preview(context.getApplicationContext(), cacheDir);
                instance.cleanupAsync();
            }
        }
        return instance;
    }

    private final PersistenceCache<Rect> sizeCache;
    private final PersistenceCache<Palette> paletteCache;
    private final PersistenceCache<String> mediaTypeCache;
    private final PersistenceCache<Boolean> noPreviewCache;
    private final ThumbnailMemCache thumbnailMemCache;
    private final ThumbnailDiskCache thumbnailDiskCache;

    final DisplayMetrics displayMetrics;
    final File cacheDir;
    final Context context;

    Preview(Context context, File cacheDir) {
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
    public Bitmap getThumbnail(File res, Stat stat, Rect constraint) {
        return thumbnailMemCache.get(res, stat, constraint);
    }

    void putThumbnail(File res, Stat stat, Rect constraint, Bitmap thumbnail) {
        thumbnailMemCache.put(res, stat, constraint, thumbnail);
    }

    @Nullable
    Bitmap getThumbnailFromDisk(File res, Stat stat, Rect constraint) throws IOException {
        return thumbnailDiskCache.get(res, stat, constraint);
    }

    void putThumbnailToDiskAsync(
            File res, Stat stat, Rect constraint, Bitmap thumbnail) {
        thumbnailDiskCache.putAsync(res, stat, constraint, thumbnail);
    }

    @Nullable
    public Rect getSize(File res, Stat stat, Rect constraint) {
        return sizeCache.get(res, stat, constraint);
    }

    void putSize(File res, Stat stat, Rect constraint, Rect size) {
        sizeCache.put(res, stat, constraint, size);
    }

    @Nullable
    public Palette getPalette(File res, Stat stat, Rect constraint) {
        return paletteCache.get(res, stat, constraint);
    }

    void putPalette(File res, Stat stat, Rect constraint, Palette palette) {
        paletteCache.put(res, stat, constraint, palette);
    }

    @Nullable
    String getMediaType(File res, Stat stat, Rect constraint) {
        return mediaTypeCache.get(res, stat, constraint);
    }

    void putMediaType(File res, Stat stat, Rect constraint, String media) {
        mediaTypeCache.put(res, stat, constraint, media);
    }

    public boolean isPreviewable(File res, Stat stat, Rect constraint) {
        return stat.size() > 0
                && stat.isRegularFile()
                && isReadable(res)
                && !TRUE.equals(noPreviewCache.get(res, stat, constraint));
    }

    void putPreviewable(File res, Stat stat, Rect constraint, boolean previewable) {
        if (previewable) {
            noPreviewCache.remove(res, stat, constraint);
        } else {
            noPreviewCache.put(res, stat, constraint, true);
        }
    }

    private static boolean isReadable(File file) {
        try {
            return file.isReadable();
        } catch (IOException e) {
            return false;
        }
    }

    @Nullable
    public Decode set(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback) {
        return DecodeChain.run(res, stat, constraint, callback, this);
    }

    Rect decodeSize(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try (InputStream in = file.newBufferedInputStream()) {

            decodeStream(in, null, options);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (options.outWidth > 0 && options.outHeight > 0) {
            return Rect.of(options.outWidth, options.outHeight);
        }
        return null;
    }

    static Palette decodePalette(Bitmap bitmap) {
        return new Palette.Builder(bitmap)
                .maximumColorCount(PALETTE_MAX_COLOR_COUNT)
                .generate();
    }

    public void clearBitmapMemCache() {
        thumbnailMemCache.clear();
    }

}
