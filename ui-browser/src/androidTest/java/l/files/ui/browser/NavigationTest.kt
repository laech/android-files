package l.files.ui.browser

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.format.DateFormat.getDateFormat
import android.text.format.DateFormat.getTimeFormat
import android.text.format.DateUtils.*
import android.text.format.Formatter.formatShortFileSize
import android.util.Log
import l.files.testing.fs.Paths
import l.files.testing.fs.Paths.removeReadPermissions
import l.files.ui.browser.sort.FileSort
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.*
import java.util.Collections.singleton
import java.util.concurrent.TimeUnit.DAYS

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

      val src = createDirectory(dir.resolve("a"))
      val dst = dir.resolve("A")
      screen().assertAllItemsDisplayedInOrder(src)
      move(src, dst)
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
    val dir = createDirectories(dir().resolve("dir"))
    val file = createFile(dir.resolve("file"))
    setActivityIntent(Intent().setData(Uri.fromFile(dir.toFile())))
    screen()
      .assertCurrentDirectory(dir)
      .assertListViewContains(file, true)
  }

  @Test
  fun can_preview() {
    val dir = createDirectory(dir().resolve("test_can_preview"))
    val empty = createFile(dir.resolve("empty"))
    val file = dir.resolve("file")
    val link = createSymbolicLink(dir.resolve("link"), file)
    write(file, singleton("hello"))
    screen()
      .clickInto(dir)
      .assertThumbnailShown(file, true)
      .assertThumbnailShown(link, true)
      .assertThumbnailShown(empty, false)
  }

  @Test
  fun can_navigate_into_etc_proc_self_fdinfo_without_crashing() {
    assumeTrue(
      "Skipping test, no permission to read /proc on Android N",
      VERSION.SDK_INT < VERSION_CODES.N
    )
    screen().selectFromNavigationMode(java.nio.file.Paths.get("/"))
    screen().clickInto(java.nio.file.Paths.get("/proc"))
    screen().clickInto(java.nio.file.Paths.get("/proc/self"))
    screen().clickInto(java.nio.file.Paths.get("/proc/self/fdinfo"))
  }

  @Test
  fun can_navigate_through_title_list_drop_down() {
    screen()
      .selectFromNavigationMode(dir().parent)
      .assertNavigationModeHierarchy(dir().parent)
  }

  @Test
  fun updates_navigation_list_when_going_into_a_new_dir() {
    screen().assertNavigationModeHierarchy(dir())
    val dir = createDirectory(dir().resolve("dir"))
    screen().clickInto(dir).assertNavigationModeHierarchy(dir)
  }

  @Test
  fun shows_size_only_if_unable_to_determine_modified_date() {
    val file = createFile(dir().resolve("file"))
    setLastModifiedTime(file, FileTime.fromMillis(0))

    val expected = formatShortFileSize(activity, size(file))
    screen().assertSummary(file) {
      assertTrue(it.contains(expected))
    }
  }

  @Test
  fun shows_time_and_size_for_file() {
    val file = createFile(dir().resolve("file"))
    write(file, singleton(file.toString()))

    val lastModifiedMillis = getLastModifiedTime(file).toMillis()
    val date = getTimeFormat(activity).format(Date(lastModifiedMillis))
    val size = formatShortFileSize(activity, size(file))
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
    val d = createDirectory(dir().resolve("dir"))
    setLastModifiedTime(
      d,
      FileTime.from(Instant.ofEpochSecond(modifiedAt / 1000, 0))
    )
    screen().assertSummary(d, expected)
  }

  @Test
  fun directory_view_is_disabled_if_no_read_permission() {
    val dir = createDirectory(dir().resolve("dir"))
    removeReadPermissions(dir)
    screen().assertDisabled(dir)
  }

  @Test
  fun link_displayed() {
    val dir = createDirectory(dir().resolve("dir"))
    val link = createSymbolicLink(dir().resolve("link"), dir)
    screen()
      .assertLinkPathDisplayed(dir, null)
      .assertLinkPathDisplayed(link, dir)
  }

  @Test
  fun can_see_changes_in_parent_directory() {
    val level1Dir = dir()
    val level2Dir = createDirectory(level1Dir.resolve("level2Dir"))
    val level3Dir = createDirectory(level2Dir.resolve("level3Dir"))

    screen()
      .sort()
      .by(FileSort.NAME)
      .clickInto(level2Dir)
      .clickInto(level3Dir)
      .assertCurrentDirectory(level3Dir)

    val level3File = createFile(level2Dir.resolve("level3File"))
    val level2File = createFile(level1Dir.resolve("level2File"))

    screen()
      .pressBack()
      .assertAllItemsDisplayedInOrder(level3Dir, level3File)
      .pressBack()
      .assertAllItemsDisplayedInOrder(level2Dir, level2File)
  }

  @Test
  fun can_see_changes_in_linked_directory() {
    val dir = createDirectory(dir().resolve("dir"))
    val link = createSymbolicLink(dir().resolve("link"), dir)

    screen()
      .clickInto(link)
      .assertCurrentDirectory(link)

    val child = createDirectory(link.resolve("child"))

    screen()
      .clickInto(child)
      .assertCurrentDirectory(child)
  }

  @Test
  fun press_action_bar_up_indicator_will_go_back() {
    val dir = createDirectory(dir().resolve("dir"))
    screen()
      .clickInto(dir)
      .assertCurrentDirectory(dir)
      .pressActionBarUpIndicator()
      .assertCurrentDirectory(dir.parent)
  }

  @Test
  fun action_bar_title_shows_name_of_directory() {
    screen()
      .clickInto(createDirectory(dir().resolve("a")))
      .assertActionBarTitle("a")
  }

  @Test
  fun action_bar_hides_up_indicator_when_there_is_no_back_stack_initially() {
    screen().assertActionBarUpIndicatorIsVisible(false)
  }

  @Test
  fun action_bar_shows_up_indicator_when_there_is_back_stack() {
    screen()
      .clickInto(createDirectory(dir().resolve("dir")))
      .assertActionBarUpIndicatorIsVisible(true)
  }

  @Test
  fun action_bar_hides_up_indicator_when_there_is_no_back_stack_to_go_back_to() {
    screen()
      .clickInto(createDirectory(dir().resolve("dir")))
      .pressBack()
      .assertActionBarUpIndicatorIsVisible(false)
  }

  @Test
  fun long_press_back_will_clear_back_stack() {
    screen()
      .clickInto(createDirectory(dir().resolve("a")))
      .clickInto(createDirectory(dir().resolve("a/b")))
      .clickInto(createDirectory(dir().resolve("a/b/c")))
      .longPressBack()
      .assertCurrentDirectory(dir())
  }

  @Test
  fun observes_on_current_directory_and_shows_added_deleted_files() {
    val a = createDirectory(dir().resolve("a"))
    screen().assertListViewContains(a, true)

    val b = createFile(dir().resolve("b"))
    screen()
      .assertListViewContains(a, true)
      .assertListViewContains(b, true)

    delete(b)
    screen()
      .assertListViewContains(a, true)
      .assertListViewContains(b, false)
  }

  @Test
  fun updates_view_on_child_directory_modified() {
    val dir = createDirectory(dir().resolve("a"))
    testUpdatesDateViewOnChildModified(dir)
  }

  @Test
  fun updates_view_on_child_file_modified() {
    val file = createFile(dir().resolve("a"))
    testUpdatesDateViewOnChildModified(file)
    testUpdatesSizeViewOnChildModified(file)
  }

  private fun testUpdatesSizeViewOnChildModified(file: Path) {
    setLastModifiedTime(file, FileTime.from(Instant.EPOCH))

    val outdated = screen().getSummary(file)
    modify(file)

    screen().assertSummary(file) {
      assertNotEquals(outdated, it)
    }
  }

  private fun testUpdatesDateViewOnChildModified(file: Path) {
    setLastModifiedTime(file, FileTime.from(Instant.ofEpochSecond(100000, 1)))

    val outdated = screen().getSummary(file)
    modify(file)

    screen().assertSummary(file) {
      assertNotEquals(outdated, it)
    }
  }

  private fun modify(file: Path) {
    val lastModifiedBefore = getLastModifiedTime(file)
    if (isDirectory(file)) {
      createDirectory(file.resolve(nanoTime().toString()))
    } else {
      file.toFile().appendText("test")
    }
    val lastModifiedAfter = getLastModifiedTime(file)
    assertNotEquals(lastModifiedBefore, lastModifiedAfter)
  }
}
