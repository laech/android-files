package l.files.common.widget;

import static android.widget.AbsListView.MultiChoiceModeListener;

public final class MultiChoiceModes {
  private MultiChoiceModes() {}

  public static MultiChoiceMode of(MultiChoiceMode... actions) {
    return new CompositeMultiChoiceMode(actions);
  }

  public static MultiChoiceModeListener asListener(final MultiChoiceMode... actions) {
    return new MultiChoiceModeListenerAdapter(of(actions));
  }
}
