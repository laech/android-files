package l.files.fs.media

import android.content.Context
import l.files.fs.Path
import l.files.fs.Stat
import java.io.IOException

object MediaTypes {

  const val MEDIA_TYPE_OCTET_STREAM = "application/octet-stream"
  const val MEDIA_TYPE_ANY = "*/*"

  @JvmStatic
  fun generalize(mediaType: String): String = when {
    mediaType.startsWith("text/") -> "text/*"
    mediaType.startsWith("image/") -> "image/*"
    mediaType.startsWith("audio/") -> "audio/*"
    mediaType.startsWith("video/") -> "video/*"
    mediaType.startsWith("application/") &&
      ((mediaType.contains("json") ||
        mediaType.contains("xml") ||
        mediaType.contains("javascript") ||
        mediaType.endsWith("/x-sh"))) -> "text/*"
    else -> mediaType
  }

  /**
   * Detects the content type of this file based on its properties
   * and its content.
   * Returns [MEDIA_TYPE_OCTET_STREAM] if unknown.
   */
  @JvmStatic
  @Throws(IOException::class)
  fun detect(context: Context, path: Path, stat: Stat): String =
    Detector.detect(context, path, stat)
}
