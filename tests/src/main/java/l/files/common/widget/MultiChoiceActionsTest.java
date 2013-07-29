package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import junit.framework.TestCase;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public final class MultiChoiceActionsTest extends TestCase {

  public void testCompose() {
    new CompositeTester().test();
  }

  public void testAsListener() {
    new AsListenerTester().test();
  }

  private static class CompositeTester {
    private final MultiChoiceAction action1;
    private final MultiChoiceAction action2;
    private final MultiChoiceAction composite;

    CompositeTester() {
      action1 = mock(MultiChoiceAction.class);
      action2 = mock(MultiChoiceAction.class);
      composite = MultiChoiceActions.compose(action1, action2);
    }

    void test() {
      testOnCreateIsDelegated();
      testOnChangeIsDelegated();
    }

    public void testOnCreateIsDelegated() {
      ActionMode mode = mock(ActionMode.class);
      Menu menu = mock(Menu.class);
      composite.onCreate(mode, menu);
      verify(action1).onCreate(mode, menu);
      verify(action2).onCreate(mode, menu);
    }

    public void testOnChangeIsDelegated() {
      ActionMode mode = mock(ActionMode.class);
      composite.onChange(mode, 0, 1, true);
      verify(action1).onChange(mode, 0, 1, true);
      verify(action2).onChange(mode, 0, 1, true);
    }
  }

  public final class AsListenerTester {
    private final MultiChoiceAction mode;
    private final MultiChoiceModeListener adapter;

    AsListenerTester() {
      mode = mock(MultiChoiceAction.class);
      adapter = MultiChoiceActions.asListener(mode);
    }

    void test() {
      testOnCreateActionModeIsDelegated();
      testOnPrepareActionModeReturnsFalseToIndicateNoNeedToUpdate();
      testOnItemCheckedStateChangedIsDelegated();
    }

    private void testOnCreateActionModeIsDelegated() {
      ActionMode actionMode = mock(ActionMode.class);
      Menu menu = mock(Menu.class);
      assertThat(adapter.onCreateActionMode(actionMode, menu)).isTrue();
      verify(mode).onCreate(actionMode, menu);
    }

    private void testOnItemCheckedStateChangedIsDelegated() {
      ActionMode actionMode = mock(ActionMode.class);
      adapter.onItemCheckedStateChanged(actionMode, 0, 1, true);
      verify(mode).onChange(actionMode, 0, 1, true);
    }

    private void testOnPrepareActionModeReturnsFalseToIndicateNoNeedToUpdate() {
      assertThat(adapter.onPrepareActionMode(null, null)).isFalse();
      verifyZeroInteractions(mode);
    }
  }
}
