package l.files.fs

import l.files.fs.exception.AccessDenied
import l.files.fs.exception.NoSuchEntry
import l.files.fs.exception.NotDirectory
import l.files.testing.fs.PathBaseTest
import l.files.testing.fs.Paths
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.stream.Collectors.toList

class PathListTest : PathBaseTest() {

  @Test
  fun list_follows_symbolic_link() {
    val dir = dir1().concat("dir").createDirectory()
    val a = dir.concat("a").createFile()
    val b = dir.concat("b").createDirectory()
    val c = dir.concat("c").createSymbolicLink(a)
    val link = dir1().concat("link").createSymbolicLink(dir)
    val expected = listOf(
      PathEntry(a.name()!!, PathType.FILE),
      PathEntry(b.name()!!, PathType.DIRECTORY),
      PathEntry(c.name()!!, PathType.SYMLINK)
    )
    link.list().sortedByName().use {
      assertThat(it.collect(toList()), equalTo(expected))
    }
  }

  @Test
  fun list_directory_content() {
    val a = dir1().concat("a").createFile()
    val b = dir1().concat("b").createDirectory()
    val expected = listOf(
      PathEntry(a.name()!!, PathType.FILE),
      PathEntry(b.name()!!, PathType.DIRECTORY)
    )
    dir1().list().sortedByName().use {
      assertThat(it.collect(toList()), equalTo(expected))
    }
  }

  @Test
  fun access_denied_failure_if_no_permission_to_read() {
    Paths.removePermissions(dir1(), Permission.read())
    listWillFail<AccessDenied>(dir1())
  }

  @Test
  fun not_such_entry_if_directory_does_not_exist() {
    listWillFail<NoSuchEntry>(dir1().concat("missing"))
  }

  @Test
  fun not_such_entry_if_symbolic_link_to_directory_does_not_exist() {
    val missing = dir1().concat("missing")
    val link = dir1().concat("link").createSymbolicLink(missing)
    listWillFail<NoSuchEntry>(link)
  }

  @Test
  fun not_such_entry_if_path_is_empty() {
    listWillFail<NoSuchEntry>(Path.of(""))
  }

  @Test
  fun not_directory_failure_if_path_is_file() {
    val file = dir1().concat("file").createFile()
    listWillFail<NotDirectory>(file)
  }

  @Test
  fun not_directory_failure_if_path_is_symbolic_link_to_file() {
    val file = dir1().concat("file").createFile()
    val link = dir1().concat("link").createSymbolicLink(file)
    listWillFail<NotDirectory>(link)
  }

  private inline fun <reified T> listWillFail(path: Path) {
    try {
      path.list().close()
      fail("Expected: " + T::class.java.name)
    } catch (e: IOException) {
      if (!T::class.java.isInstance(e)) {
        throw e
      }
    }
  }
}
