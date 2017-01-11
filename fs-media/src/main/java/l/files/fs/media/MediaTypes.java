package l.files.fs.media;

import android.content.Context;

import java.io.IOException;

import l.files.fs.FileSystem;
import l.files.fs.Path;
import l.files.fs.Stat;

public final class MediaTypes {

    public static final String MEDIA_TYPE_OCTET_STREAM = "application/octet-stream";
    public static final String MEDIA_TYPE_ANY = "*/*";

    private MediaTypes() {
    }

    public static String generalize(String mediaType) {
        if (mediaType.startsWith("text/")) return "text/*";
        if (mediaType.startsWith("image/")) return "image/*";
        if (mediaType.startsWith("audio/")) return "audio/*";
        if (mediaType.startsWith("video/")) return "video/*";
        if (mediaType.startsWith("application/")) {
            if (mediaType.contains("json") ||
                    mediaType.contains("xml") ||
                    mediaType.contains("javascript") ||
                    mediaType.endsWith("/x-sh")) {
                return "text/*";
            }
        }
        return mediaType;
    }

    /**
     * Detects the content type of this file based on its properties
     * and its content.
     * Returns {@link #MEDIA_TYPE_OCTET_STREAM} if unknown.
     */
    public static String detect(
            Context context,
            FileSystem fs,
            Path path,
            Stat stat) throws IOException {
        return Detector.INSTANCE.detect(context, fs, path, stat).intern();
    }

}
