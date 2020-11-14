package l.files.fs.media

import android.content.Context
import org.apache.tika.io.TaggedIOException
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaMetadataKeys.RESOURCE_NAME_KEY
import org.apache.tika.mime.MimeTypeException
import org.apache.tika.mime.MimeTypes
import org.apache.tika.mime.MimeTypesFactory
import java.io.IOException
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

internal object Detector {

  // Media types for file types, kept consistent with the linux "file" command
  private const val INODE_DIRECTORY = "inode/directory"

  @Volatile
  private var types: MimeTypes? = null

  fun detect(context: Context, path: Path): String {
    val attrs = readAttributes(path, BasicFileAttributes::class.java)
    return when {
      attrs.isSymbolicLink -> detect(context, readSymbolicLink(path))
      attrs.isRegularFile -> detectFile(context, path)
      attrs.isDirectory -> INODE_DIRECTORY
      else -> MediaTypes.MEDIA_TYPE_OCTET_STREAM
    }
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
    newInputStream(path).buffered().use {
      val meta = Metadata()
      meta.add(RESOURCE_NAME_KEY, path.toString())
      types.detect(it, meta).baseType.toString()
    }
}
