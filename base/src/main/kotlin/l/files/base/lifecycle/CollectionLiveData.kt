package l.files.base.lifecycle

import androidx.lifecycle.LiveData
import java.util.Collections.unmodifiableSet

typealias SetLiveData<T> = CollectionLiveData<T, MutableSet<T>, Set<T>>

class CollectionLiveData<
  T,
  MC : MutableCollection<T>,
  IC : Collection<T>,
  >
private constructor(
  private val items: MC,
  private val copy: (MC) -> IC,
) : LiveData<IC>() {

  var oldValue: IC? = null

  override fun setValue(value: IC) {
    oldValue = this.value
    super.setValue(value)
  }

  fun contains(item: T): Boolean = items.contains(item)

  fun add(item: T) {
    if (items.add(item)) {
      update()
    }
  }

  fun addAll(items: Collection<T>) {
    if (this.items.addAll(items)) {
      update()
    }
  }

  fun remove(item: T) {
    if (items.remove(item)) {
      update()
    }
  }

  fun removeAll(items: Collection<T>) {
    if (this.items.removeAll(items)) {
      update()
    }
  }

  private fun update() {
    setValue(copy(items))
  }

  companion object {
    fun <T> setLiveData(): SetLiveData<T> =
      CollectionLiveData(HashSet(), { unmodifiableSet(HashSet(it)) })
  }
}
