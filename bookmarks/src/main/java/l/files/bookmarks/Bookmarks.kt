package l.files.bookmarks

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Environment.*
import android.util.Log
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import l.files.base.lifecycle.CollectionLiveData.Companion.setLiveData
import l.files.base.lifecycle.SetLiveData
import l.files.base.text.CollationKey
import java.nio.file.Files.exists
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.Collator
import java.util.Collections.unmodifiableSet

private const val TAG = "bookmarks"
internal const val PREF_KEY = "bookmarks"

private var bookmarks: SetLiveData<Path>? = null

@MainThread
fun Fragment.getBookmarks(): SetLiveData<Path> {
  if (bookmarks == null) {
    bookmarks = initBookmarks(requireActivity().application)
  }
  return bookmarks!!
}

private fun initBookmarks(app: Application): SetLiveData<Path> {
  val bookmarks = setLiveData<Path>()
  GlobalScope.launch(Dispatchers.Main) {
    val (pref, paths) = withContext(Dispatchers.IO) {
      val pref = app.getSharedPreferences("bookmarks", MODE_PRIVATE)
      Pair(pref, loadBookmarks(pref))
    }
    bookmarks.addAll(paths)
    bookmarks.observeForever {
      if (bookmarks.oldValue != null && bookmarks.oldValue != it) {
        pref
          .edit()
          .putStringSet(PREF_KEY, it.map(Path::toString).toSet())
          .apply()
      }
    }
  }
  return bookmarks
}

fun Collection<Path>.collate(
  alwaysOnTop: (Path) -> Boolean = { false },
  collator: Collator = Collator.getInstance()
): List<Path> = partition(alwaysOnTop).let { (top, bottom) ->
  top + bottom
    .map {
      Pair(
        it,
        CollationKey.create(collator, it.fileName?.toString() ?: "")
      )
    }
    .sortedBy { it.second }
    .map { it.first }
    .toList()
}

internal fun loadBookmarks(pref: SharedPreferences): Set<Path> {
  val encodedPaths = pref.getStringSet(PREF_KEY, null)
  return if (encodedPaths != null) {
    decode(encodedPaths)
  } else {
    loadDefaultBookmarks()
  }
}

private fun decode(encoded: Collection<String>): Set<Path> {
  val bookmarks = mutableSetOf<Path>()
  for (element in encoded) {
    try {
      val path = Paths.get(element)
      if (exists(path)) {
        bookmarks.add(path)
      }
    } catch (e: InvalidPathException) {
      Log.w(TAG, "Invalid bookmark: $element", e)
    }
  }
  return unmodifiableSet(bookmarks)
}

private fun loadDefaultBookmarks(): Set<Path> {
  val defaults = mutableSetOf<Path>()
  addIfExists(defaults, getExternalStorageDirectory().toPath())
  addIfExists(defaults, externalStoragePath(DIRECTORY_DCIM))
  addIfExists(defaults, externalStoragePath(DIRECTORY_MUSIC))
  addIfExists(defaults, externalStoragePath(DIRECTORY_MOVIES))
  addIfExists(defaults, externalStoragePath(DIRECTORY_PICTURES))
  addIfExists(defaults, externalStoragePath(DIRECTORY_DOWNLOADS))
  addIfExists(defaults, Paths.get("/sdcard2"))
  return unmodifiableSet(defaults)
}

private fun addIfExists(paths: MutableSet<Path>, path: Path) {
  if (exists(path)) {
    paths.add(path)
  }
}

private fun externalStoragePath(name: String): Path =
  getExternalStorageDirectory().toPath().resolve(name)
