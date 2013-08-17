package l.files.app.mode;

import android.support.v4.app.FragmentManager;
import android.widget.AbsListView;
import com.squareup.otto.Bus;
import l.files.common.widget.MultiChoiceAction;

public final class Modes {
  private Modes() {}

  public static MultiChoiceAction newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceAction newDeleteAction(AbsListView list, FragmentManager manager) {
    return new DeleteAction(list, manager);
  }

  public static MultiChoiceAction newSelectAllAction(AbsListView list) {
    return new SelectAllAction(list);
  }

  public static MultiChoiceAction newCutAction(AbsListView list, Bus bus) {
    return new CutAction(list, bus);
  }
}
