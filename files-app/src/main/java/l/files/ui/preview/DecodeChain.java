package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import l.files.common.graphics.Rect;
import l.files.fs.File;
import l.files.fs.Stat;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static l.files.ui.preview.DecodeApk.isApk;
import static l.files.ui.preview.DecodeAudio.isAudio;
import static l.files.ui.preview.DecodeImage.isImage;
import static l.files.ui.preview.DecodePdf.isPdf;
import static l.files.ui.preview.DecodeVideo.isVideo;
import static l.files.ui.preview.Preview.decodePalette;

final class DecodeChain extends Decode {

    // No need to set UncaughtExceptionHandler to terminate
    // on exception already set by Android
    private static final Executor executor = newFixedThreadPool(5);

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

        if (isImage(media)) {
            publishProgress(new DecodeImage(
                    file, stat, constraint, callback, context));

        } else if (isPdf(media)) {
            publishProgress(new DecodePdf(
                    file, stat, constraint, callback, context));

        } else if (isAudio(media)) {
            publishProgress(new DecodeAudio(
                    file, stat, constraint, callback, context));

        } else if (isVideo(media)) {
            publishProgress(new DecodeVideo(
                    file, stat, constraint, callback, context));

        } else if (isApk(media)) {
            publishProgress(new DecodeApk(
                    file, stat, constraint, callback, context));

        } else {
            publishProgress(NoPreview.INSTANCE);
        }

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
            return file.detectMediaType(stat);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
