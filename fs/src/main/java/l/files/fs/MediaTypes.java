package l.files.fs;

public class MediaTypes {

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

}
