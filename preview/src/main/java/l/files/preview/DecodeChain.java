package l.files.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import l.files.fs.File;
import l.files.fs.Stat;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.preview.Preview.decodePalette;

final class DecodeChain extends Decode {

    // No need to set UncaughtExceptionHandler to terminate
    // on exception already set by Android
    private static final Executor executor = newFixedThreadPool(5);

    private static final Previewer[] PREVIEWERS = {
            DecodeImage.PREVIEWER,
            DecodePdf.PREVIEWER,
            DecodeApk.PREVIEWER,
            DecodeAudio.PREVIEWER,
            DecodeVideo.PREVIEWER,
            DecodeText.PREVIEWER
    };

    DecodeChain(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);
    }

    @Override
    DecodeChain executeOnPreferredExecutor() {
        return (DecodeChain) executeOnExecutor(executor);
    }

    @Nullable
    static Decode run(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {

        if (!context.isPreviewable(res, stat, constraint)) {
            return null;
        }

        Bitmap cached = context.getThumbnail(res, stat, constraint);
        if (cached != null) {
            callback.onPreviewAvailable(res, cached);
            return null;
        }

        Rect size = context.getSize(res, stat, constraint);
        if (size != null) {
            callback.onSizeAvailable(res, size);
        }

        return new DecodeChain(res, stat, constraint, callback, context)
                .executeOnPreferredExecutor();
    }

    @Override
    protected Object doInBackground(Object... params) {
        if (isCancelled()) {
            return null;
        }

        if (checkNotPreviewable()) {
            return null;
        }

        if (checkIsCache()) {
            return null;
        }

        if (checkThumbnailMemCache()) {
            return null;
        }

        Rect size = context.getSize(file, stat, constraint);
        if (size == null) {
      /*
       * Currently decoding the size is much quicker
       * than decoding anything else.
       */
            size = context.decodeSize(file);
            if (size != null) {
                publishProgress(size);
            }
        }

        if (checkThumbnailDiskCache()) {
            return null;
        }

        if (isCancelled()) {
            return null;
        }

        String media = checkMediaType();
        if (media == null) {
            return null;
        }

        if (isCancelled()) {
            return null;
        }

        for (Previewer previewer : PREVIEWERS) {
            if (previewer.accept(media)) {
                publishProgress(previewer.create(
                        file, stat, constraint, callback, context));
                return null;
            }
        }

        publishProgress(NoPreview.INSTANCE);

        return null;
    }

    private boolean checkIsCache() {
        if (file.hierarchy().contains(context.cacheDir)) {
            publishProgress(NoPreview.INSTANCE);
            return true;
        }
        return false;
    }

    private boolean checkNotPreviewable() {
        if (!context.isPreviewable(file, stat, constraint)) {
            publishProgress(NoPreview.INSTANCE);
            return true;
        }
        return false;
    }

    private boolean checkThumbnailMemCache() {
        Bitmap thumbnail = context.getThumbnail(file, stat, constraint);
        if (thumbnail != null) {
            publishProgress(thumbnail);
            return true;
        }
        return false;
    }

    private boolean checkThumbnailDiskCache() {
        Bitmap thumbnail = null;
        try {
            thumbnail = context.getThumbnailFromDisk(file, stat, constraint);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (thumbnail != null) {
            publishProgress(thumbnail);
            if (context.getPalette(file, stat, constraint) == null) {
                publishProgress(decodePalette(thumbnail));
            }
            return true;
        }
        return false;
    }

    private String checkMediaType() {
        String media = context.getMediaType(file, stat, constraint);
        if (media == null) {
            media = decodeMedia();
            if (media != null) {
                context.putMediaType(file, stat, constraint, media);
            }
        }
        if (media == null) {
            publishProgress(NoPreview.INSTANCE);
        }
        return media;
    }

    private String decodeMedia() {
        try {
            return file.detectContentMediaType(stat);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
