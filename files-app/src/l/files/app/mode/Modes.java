package l.files.app.mode;

import android.content.Context;
import android.widget.AbsListView;
import l.files.common.widget.MultiChoiceAction;

public final class Modes {
  private Modes() {}

  public static MultiChoiceAction newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceAction newDeleteAction(Context context, AbsListView list) {
    return new DeleteAction(context, list);
  }
}
