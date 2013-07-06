package l.files.ui.mode;

import static android.widget.AbsListView.MultiChoiceModeListener;

public final class MultiChoiceModeListeners {

  public static MultiChoiceModeListener of(MultiChoiceModeAction... actions) {
    return new MultiChoiceModeDelegate(actions);
  }

  private MultiChoiceModeListeners() {}
}
