package l.files.ui.browser

import android.app.Instrumentation
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.view.KeyEvent.KEYCODE_BACK
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.collection.SimpleArrayMap
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.view.GravityCompat.START
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.files_activity.*
import kotlinx.android.synthetic.main.files_grid_item.view.*
import l.files.base.Function
import l.files.fs.hierarchy
import l.files.ui.base.fs.FileInfo
import l.files.ui.base.fs.FileLabels
import l.files.ui.browser.Instrumentations.*
import org.junit.Assert.*
import java.io.IOException
import java.nio.file.Files.list
import java.nio.file.Files.readAttributes
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit
import java.util.function.Supplier


internal class UiFileActivity(
  val instrumentation: Instrumentation,
  provider: Supplier<FilesActivity>
) {

  val activity: FilesActivity by lazy { provider.get() }

  private val fragment: FilesFragment
    get() = activity.fragment

  fun bookmark(): UiFileActivity {
    assertBookmarkMenuChecked(false)
    selectMenuAction(R.id.bookmark)
    return this
  }

  fun unbookmark(): UiFileActivity {
    assertBookmarkMenuChecked(true)
    selectMenuAction(R.id.bookmark)
    return this
  }

  fun newFolder(): UiNewDir {
    selectMenuAction(R.id.new_dir)
    return UiNewDir(this)
  }

  fun rename(): UiRename {
    selectActionModeAction(R.id.rename)
    return UiRename(this)
  }

  fun delete(): UiDelete {
    selectActionModeAction(R.id.delete)
    return UiDelete(this)
  }

  fun copy(): UiFileActivity {
    selectActionModeAction(android.R.id.copy)
    waitForActionModeToFinish()
    return this
  }

  fun cut(): UiFileActivity {
    selectActionModeAction(android.R.id.cut)
    waitForActionModeToFinish()
    return this
  }

  fun paste(): UiFileActivity {
    selectMenuAction(android.R.id.paste)
    return this
  }

  fun sort(): UiSort {
    selectMenuAction(R.id.sort_by)
    return UiSort(this)
  }

  fun selectFromNavigationMode(dir: Path): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      val position = activity.hierarchy().indexOf(dir)
      activity.titleView.setSelection(position)
    }
    return this
  }

  fun clickInto(file: Path): UiFileActivity {
    click(file)
    assertCurrentDirectory(file)
    return this
  }

  fun click(file: Path): UiFileActivity {
    clickItemOnMainThread(instrumentation, Supplier(recycler), file)
    return this
  }

  fun longClick(file: Path): UiFileActivity {
    longClickItemOnMainThread(
      instrumentation,
      Supplier(recycler),
      file
    )
    return this
  }

  fun openBookmarksDrawer(): UiBookmarksFragment {
    awaitOnMainThread(instrumentation) {
      activity.drawerView.openDrawer(START)
    }
    assertDrawerIsOpened(true)
    return UiBookmarksFragment(this)
  }

  fun assertDrawerIsOpened(opened: Boolean): UiFileActivity {
    awaitOnMainThread(
      instrumentation
    ) {
      assertEquals(
        opened,
        activity.drawerView.isDrawerOpen(START)
      )
    }
    return this
  }

  fun pressBack(): UiFileActivity {
    instrumentation.waitForIdleSync()
    instrumentation.sendKeyDownUpSync(KEYCODE_BACK)
    instrumentation.waitForIdleSync()
    return this
  }

  fun longPressBack(): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      assertTrue(activity.onKeyLongPress(KEYCODE_BACK, null))
    }
    return this
  }

  fun pressActionBarUpIndicator(): UiFileActivity {
    waitForUpIndicatorToAppear()
    awaitOnMainThread(instrumentation) {
      val item = TestMenuItem(android.R.id.home)
      assertTrue(activity.onOptionsItemSelected(item))
    }
    return this
  }

  private fun waitForUpIndicatorToAppear() {
    awaitOnMainThread(instrumentation) {
      assertEquals(1f, activity.navigationIcon.progress)
    }
  }

  fun assertCanRename(can: Boolean): UiFileActivity {
    assertEquals(can, renameMenu().isEnabled)
    return this
  }

  fun assertCanPaste(can: Boolean): UiFileActivity =
    findOptionMenuItem(android.R.id.paste) {
      assertEquals("Paste menu enabled to be $can", can, it.isEnabled)
    }

  private fun findOptionMenuItem(
    id: Int,
    consumer: (MenuItem) -> Unit
  ): UiFileActivity {
    awaitOnMainThread(instrumentation) {

      val toolbar = activity.toolbarView
      toolbar.hideOverflowMenu()
      toolbar.showOverflowMenu()

      val item = toolbar.menu.findItem(id)
      assertNotNull(item)
      consumer(item)
      toolbar.hideOverflowMenu()
    }
    return this
  }

  private fun clickOptionMenuItem(id: Int): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      activity.toolbarView.menu.performIdentifierAction(id, 0)
    }
    return this
  }

  fun assertCurrentDirectory(expected: Path): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      val fragment = activity.fragment
      val actual = fragment.directory()
      assertEquals(expected, actual)
    }
    return this
  }

  fun assertListViewContains(item: Path, contains: Boolean): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      assertEquals(contains, resources().contains(item))
    }
    return this
  }

  fun assertActionBarTitle(title: String): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      assertEquals(title, label(activity.titleView.selectedItem as Path))
    }
    return this
  }

  private fun label(file: Path): String =
    FileLabels.get(activity.resources, file)

  fun assertActionBarUpIndicatorIsVisible(visible: Boolean): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      assertEquals(if (visible) 1f else 0f, activity.navigationIcon.progress)
    }
    return this
  }

  private fun <R> findItemOnMainThread(file: Path, function: (View) -> R): R =
    findItemOnMainThread(
      instrumentation,
      Supplier(recycler),
      file,
      Function(function)
    )

  private val recycler: () -> RecyclerView = {
    awaitOnMainThread<RecyclerView>(instrumentation) { fragment.recycler }
  }

  private fun renameMenu(): MenuItem =
    activity.currentActionMode()!!.menu.findItem(R.id.rename)

  private fun selectMenuAction(id: Int): UiFileActivity {
    findOptionMenuItem(id) { assertTrue(it.isEnabled) }
    return clickOptionMenuItem(id)
  }

  fun selectActionModeAction(id: Int) {
    awaitOnMainThread(instrumentation) {
      val mode = activity.currentActionMode()!!
      val item = mode.menu.findItem(id)
      assertTrue(
        activity
          .currentActionModeCallback()!!
          .onActionItemClicked(mode, item)
      )
    }
  }

  fun waitForActionModeToFinish() {
    awaitOnMainThread(instrumentation) {
      assertNull(activity.currentActionMode())
    }
  }

  /**
   * Clicks the "Select All" action item.
   */
  fun selectAll(): UiFileActivity {
    selectActionModeAction(android.R.id.selectAll)
    return this
  }

  /**
   * Asserts whether the given item is currently checked.
   */
  fun assertChecked(file: Path, checked: Boolean): UiFileActivity {
    findItemOnMainThread(file) {
      assertEquals(
        checked,
        it.isActivated
      )
    }
    return this
  }

  /**
   * Asserts whether the activity.get() currently in an action mode.
   */
  fun assertActionModePresent(present: Boolean): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      assertEquals(present, activity.currentActionMode() != null)
    }
    return this
  }

  fun assertActionModeTitle(title: Any): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      val mode = activity.currentActionMode()!!
      assertEquals(title.toString(), mode.title.toString())
    }
    return this
  }

  fun assertBookmarkMenuChecked(checked: Boolean): UiFileActivity =
    findOptionMenuItem(R.id.bookmark) {
      assertEquals(checked, it.isChecked)
    }

  fun assertThumbnailShown(
    path: Path,
    shown: Boolean
  ): UiFileActivity {
    findItemOnMainThread(path) {
      val drawable = it.image.drawable
      assertEquals(
        shown,
        drawable is BitmapDrawable || drawable is RoundedBitmapDrawable
      )
    }
    return this
  }

  fun assertLinkPathDisplayed(
    link: Path,
    target: Path?
  ): UiFileActivity {
    findItemOnMainThread(link) {
      val linkView = it.findViewById<TextView>(R.id.link)
      if (target != null) {
        val res = it.resources
        val expected = res.getString(R.string.link_x, target)
        val actual = linkView.text.toString()
        assertEquals(expected, actual)
        assertEquals(VISIBLE, linkView.visibility)
      } else {
        assertEquals(GONE, linkView.visibility)
      }
    }
    return this
  }

  fun assertSummary(path: Path, expected: CharSequence): UiFileActivity =
    assertSummary(path) {
      assertEquals(expected, it)
    }

  fun assertSummary(path: Path, assertion: (String) -> Unit): UiFileActivity {
    findItemOnMainThread(path) { assertion(it.summary.text.toString()) }
    return this
  }

  fun getSummary(path: Path): String =
    findItemOnMainThread(path) { it.summary.text.toString() }

  fun assertBookmarksSidebarIsClosed(): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      assertEquals(false, activity.drawerView.isDrawerOpen(START))
    }
    return this
  }

  fun assertDisabled(path: Path): UiFileActivity {
    findItemOnMainThread(path) {
      assertFalse(it.title.isEnabled)
      assertFalse(it.summary.isEnabled)
      assertFalse(it.link.isEnabled)
    }
    return this
  }

  fun assertNavigationModeHierarchy(dir: Path): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      val actual = activity.hierarchy()
      val expected = dir.hierarchy()
      assertEquals(expected, actual)
      assertEquals(dir, activity.titleView.selectedItem)
    }
    return this
  }

  @JvmOverloads
  @Throws(IOException::class)
  fun assertListMatchesFileSystem(
    dir: Path,
    timeout: Int = 1,
    timeoutUnit: TimeUnit = TimeUnit.MINUTES
  ): UiFileActivity {
    await(
      {
        val filesInView = fileAttrsInView()
        list(dir).use {
          it.forEach { child ->

            val oldAttrs = filesInView.remove(child)
            if (oldAttrs == null) {
              fail("Path in file system but not in view: $child")
            }

            val newAttrs = readAttributes(
              child,
              BasicFileAttributes::class.java,
              NOFOLLOW_LINKS
            ).toMap()
            if (newAttrs != oldAttrs) {
              fail(
                """
                    Path details differ for : $child
                    new: $newAttrs
                    old: $oldAttrs
                    """.trimIndent()
              )
            }
          }
        }

        if (!filesInView.isEmpty) {
          fail(
            "Path in view but not on file system:" +
              " ${filesInView.keyAt(0)}" +
              "=${filesInView.valueAt(0)}"
          )
        }
        null
      },
      timeout.toLong(),
      timeoutUnit
    )
    return this
  }

  private fun fileAttrsInView(): SimpleArrayMap<Path, Map<String, Any>> {
    val items = fileItems()
    val result = SimpleArrayMap<Path, Map<String, Any>>(items.size)
    for (item in items) {
      result.put(item.selfPath(), item.selfAttrs()?.toMap())
    }
    return result
  }

  private fun BasicFileAttributes.toMap() = mapOf(
    "size" to size(),
    "lastModifiedTime" to lastModifiedTime(),
  )

  private fun fileItems(): List<FileInfo> =
    fragment.items().filterIsInstance<FileInfo>()

  private fun resources(): List<Path> =
    fileItems().map(FileInfo::selfPath)

  fun assertAllItemsDisplayedInOrder(vararg expected: Path): UiFileActivity {
    awaitOnMainThread(instrumentation) {
      val actual = resources()
      if (listOf(*expected) != actual) {
        throw AssertionError(
          """
                expected in order:
                ${TextUtils.join("\n", expected)}
                bus was:
                ${TextUtils.join("\n", actual)}
                """.trimIndent()
        )
      }
    }
    return this
  }

  val info: UiInfo
    get() {
      selectActionModeAction(R.id.info)
      return UiInfo(this)
    }

}
