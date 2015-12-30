package l.files.fs.media;

import java.io.IOException;

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
     * without reading the content of this file.
     * Returns {@link #MEDIA_TYPE_OCTET_STREAM} if unknown.
     */
    public static String detectByProperties(Path path, Stat stat)
            throws IOException {
        return BasicDetector.INSTANCE.detect(path, stat);
    }

    /**
     * Detects the content type of this file based on its content.
     * Returns {@link #MEDIA_TYPE_OCTET_STREAM} if unknown.
     */
    public static String detectByContent(Path path, Stat stat)
            throws IOException {
        return MagicDetector.INSTANCE.detect(path, stat);
    }

    /**
     * Detects the content type of this file based on its properties
     * and its content.
     * Returns {@link #MEDIA_TYPE_OCTET_STREAM} if unknown.
     */
    public static String detect(Path path, Stat stat)
            throws IOException {
        return MetaMagicDetector.INSTANCE.detect(path, stat);
    }

}
