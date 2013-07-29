package l.files.ui.app.files.mode;

import android.widget.AbsListView;
import l.files.common.widget.MultiChoiceMode;

import static l.files.trash.TrashService.TrashMover;

public final class Modes {
  private Modes() {}

  public static MultiChoiceMode newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceMode newMoveToTrashAction(AbsListView list, TrashMover mover) {
    return new MoveToTrashAction(list, mover);
  }
}
