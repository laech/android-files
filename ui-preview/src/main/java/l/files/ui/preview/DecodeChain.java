package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;
import l.files.ui.preview.Preview.Using;

import static java.util.Locale.ENGLISH;

final class DecodeChain extends Decode {

    private static final Previewer[] PREVIEWERS = {
            DecodeImage.PREVIEWER,
            DecodePdf.PREVIEWER,
            DecodeApk.PREVIEWER,
            DecodeAudioVideo.PREVIEWER,
            DecodeText.PREVIEWER
    };

    DecodeChain(
            Path file,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Using using,
            Preview context) {
        super(file, stat, constraint, callback, using, context);
    }

    @Nullable
    static Decode run(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
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

        if (using == Using.FILE_EXTENSION) {
            String extensionInLowercase = path.name().ext().toLowerCase(ENGLISH);
            for (Previewer previewer : PREVIEWERS) {
                if (previewer.acceptsFileExtension(path, extensionInLowercase)) {
                    return previewer.create(
                            path,
                            stat,
                            constraint,
                            callback,
                            using,
                            context).executeOnPreferredExecutor();
                }
            }
        }

        String media = context.getMediaType(path, stat, constraint, true);
        if (media != null) {
            for (Previewer previewer : PREVIEWERS) {
                if (previewer.acceptsMediaType(path, media)) {
                    return previewer.create(
                            path,
                            stat,
                            constraint,
                            callback,
                            Using.MEDIA_TYPE,
                            context).executeOnPreferredExecutor();
                }
            }
            return null;
        }

        return new DecodeChain(
                path,
                stat,
                constraint,
                callback,
                Using.MEDIA_TYPE,
                context
        ).executeOnPreferredExecutor();
    }

    @Override
    Object onDoInBackground() {

        String media = checkMediaType();
        if (media != null) {
            for (Previewer previewer : PREVIEWERS) {
                if (previewer.acceptsMediaType(path, media)) {
                    publishProgress(previewer.create(
                            path, stat, constraint, callback, Using.MEDIA_TYPE, context));
                    return null;
                }
            }
        }

        publishProgress(NoPreview.INSTANCE);

        return null;
    }

    private String checkMediaType() {
        String media = context.getMediaType(path, stat, constraint, true);
        if (media == null) {
            media = decodeMedia();
            if (media != null) {
                context.putMediaType(path, stat, constraint, media);
            }
        }
        if (media == null) {
            publishProgress(NoPreview.INSTANCE);
        }
        return media;
    }

    private String decodeMedia() {
        try {
            return MediaTypes.detectByContent(path, stat);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
