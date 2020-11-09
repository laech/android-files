package l.files.fs

import linux.Dirent
import linux.ErrnoException
import java.io.IOException
import java.nio.file.Files
import java.util.Arrays.copyOfRange
import java.util.Comparator.comparing
import java.util.Spliterator.DISTINCT
import java.util.Spliterators.spliteratorUnknownSize
import java.util.stream.Stream
import java.util.stream.StreamSupport.stream

enum class PathType {
  FILE,
  DIRECTORY,
  SYMLINK,
  OTHER,
}

typealias PathEntry = Pair<Path, PathType>

fun Stream<PathEntry>.sortedByName(): Stream<PathEntry> =
  sorted(comparing(Pair<Path, *>::first))

/**
 * Lists the content of this directory, follows symbolic link.
 * The returned stream needs to be closed after use.
 */
@Throws(IOException::class)
fun Path.list(): Stream<PathEntry> {
  val iterator = PathIterator(this)
  return stream(spliteratorUnknownSize(iterator, DISTINCT), false)
    .onClose(iterator::close)
}

@Throws(IOException::class)
fun Path.listPaths(): Stream<Path> = list().map(PathEntry::first).map(::concat)

private class PathIterator(
  private val path: Path
) : CloseableIterator<PathEntry>() {

  private var entry = Dirent()
  private val dir = path.opendir()

  override fun doClose() = path.closedir(dir)

  override fun computeNext() {
    while (path.readdir(dir, entry) != null) {
      if (entry.isSelfOrParent) {
        continue
      }
      val name = copyOfRange(entry.d_name, 0, entry.d_name_len)
      setNext(Pair(Path.of(name), entry.type()))
      return
    }
    done()
    close()
  }
}

private abstract class CloseableIterator<T>
  : AbstractIterator<T>(), AutoCloseable {

  private var closed = false

  final override fun close() {
    if (closed) {
      return
    }
    closed = true
    doClose()
  }

  abstract fun doClose()
}

private fun Path.opendir() = try {
  Dirent.opendir(toByteArray())
} catch (e: ErrnoException) {
  throw e.toIOException(this)
}

private fun Path.closedir(dir: Dirent.DIR) = try {
  Dirent.closedir(dir)
} catch (e: ErrnoException) {
  throw e.toIOException(this)
}

private fun Path.readdir(dir: Dirent.DIR, entry: Dirent) = try {
  Dirent.readdir(dir, entry)
} catch (e: ErrnoException) {
  throw e.toIOException(this)
}
