package l.files.ui.browser

import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.text.format.DateUtils.*
import android.text.format.Formatter.formatShortFileSize
import android.util.Log
import l.files.base.io.Charsets.UTF_8
import l.files.fs.Instant
import l.files.fs.Instant.EPOCH
import l.files.fs.LinkOption.NOFOLLOW
import l.files.fs.Path
import l.files.fs.Permission
import l.files.testing.fs.Paths
import l.files.ui.browser.sort.FileSort
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.util.*
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

class NavigationTest : BaseFilesActivityTest() {

  @Test
  fun can_see_file_renamed_to_different_casing() {
    val dir = try {
      createCaseInsensitiveFileSystemDir("can_see_file_renamed_to_different_casing")
    } catch (e: CannotRenameFileToDifferentCasing) {
      Log.d(
        "NavigationTest",
        "skipping test_can_see_file_renamed_to_different_casing()",
        e
      )
      return
    }

    try {

      val src = dir.concat("a").createDirectory()
      val dst = dir.concat("A")
      screen().assertAllItemsDisplayedInOrder(src)
      src.rename(dst)
      screen()
        .sort()
        .by(FileSort.NAME)
        .assertAllItemsDisplayedInOrder(dst)

    } finally {
      Paths.deleteRecursiveIfExists(dir)
    }
  }

  @Test
  fun can_start_from_data_uri() {
    val dir = dir().concat("dir").createDirectories()
    val file = dir.concat("file").createFile()
    setActivityIntent(Intent().setData(dir.toUri()))
    screen()
      .assertCurrentDirectory(dir)
      .assertListViewContains(file, true)
  }

  @Test
  fun can_preview() {
    val dir = dir().concat("test_can_preview").createDirectory()
    val empty = dir.concat("empty").createFile()
    val file = dir.concat("file")
    val link = dir.concat("link").createSymbolicLink(file)
    Paths.writeUtf8(file, "hello")
    screen()
      .clickInto(dir)
      .assertThumbnailShown(file, true)
      .assertThumbnailShown(link, true)
      .assertThumbnailShown(empty, false)
  }

  @Test
  fun can_navigate_into_non_utf8_named_dir() {
    val nonUtf8Name = byteArrayOf(-19, -96, -67, -19, -80, -117)
    assertNotEquals(
      nonUtf8Name.clone(),
      nonUtf8Name.toString(UTF_8).toByteArray(UTF_8)
    )
    val nonUtf8NamedDir = dir().concat(nonUtf8Name).createDirectory()
    val child = nonUtf8NamedDir.concat("a").createFile()
    screen()
      .clickInto(nonUtf8NamedDir)
      .assertListViewContains(child, true)
  }

  @Test
  fun can_shows_dirs_with_same_name_but_different_name_bytes() {
    val notUtf8 = byteArrayOf(-19, -96, -67, -19, -80, -117)
    val utf8 = notUtf8.toString(UTF_8).toByteArray(UTF_8)
    assertFalse(notUtf8.contentEquals(utf8))
    assertEquals(notUtf8.toString(UTF_8), utf8.toString(UTF_8))

    val notUtf8Dir = dir().concat(notUtf8).createDirectory()
    val notUtf8Child = notUtf8Dir.concat("notUtf8").createFile()
    val utf8Dir = dir().concat(utf8).createDirectory()
    val utf8Child = utf8Dir.concat("utf8").createFile()

    screen()
      .assertListViewContains(notUtf8Dir, true)
      .assertListViewContains(utf8Dir, true)

      .clickInto(notUtf8Dir)
      .assertListViewContains(notUtf8Child, true)
      .assertListViewContains(utf8Child, false)
      .pressBack()

      .clickInto(utf8Dir)
      .assertListViewContains(utf8Child, true)
      .assertListViewContains(notUtf8Child, false)
  }

  @Test
  fun can_navigate_into_etc_proc_self_fdinfo_without_crashing() {
    assumeTrue(
      "Skipping test, no permission to read /proc on Android N",
      VERSION.SDK_INT < VERSION_CODES.N
    )
    screen().selectFromNavigationMode(Path.of("/"))
    screen().clickInto(Path.of("/proc"))
    screen().clickInto(Path.of("/proc/self"))
    screen().clickInto(Path.of("/proc/self/fdinfo"))
  }

  @Test
  fun can_navigate_through_title_list_drop_down() {
    val parent = dir().parent()!!
    screen()
      .selectFromNavigationMode(parent)
      .assertNavigationModeHierarchy(parent)
  }

  @Test
  fun updates_navigation_list_when_going_into_a_new_dir() {
    screen().assertNavigationModeHierarchy(dir())
    val dir = dir().concat("dir").createDirectory()
    screen().clickInto(dir).assertNavigationModeHierarchy(dir)
  }

  @Test
  fun shows_size_only_if_unable_to_determine_modified_date() {
    val file = dir().concat("file").createFile()
    file.setLastModifiedTime(NOFOLLOW, EPOCH)

    val size = file.stat(NOFOLLOW).size()
    val expected = formatShortFileSize(activity, size)

    screen().assertSummary(file) {
      assertTrue(it.contains(expected))
    }
  }

  @Test
  fun shows_time_and_size_for_file() {
    val file = dir().concat("file").createFile()
    Paths.appendUtf8(file, file.toString())

    val stat = file.stat(NOFOLLOW)
    val lastModifiedMillis = stat.lastModifiedTime().to(MILLISECONDS)
    val date = getTimeFormat(activity).format(Date(lastModifiedMillis))
    val size = formatShortFileSize(activity, stat.size())
    val expected = activity.getString(R.string.x_dot_y, date, size)
    screen().assertSummary(file, expected)
  }

  @Test
  fun shows_time_only_for_today() {
    val time = currentTimeMillis()
    val format = getTimeFormat(activity)
    val expected = format.format(Date(time))
    testDirectorySummary(expected, time)
  }

  @Test
  fun shows_time_as_month_day_for_date_of_current_year() {
    val time = currentTimeMillis() - DAYS.toMillis(2)
    val flags = (FORMAT_SHOW_DATE or FORMAT_ABBREV_MONTH or FORMAT_NO_YEAR)
    val expected = formatDateTime(activity, time, flags)
    testDirectorySummary(expected, time)
  }

  @Test
  fun shows_time_as_year_month_day_for_date_outside_of_current_year() {
    val time = currentTimeMillis() - DAYS.toMillis(400)
    val format = getDateFormat(activity)
    val expected = format.format(Date(time))
    testDirectorySummary(expected, time)
  }

  private fun testDirectorySummary(expected: String, modifiedAt: Long) {
    val d = dir().concat("dir").createDirectory()
    d.setLastModifiedTime(NOFOLLOW, Instant.of(modifiedAt / 1000, 0))
    screen().assertSummary(d, expected)
  }

  @Test
  fun directory_view_is_disabled_if_no_read_permission() {
    val dir = dir().concat("dir").createDirectory()
    Paths.removePermissions(dir, Permission.read())
    screen().assertDisabled(dir)
  }

  @Test
  fun link_displayed() {
    val dir = dir().concat("dir").createDirectory()
    val link = dir().concat("link").createSymbolicLink(dir)
    screen()
      .assertLinkPathDisplayed(dir, null)
      .assertLinkPathDisplayed(link, dir)
  }

  @Test
  fun can_see_changes_in_parent_directory() {
    val level1Dir = dir()
    val level2Dir = level1Dir.concat("level2Dir").createDirectory()
    val level3Dir = level2Dir.concat("level3Dir").createDirectory()

    screen()
      .sort()
      .by(FileSort.NAME)
      .clickInto(level2Dir)
      .clickInto(level3Dir)
      .assertCurrentDirectory(level3Dir)

    val level3File = level2Dir.concat("level3File").createFile()
    val level2File = level1Dir.concat("level2File").createFile()

    screen()
      .pressBack()
      .assertAllItemsDisplayedInOrder(level3Dir, level3File)
      .pressBack()
      .assertAllItemsDisplayedInOrder(level2Dir, level2File)
  }

  @Test
  fun can_see_changes_in_linked_directory() {
    val dir = dir().concat("dir").createDirectory()
    val link = dir().concat("link").createSymbolicLink(dir)

    screen()
      .clickInto(link)
      .assertCurrentDirectory(link)

    val child = link.concat("child").createDirectory()

    screen()
      .clickInto(child)
      .assertCurrentDirectory(child)
  }

  @Test
  fun press_action_bar_up_indicator_will_go_back() {
    val dir = dir().concat("dir").createDirectory()
    val parent = dir.parent()!!

    screen()
      .clickInto(dir)
      .assertCurrentDirectory(dir)
      .pressActionBarUpIndicator()
      .assertCurrentDirectory(parent)
  }

  @Test
  fun action_bar_title_shows_name_of_directory() {
    screen()
      .clickInto(dir().concat("a").createDirectory())
      .assertActionBarTitle("a")
  }

  @Test
  fun action_bar_hides_up_indicator_when_there_is_no_back_stack_initially() {
    screen().assertActionBarUpIndicatorIsVisible(false)
  }

  @Test
  fun action_bar_shows_up_indicator_when_there_is_back_stack() {
    screen()
      .clickInto(dir().concat("dir").createDirectory())
      .assertActionBarUpIndicatorIsVisible(true)
  }

  @Test
  fun action_bar_hides_up_indicator_when_there_is_no_back_stack_to_go_back_to() {
    screen()
      .clickInto(dir().concat("dir").createDirectory())
      .pressBack()
      .assertActionBarUpIndicatorIsVisible(false)
  }

  @Test
  fun long_press_back_will_clear_back_stack() {
    screen()
      .clickInto(dir().concat("a").createDirectory())
      .clickInto(dir().concat("a/b").createDirectory())
      .clickInto(dir().concat("a/b/c").createDirectory())
      .longPressBack()
      .assertCurrentDirectory(dir())
  }

  @Test
  fun observes_on_current_directory_and_shows_added_deleted_files() {
    val a = dir().concat("a").createDirectory()
    screen().assertListViewContains(a, true)

    val b = dir().concat("b").createFile()
    screen()
      .assertListViewContains(a, true)
      .assertListViewContains(b, true)

    b.delete()
    screen()
      .assertListViewContains(a, true)
      .assertListViewContains(b, false)
  }

  @Test
  fun updates_view_on_child_directory_modified() {
    val dir = dir().concat("a").createDirectory()
    testUpdatesDateViewOnChildModified(dir)
  }

  @Test
  fun updates_view_on_child_file_modified() {
    val file = dir().concat("a").createFile()
    testUpdatesDateViewOnChildModified(file)
    testUpdatesSizeViewOnChildModified(file)
  }

  private fun testUpdatesSizeViewOnChildModified(file: Path) {
    file.setLastModifiedTime(NOFOLLOW, EPOCH)

    val outdated = screen().getSummary(file)
    modify(file)

    screen().assertSummary(file) {
      assertNotEquals(outdated, it)
    }
  }

  private fun testUpdatesDateViewOnChildModified(file: Path) {
    file.setLastModifiedTime(NOFOLLOW, Instant.of(100000, 1))

    val outdated = screen().getSummary(file)
    modify(file)

    screen().assertSummary(file) {
      assertNotEquals(outdated, it)
    }
  }

  private fun modify(file: Path) {
    val stat = file.stat(NOFOLLOW)
    val lastModifiedBefore = stat.lastModifiedTime()
    if (stat.isDirectory) {
      file.concat(nanoTime().toString()).createDirectory()
    } else {
      Paths.appendUtf8(file, "test")
    }
    val lastModifiedAfter = file.stat(NOFOLLOW).lastModifiedTime()
    assertNotEquals(lastModifiedBefore, lastModifiedAfter)
  }
}
