package l.files.ui.preview;

import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;
import l.files.ui.preview.Preview.Using;

import static java.util.Locale.ENGLISH;
import static l.files.ui.preview.Preview.Using.MEDIA_TYPE;

final class DecodeChain extends Decode {

    // Need to update NoPreview cache version to invalidate
    // cache when we add a new decoder so existing files
    // marked as not previewable will get re-evaluated.
    private static final Previewer[] PREVIEWERS = {
            DecodeSvg.PREVIEWER,
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
                            MEDIA_TYPE,
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
                MEDIA_TYPE,
                context
        ).executeOnPreferredExecutor();
    }

    @Override
    Object onDoInBackground() {

        try {
            String media = checkMediaType();
            context.putMediaType(path, stat, constraint, media);
            for (Previewer previewer : PREVIEWERS) {
                if (previewer.acceptsMediaType(path, media)) {
                    publishProgress(previewer.create(
                            path, stat, constraint, callback, MEDIA_TYPE, context));
                    break;
                }
            }
        } catch (IOException e) {
            publishProgress(new NoPreview(e));
        }

        return null;
    }

    private String checkMediaType() throws IOException {
        String media = context.getMediaType(path, stat, constraint, true);
        if (media == null) {
            media = decodeMedia();
        }
        return media;
    }

    private String decodeMedia() throws IOException {
        return MediaTypes.detectByContent(context.context, path, stat);
    }
}
