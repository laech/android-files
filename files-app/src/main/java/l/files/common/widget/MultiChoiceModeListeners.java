package l.files.common.widget;

import static android.widget.AbsListView.MultiChoiceModeListener;

public final class MultiChoiceModeListeners {
  private MultiChoiceModeListeners() {}

  public static CompositeMultiChoiceAction compose(MultiChoiceModeListener... listeners) {
    return new CompositeMultiChoiceAction(listeners);
  }
}
