package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.ui.preview.Preview.decodePalette;

final class DecodeChain extends Decode {

    private static final Previewer[] PREVIEWERS = {
            DecodeImage.PREVIEWER,
            DecodePdf.PREVIEWER,
            DecodeApk.PREVIEWER,
            DecodeAudio.PREVIEWER,
            DecodeVideo.PREVIEWER,
            DecodeText.PREVIEWER
    };

    DecodeChain(
            Path file,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(file, stat, constraint, callback, context);
    }

    @Nullable
    static Decode run(
            Path path,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {

        if (!context.isPreviewable(path, stat, constraint)) {
            return null;
        }

        Bitmap cached = context.getThumbnail(path, stat, constraint, true);
        if (cached != null) {
            callback.onPreviewAvailable(path, stat, cached);
            return null;
        }

        Rect size = context.getSize(path, stat, constraint, true);
        if (size != null) {
            callback.onSizeAvailable(path, stat, size);
        }

        return (Decode) new DecodeChain(path, stat, constraint, callback, context)
                .executeOnPreferredExecutor();
    }

    @Override
    Object onDoInBackground() {
        if (isCancelled()) {
            return null;
        }

        if (!checkPreviewable()) {
            return null;
        }

        if (checkIsCache()) {
            return null;
        }

        if (checkThumbnailMemCache()) {
            return null;
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
            if (previewer.accept(file, media)) {
                publishProgress(previewer.create(
                        file, stat, constraint, callback, context));
                return null;
            }
        }

        publishProgress(NoPreview.INSTANCE);

        return null;
    }

    private boolean checkIsCache() {
        if (Files.hierarchy(file).contains(context.cacheDir)) {
            publishProgress(NoPreview.INSTANCE);
            return true;
        }
        return false;
    }

    private boolean checkPreviewable() {
        if (context.isPreviewable(file, stat, constraint)) {
            return true;
        }
        publishProgress(NoPreview.INSTANCE);
        return false;
    }

    private boolean checkThumbnailMemCache() {
        Bitmap thumbnail = context.getThumbnail(file, stat, constraint, true);
        if (thumbnail != null) {
            publishProgress(thumbnail);
            return true;
        }
        return false;
    }

    private boolean checkThumbnailDiskCache() {
        Bitmap thumbnail = null;
        try {
            thumbnail = context.getThumbnailFromDisk(file, stat, constraint, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (thumbnail != null) {

            if (context.getSize(file, stat, constraint, true) == null) {
                context.putSize(file, stat, constraint, Rect.of(
                        thumbnail.getWidth(),
                        thumbnail.getHeight()));
            }

            if (context.getPalette(file, stat, constraint, true) == null) {
                publishProgress(decodePalette(thumbnail));
            }

            publishProgress(thumbnail);

            return true;
        }
        return false;
    }

    private String checkMediaType() {
        String media = context.getMediaType(file, stat, constraint, true);
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
            return Files.detectContentMediaType(file, stat);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
