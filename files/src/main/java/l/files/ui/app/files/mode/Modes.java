package l.files.ui.app.files.mode;

import android.widget.AbsListView;
import l.files.ui.mode.MultiChoiceModeAction;

public final class Modes {

  public static MultiChoiceModeAction newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  private Modes() { }
}
