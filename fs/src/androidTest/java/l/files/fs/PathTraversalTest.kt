package l.files.fs

import l.files.fs.TraverseOrder.POST
import l.files.fs.TraverseOrder.PRE
import l.files.fs.exception.AccessDenied
import l.files.testing.fs.PathBaseTest
import l.files.testing.fs.Paths
import org.junit.Test
import java.util.Comparator.comparing
import java.util.stream.Collectors.toList

class PathTraversalTest : PathBaseTest() {

  @Test
  fun traverse_no_follow_link() {
    val dir = dir1().concat("dir").createDirectory()
    val link = dir1().concat("link").createSymbolicLink(dir)
    link.concat("a").createFile()
    link.concat("b").createFile()

    val expected = listOf(
      Triple(link, PRE, null),
      Triple(link, POST, null)
    )
    link.traverse().use {
      checkEqual(expected, it.collect(toList()))
    }
  }

  @Test
  fun traverse_continues_on_exception() {
    val a = dir1().concat("a").createDirectory()
    val b = dir1().concat("b").createDirectory()
    Paths.removePermissions(a, Permission.read())

    val expected = listOf(
      Triple(dir1(), PRE, null),
      Triple(a, PRE, AccessDenied::class.java),
      Triple(a, POST, AccessDenied::class.java),
      Triple(b, PRE, null),
      Triple(b, POST, null),
      Triple(dir1(), POST, null)
    )
    dir1().traverse().use {
      checkEqual(expected, it.collect(toList()))
    }
  }

  @Test
  fun traverse_order() {
    dir1().concat("a/1").createDirectories()
    dir1().concat("a/1/i").createFile()
    dir1().concat("a/2").createDirectories()
    dir1().concat("b").createDirectories()
    val expected = listOf(
      Triple(dir1(), PRE, null),
      Triple(dir1().concat("a"), PRE, null),
      Triple(dir1().concat("a/1"), PRE, null),
      Triple(dir1().concat("a/1/i"), PRE, null),
      Triple(dir1().concat("a/1/i"), POST, null),
      Triple(dir1().concat("a/1"), POST, null),
      Triple(dir1().concat("a/2"), PRE, null),
      Triple(dir1().concat("a/2"), POST, null),
      Triple(dir1().concat("a"), POST, null),
      Triple(dir1().concat("b"), PRE, null),
      Triple(dir1().concat("b"), POST, null),
      Triple(dir1(), POST, null)
    )
    dir1()
      .traverse { _, s -> s.sorted(comparing(PathEntry::first)) }
      .use {
        checkEqual(expected, it.collect(toList()))
      }
  }

  private fun <E> checkEqual(
    expected: List<Triple<Path, TraverseOrder, E>>,
    actual: List<TraverseEntry>
  ) {
    val mapped = actual.map { (path, order, error) ->
      Triple(
        path,
        order,
        error?.javaClass
      )
    }
    if (expected != mapped) {
      throw AssertionError(
        """
        expected:
        ${expected.joinToString<Any?>("\n")}
        actual:
        ${mapped.joinToString<Any?>("\n")}
        """.trimIndent()
      )
    }
  }
}
