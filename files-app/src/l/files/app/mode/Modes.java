package l.files.app.mode;

import android.widget.AbsListView;
import com.squareup.otto.Bus;
import l.files.common.widget.MultiChoiceAction;

public final class Modes {
  private Modes() {}

  public static MultiChoiceAction newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceAction newDeleteAction(AbsListView list, Bus bus) {
    return new DeleteAction(list, bus);
  }

  public static MultiChoiceAction newSelectAllAction(AbsListView list) {
    return new SelectAllAction(list);
  }

  public static MultiChoiceAction newCutAction(AbsListView list, Bus bus) {
    return new CutAction(list, bus);
  }

  public static MultiChoiceAction newCopyAction(AbsListView list, Bus bus) {
    return new CopyAction(list, bus);
  }
}
