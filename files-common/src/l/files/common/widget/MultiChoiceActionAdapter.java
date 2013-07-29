package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;

/**
 * An implementation that does nothing by default.
 */
public class MultiChoiceActionAdapter implements MultiChoiceAction {

  @Override public void onCreate(ActionMode mode, Menu menu) {}

  @Override public void onChange(ActionMode mode, int position, long id, boolean checked) {}
}
