package l.files.ui.mode;

import static android.widget.AbsListView.MultiChoiceModeListener;

public final class MultiChoiceModes {

  public static MultiChoiceMode of(MultiChoiceMode... actions) {
    return new CompositeMultiChoiceMode(actions);
  }

  public static MultiChoiceModeListener asListener(final MultiChoiceMode... actions) {
    return new MultiChoiceModeListenerAdapter(of(actions));
  }

  private MultiChoiceModes() {}

}
