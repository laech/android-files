package l.files.app.mode;

import android.support.v4.app.FragmentManager;
import android.widget.AbsListView;
import l.files.common.widget.MultiChoiceAction;

public final class Modes {
  private Modes() {}

  public static MultiChoiceAction newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceAction newDeleteAction(FragmentManager manager, AbsListView list) {
    return new DeleteFilesAction(manager, list);
  }

  public static MultiChoiceAction newSelectAllAction(AbsListView list) {
    return new SelectAllAction(list);
  }
}
