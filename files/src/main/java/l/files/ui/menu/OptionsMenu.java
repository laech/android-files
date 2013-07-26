package l.files.ui.menu;

import android.app.Activity;
import android.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

public interface OptionsMenu {

  /**
   * @see Activity#onCreateOptionsMenu(Menu)
   * @see Fragment#onCreateOptionsMenu(Menu, MenuInflater)
   */
  void onCreate(Menu menu);

  /**
   * @see Activity#onPrepareOptionsMenu(Menu)
   * @see Fragment#onPrepareOptionsMenu(Menu)
   */
  void onPrepare(Menu menu);

  /**
   * @see Activity#onOptionsMenuClosed(Menu)
   * @see Fragment#onOptionsMenuClosed(Menu)
   */
  void onClose(Menu menu);

}
