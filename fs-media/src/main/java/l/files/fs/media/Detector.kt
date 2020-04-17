package l.files.fs.media

import android.content.Context
import l.files.fs.LinkOption.FOLLOW
import l.files.fs.Path
import l.files.fs.Stat
import l.files.fs.newBufferedInputStream
import org.apache.tika.io.TaggedIOException
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaMetadataKeys
import org.apache.tika.mime.MimeTypeException
import org.apache.tika.mime.MimeTypes
import org.apache.tika.mime.MimeTypesFactory
import java.io.IOException

internal object Detector {

  // Media types for file types, kept consistent with the linux "file" command
  private const val INODE_DIRECTORY = "inode/directory"
  private const val INODE_BLOCKDEVICE = "inode/blockdevice"
  private const val INODE_CHARDEVICE = "inode/chardevice"
  private const val INODE_FIFO = "inode/fifo"
  private const val INODE_SOCKET = "inode/socket"

  @Volatile
  private var types: MimeTypes? = null

  @JvmOverloads
  @Throws(IOException::class)
  fun detect(
    context: Context,
    path: Path,
    stat: Stat = path.stat(FOLLOW)
  ): String = when {
    stat.isSymbolicLink -> detect(
      context,
      path.readSymbolicLink(),
      path.stat(FOLLOW)
    )
    stat.isRegularFile -> detectFile(context, path)
    stat.isFifo -> INODE_FIFO
    stat.isSocket -> INODE_SOCKET
    stat.isDirectory -> INODE_DIRECTORY
    stat.isBlockDevice -> INODE_BLOCKDEVICE
    stat.isCharacterDevice -> INODE_CHARDEVICE
    else -> MediaTypes.MEDIA_TYPE_OCTET_STREAM
  }

  private fun detectFile(context: Context, path: Path): String {
    if (types == null) {
      synchronized(this) {
        if (types == null) {
          types = try {
            createMimeTypes(context)
          } catch (e: MimeTypeException) {
            throw IOException(e)
          }
        }
      }
    }

    return try {
      detectFile(types!!, path)
    } catch (e: TaggedIOException) {
      throw e.cause ?: e
    }
  }

  private fun createMimeTypes(context: Context): MimeTypes =
    /*
     * tika_mimetypes_xml_1_12 is a v1.12 of org.apache.tika.mime/tika-mimetypes.xml,
     * this is to work around the slowness of Android's Class.getResource*()
     * and to avoid the unnecessary memory usage increase because of the caching
     * used for the jar content created by Android's Class.getResource*().
     */
    context.resources
      .openRawResource(R.raw.tika_mimetypes_1_10)
      .use { MimeTypesFactory.create(it) }

  private fun detectFile(types: MimeTypes, path: Path): String =
    path.newBufferedInputStream().use {
      val meta = Metadata()
      meta.add(TikaMetadataKeys.RESOURCE_NAME_KEY, path.toString())
      types.detect(it, meta).baseType.toString()
    }
}
