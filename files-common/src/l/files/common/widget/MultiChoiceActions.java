package l.files.common.widget;

import static android.widget.AbsListView.MultiChoiceModeListener;

public final class MultiChoiceActions {
  private MultiChoiceActions() {}

  public static MultiChoiceAction compose(MultiChoiceAction... actions) {
    return new CompositeMultiChoiceAction(actions);
  }

  public static MultiChoiceModeListener asListener(final MultiChoiceAction... actions) {
    return new MultiChoiceModeListenerAdapter(compose(actions));
  }
}
