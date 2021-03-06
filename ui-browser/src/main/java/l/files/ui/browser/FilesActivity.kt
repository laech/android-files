package l.files.ui.browser

import android.content.ContentResolver
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.view.ActionMode
import androidx.core.view.GravityCompat.START
import androidx.drawerlayout.widget.DrawerLayout.*
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import kotlinx.android.synthetic.main.files_activity.*
import l.files.ui.base.app.BaseActivity
import l.files.ui.base.app.OptionsMenus
import l.files.ui.base.fs.IOExceptions
import l.files.ui.base.fs.OpenFileEvent
import l.files.ui.base.fs.UserDirs
import l.files.ui.browser.menu.ActionBarDrawerToggleMenu
import l.files.ui.browser.menu.GoBackOnHomePressedMenu
import l.files.ui.browser.menu.NewTabMenu
import l.files.ui.preview.getPreview
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Consumer

class FilesActivity : BaseActivity(),
  OnBackStackChangedListener,
  OnItemSelectedListener {

  private lateinit var hierarchy: HierarchyAdapter

  lateinit var navigationIcon: DrawerArrowDrawable
    private set

  private val openFileListener = Consumer { event: OpenFileEvent ->
    onOpen(event)
  }

  fun hierarchy(): List<Path> {
    return hierarchy.get()
  }

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)
    setContentView(R.layout.files_activity)
    getPreview().readCacheAsyncIfNeeded()

    navigationIcon = DrawerArrowDrawable(this)
    navigationIcon.color = Color.WHITE
    toolbarView.navigationIcon = navigationIcon
    setSupportActionBar(toolbarView)

    hierarchy = HierarchyAdapter()

    titleView.adapter = hierarchy
    titleView.onItemSelectedListener = this

    val actionBar = supportActionBar!!
    actionBar.setDisplayHomeAsUpEnabled(true)
    actionBar.setDisplayShowTitleEnabled(false)
    setOptionsMenu(
      OptionsMenus.compose(
        ActionBarDrawerToggleMenu(drawerView, supportFragmentManager),
        GoBackOnHomePressedMenu(this),
        NewTabMenu(this)
      )
    )

    supportFragmentManager.addOnBackStackChangedListener(this)
    if (state == null) {
      supportFragmentManager
        .beginTransaction()
        .replace(
          R.id.content,
          FilesFragment.create(initialDirectory),
          FilesFragment.TAG
        )
        .commit()
    }

    Handler().post(::updateToolBar)
    OpenFileEvent.topic.weakSubscribeOnMainThread(openFileListener)
  }

  override fun onDestroy() {
    OpenFileEvent.topic.unsubscribeOnMainThread(openFileListener)
    supportFragmentManager.removeOnBackStackChangedListener(this)
    super.onDestroy()
  }

  override fun onItemSelected(
    parent: AdapterView<*>,
    view: View,
    position: Int,
    id: Long
  ) {
    val path = parent.adapter.getItem(position) as Path
    if (path != fragment.directory()) {
      onOpen(OpenFileEvent(path))
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>) {}

  // TODO
  private val initialDirectory: Path
    get() {
      var dir: Path? = intent.getStringExtra(EXTRA_DIRECTORY)?.let(Paths::get)
      if (dir == null && intent?.data?.scheme == ContentResolver.SCHEME_FILE) {
        dir = Paths.get(intent.data!!.path) // TODO
      }
      return dir ?: UserDirs.DIR_HOME
    }

  override fun onPause() {
    this.getPreview().writeCacheAsyncIfNeeded()
    super.onPause()
  }

  override fun onBackPressed() {
    if (isSidebarOpen) {
      closeSidebar()
    } else {
      super.onBackPressed()
    }
  }

  override fun onBackStackChanged() {
    updateToolBar()
  }

  private fun updateToolBar() {
    val directory = fragment.directory()
    val backStacks = supportFragmentManager.backStackEntryCount
    navigationIcon.progress = if (backStacks == 0) 0f else 1f
    hierarchy.set(directory)
    titleView.setSelection(hierarchy.indexOf(directory))
  }

  override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
    return if (keyCode == KeyEvent.KEYCODE_BACK) {
      while (supportFragmentManager.backStackEntryCount > 0) {
        supportFragmentManager.popBackStackImmediate()
      }
      true
    } else {
      super.onKeyLongPress(keyCode, event)
    }
  }

  override fun onSupportActionModeFinished(mode: ActionMode) {
    super.onSupportActionModeFinished(mode)
    drawerView.setDrawerLockMode(LOCK_MODE_UNLOCKED)
  }

  override fun onSupportActionModeStarted(mode: ActionMode) {
    super.onSupportActionModeStarted(mode)
    drawerView.setDrawerLockMode(
      if (isSidebarOpen) LOCK_MODE_LOCKED_OPEN
      else LOCK_MODE_LOCKED_CLOSED
    )
  }

  private val isSidebarOpen: Boolean
    get() = drawerView.isDrawerOpen(START)

  private fun closeSidebar() {
    drawerView.closeDrawer(START)
  }

  private fun onOpen(event: OpenFileEvent) {
    currentActionMode()?.finish()
    if (drawerView.isDrawerOpen(START)) {
      drawerView.closeDrawers()
    }
    ShowTask(event.path, this).execute()
  }

  private class ShowTask(
    private val path: Path,
    activity: FilesActivity
  ) : AsyncTask<Void, Void, Any>() {

    private val activityRef: WeakReference<FilesActivity> =
      WeakReference(activity)

    override fun doInBackground(vararg params: Void): Any = try {
      Files.readAttributes(path, BasicFileAttributes::class.java)
    } catch (e: IOException) {
      e
    }

    override fun onPostExecute(result: Any) {
      super.onPostExecute(result)
      val activity = activityRef.get()
      if (activity != null && !activity.isFinishing) {
        if (result is BasicFileAttributes) {
          activity.doShow(path, result)
        } else {
          val msg = IOExceptions.message(result as IOException)
          Toast.makeText(activity, msg, LENGTH_SHORT).show()
        }
      }
    }
  }

  private fun doShow(path: Path, attrs: BasicFileAttributes) {
    if (!isReadable(path)) { // TODO Check in background
      showPermissionDenied()
    } else if (attrs.isDirectory) {
      showDirectory(path)
    } else {
      showFile(path)
    }
  }

  private fun isReadable(path: Path): Boolean = try {
    Files.isReadable(path)
  } catch (e: IOException) {
    false
  }

  private fun showPermissionDenied() {
    Toast.makeText(this, R.string.permission_denied, LENGTH_SHORT).show()
  }

  private fun showDirectory(path: Path) {
    if (fragment.directory() == path) {
      return
    }
    val f = FilesFragment.create(path)
    supportFragmentManager
      .beginTransaction()
      .setBreadCrumbTitle(
        path.toAbsolutePath().fileName?.toString() ?: path.toString()
      )
      .setTransition(TRANSIT_FRAGMENT_OPEN)
      .replace(R.id.content, f, FilesFragment.TAG)
      .addToBackStack(null)
      .commit()
  }

  private fun showFile(file: Path) {
    OpenFile(this, file).execute()
  }

  val fragment: FilesFragment
    get() = supportFragmentManager
      .findFragmentByTag(FilesFragment.TAG) as FilesFragment

  companion object {
    const val EXTRA_DIRECTORY = "directory"
  }
}
