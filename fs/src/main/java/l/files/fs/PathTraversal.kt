package l.files.fs

import l.files.fs.LinkOption.NOFOLLOW
import java.io.IOException
import java.util.stream.Stream

enum class TraverseOrder {
  PRE,
  POST
}

/**
 * The exception indicates the path was a directory,
 * but this error prevented us from accessing its content.
 */
typealias TraverseEntry = Triple<Path, TraverseOrder, IOException?>

typealias TraverseTransform = (Path, Stream<PathEntry>) -> Stream<PathEntry>

private val id: TraverseTransform = { _, s -> s }

/**
 * Performs a depth first traverse of this tree, does not follow symbolic links.
 *
 * e.g. traversing the follow tree:
 *
 *      a
 *     / \
 *    b   c
 *
 * will generate:
 *
 *    TraverseEntry(a, PRE)
 *    TraverseEntry(b, PRE)
 *    TraverseEntry(b, POST)
 *    TraverseEntry(c, PRE)
 *    TraverseEntry(c, POST)
 *    TraverseEntry(a, POST)
 *
 * The returned stream needs to be closed after use.
 *
 * Error regarding the root (this) path will be reported the same way as others,
 * i.e. an entry with error in the stream.
 *
 * @param mapper performs custom transformation on sub streams before traversal
 */
fun Path.traverse(mapper: TraverseTransform = id): Stream<TraverseEntry> =
  try {
    doTraverse(stat(NOFOLLOW).type(), mapper)
  } catch (e: IOException) {
    Stream.of(
      TraverseEntry(this, TraverseOrder.PRE, e),
      TraverseEntry(this, TraverseOrder.POST, e)
    )
  }

fun Path.traverse(
  order: TraverseOrder,
  mapper: TraverseTransform = id
): Stream<Pair<Path, IOException?>> =
  traverse(mapper)
    .filter { it.second == order }
    .map { Pair(it.first, it.third) }

private fun Path.doTraverse(
  type: PathType,
  mapper: (Path, Stream<PathEntry>) -> Stream<PathEntry>
): Stream<TraverseEntry> {
  val (tree, error) = if (type == PathType.DIRECTORY) {
    try {

      Pair(
        mapper(this, list()).flatMap { (name, type) ->
          concat(name).doTraverse(type, mapper)
        },
        null as IOException?
      )

    } catch (e: IOException) {
      Pair(Stream.empty<TraverseEntry>(), e)
    }
  } else {
    Pair(Stream.empty(), null)
  }
  val pre = Stream.of(TraverseEntry(this, TraverseOrder.PRE, error))
  val post = Stream.of(TraverseEntry(this, TraverseOrder.POST, error))
  return Stream.concat(pre, Stream.concat(tree, post))
    .onClose { tree.close() }
}
