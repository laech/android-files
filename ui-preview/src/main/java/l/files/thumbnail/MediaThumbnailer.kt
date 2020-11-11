package l.files.thumbnail

import android.content.Context
import android.media.MediaMetadataRetriever
import l.files.ui.base.graphics.Rect
import l.files.ui.base.media.MediaMetadataRetrievers
import java.nio.file.Path

internal object MediaThumbnailer : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    type.startsWith("audio/") ||
      type.startsWith("video/")

  override fun create(input: Path, max: Rect, context: Context) =
    MediaMetadataRetriever().let { retriever ->
      try {
        retriever.setDataSource(input.toString())
        MediaMetadataRetrievers.getAnyThumbnail(retriever, max)
      } finally {
        retriever.release()
      }
    }
}
