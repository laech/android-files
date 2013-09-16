package l.files.app.mode;

import static android.widget.AbsListView.MultiChoiceModeListener;

import android.support.v4.app.FragmentManager;
import android.widget.AbsListView;
import com.squareup.otto.Bus;

public final class Modes {
  private Modes() {}

  public static MultiChoiceModeListener newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceModeListener newDeleteAction(AbsListView list, Bus bus) {
    return new DeleteAction(list, bus);
  }

  public static MultiChoiceModeListener newSelectAllAction(AbsListView list) {
    return new SelectAllAction(list);
  }

  public static MultiChoiceModeListener newCutAction(AbsListView list, Bus bus) {
    return new CutAction(list, bus);
  }

  public static MultiChoiceModeListener newCopyAction(AbsListView list, Bus bus) {
    return new CopyAction(list, bus);
  }

  public static MultiChoiceModeListener newRenameAction(AbsListView list, FragmentManager manager) {
    return new RenameAction(list, manager);
  }
}
