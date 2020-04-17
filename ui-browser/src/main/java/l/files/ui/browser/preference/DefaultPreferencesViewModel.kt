package l.files.ui.browser.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class DefaultPreferencesViewModel(context: Context) : ViewModel() {

  val preferences = liveData<SharedPreferences> {
    emit(withContext(IO) { getDefaultSharedPreferences(context) })
  }

}

private class DefaultPreferencesViewModelFactory(
  private val context: Context
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
    modelClass.cast(DefaultPreferencesViewModel(context))!!
}

fun Fragment.getDefaultPreferencesViewModel(): DefaultPreferencesViewModel =
  ViewModelProvider(
    requireActivity(),
    DefaultPreferencesViewModelFactory(requireContext())
  ).get()
