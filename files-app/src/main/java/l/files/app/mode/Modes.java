package l.files.app.mode;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.support.v4.app.FragmentManager;
import android.widget.AbsListView;

import static android.widget.AbsListView.MultiChoiceModeListener;

public final class Modes {
  private Modes() {}

  public static MultiChoiceModeListener newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceModeListener newDeleteAction(AbsListView list, ContentResolver resolver) {
    return new DeleteAction(list, resolver);
  }

  public static MultiChoiceModeListener newSelectAllAction(AbsListView list) {
    return new SelectAllAction(list);
  }

  public static MultiChoiceModeListener newCutAction(AbsListView list, ClipboardManager manager) {
    return new CutAction(list, manager);
  }

  public static MultiChoiceModeListener newCopyAction(AbsListView list, ClipboardManager manager) {
    return new CopyAction(list, manager);
  }

  public static MultiChoiceModeListener newRenameAction(
      AbsListView list, FragmentManager manager, String parentId) {
    return new RenameAction(list, manager, parentId);
  }
}
