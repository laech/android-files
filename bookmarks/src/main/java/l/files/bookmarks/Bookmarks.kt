package l.files.bookmarks

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Environment.*
import android.util.Base64
import android.util.Log
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import l.files.base.text.CollationKey
import l.files.fs.LinkOption
import l.files.fs.Path
import java.io.File
import java.io.IOException
import java.text.Collator
import java.util.Collections.unmodifiableSet

@MainThread
interface BookmarksManager {
    val liveData: LiveData<Set<Path>>
    fun contains(item: Path): Boolean
    fun add(item: Path)
    fun remove(item: Path)
    fun removeAll(items: Collection<Path>)
}

private const val TAG = "bookmarks"
internal const val PREF_KEY = "bookmarks"

internal class BookmarksViewModel(
    private val pref: Lazy<SharedPreferences>
) : ViewModel(), BookmarksManager {

    override val liveData: MutableLiveData<Set<Path>> by lazy {
        val data = MutableLiveData<Set<Path>>()
        viewModelScope.launch {
            data.value =
                (data.value ?: emptySet()) + withContext(Dispatchers.IO) {
                    loadBookmarks(pref.value)
                }
        }
        data
    }

    private val items = mutableSetOf<Path>()

    override fun contains(item: Path): Boolean = items.contains(item)

    override fun add(item: Path) {
        if (items.add(item)) {
            update()
        }
    }

    override fun remove(item: Path) {
        if (items.remove(item)) {
            update()
        }
    }

    override fun removeAll(items: Collection<Path>) {
        if (this.items.removeAll(items)) {
            update()
        }
    }

    private fun update() {
        val copy = unmodifiableSet(HashSet(items))
        liveData.value = copy
        pref.value.edit().putStringSet(PREF_KEY, encode(copy)).apply()
    }
}

private class BookmarksViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        modelClass.cast(BookmarksViewModel(lazy {
            app.getSharedPreferences("bookmarks", MODE_PRIVATE)
        }))!!
}

@MainThread
fun Fragment.getBookmarkManager(): BookmarksManager =
    ViewModelProvider(
        requireActivity(),
        BookmarksViewModelFactory(requireActivity().application)
    ).get(BookmarksViewModel::class.java)

fun Collection<Path>.collate(
    alwaysOnTop: (Path) -> Boolean = { false },
    collator: Collator = Collator.getInstance()
): List<Path> = partition(alwaysOnTop).let { (top, bottom) ->
    top + bottom
        .map { Pair(it, CollationKey.create(collator, it.name.or(""))) }
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

private fun encode(path: Path): String =
    Base64.encodeToString(path.toByteArray(), Base64.DEFAULT)

internal fun encode(bookmarks: Collection<Path>): Set<String> =
    bookmarks.map(::encode).toSet()

private fun decode(encoded: String): Path =
    Path.of(Base64.decode(encoded, Base64.DEFAULT))

private fun decode(encoded: Collection<String>): Set<Path> {
    val bookmarks = mutableSetOf<Path>()
    for (element in encoded) {
        try {
            val path = decode(element)
            if (exists(path)) {
                bookmarks.add(path)
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid bookmark: $element", e)
        }
    }
    return unmodifiableSet(bookmarks)
}

private fun loadDefaultBookmarks(): Set<Path> {
    val defaults = mutableSetOf<Path>()
    addIfExists(defaults, Path.of(getExternalStorageDirectory()))
    addIfExists(defaults, externalStoragePath(DIRECTORY_DCIM))
    addIfExists(defaults, externalStoragePath(DIRECTORY_MUSIC))
    addIfExists(defaults, externalStoragePath(DIRECTORY_MOVIES))
    addIfExists(defaults, externalStoragePath(DIRECTORY_PICTURES))
    addIfExists(defaults, externalStoragePath(DIRECTORY_DOWNLOADS))
    addIfExists(defaults, Path.of("/sdcard2"))
    return unmodifiableSet(defaults)
}

private fun addIfExists(paths: MutableSet<Path>, path: Path) {
    if (exists(path)) {
        paths.add(path)
    }
}

private fun exists(path: Path): Boolean = try {
    path.exists(LinkOption.FOLLOW)
} catch (e: IOException) {
    false
}

private fun externalStoragePath(name: String): Path =
    Path.of(File(getExternalStorageDirectory(), name))
