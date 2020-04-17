package l.files.thumbnail

import android.content.Context
import android.media.MediaMetadataRetriever
import l.files.fs.Path
import l.files.ui.base.graphics.Rect
import l.files.ui.base.media.MediaMetadataRetrievers

internal object MediaThumbnailer : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    type.startsWith("audio/") ||
      type.startsWith("video/")

  override fun create(input: Path, max: Rect, context: Context) =
    MediaMetadataRetriever().let { retriever ->
      input.newInputStream().use {
        try {
          retriever.setDataSource(it.fd)
          MediaMetadataRetrievers.getAnyThumbnail(retriever, max)
        } finally {
          retriever.release()
        }
      }
    }
}
