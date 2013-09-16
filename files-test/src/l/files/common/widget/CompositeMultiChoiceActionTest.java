package l.files.common.widget;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import l.files.test.BaseTest;

public final class CompositeMultiChoiceActionTest extends BaseTest {

  private ActionMode mode;
  private Menu menu;
  private MenuItem item;
  private MultiChoiceModeListener action1;
  private MultiChoiceModeListener action2;
  private CompositeMultiChoiceAction composite;

  @Override protected void setUp() throws Exception {
    super.setUp();
    mode = mock(ActionMode.class);
    menu = mock(Menu.class);
    item = mock(MenuItem.class);
    action1 = mock(MultiChoiceModeListener.class);
    action2 = mock(MultiChoiceModeListener.class);
    composite = new CompositeMultiChoiceAction(action1, action2);
  }

  public void testOnCreateIsDelegated() {
    composite.onCreateActionMode(mode, menu);
    verify(action1).onCreateActionMode(mode, menu);
    verify(action2).onCreateActionMode(mode, menu);
  }

  public void testOnCreateShouldReturnFalseIfAnyActionReturnsFalseToIndicateActionModeShouldNotBeCreated() {
    given(action1.onCreateActionMode(mode, menu)).willReturn(true);
    given(action2.onCreateActionMode(mode, menu)).willReturn(false);
    assertFalse(composite.onCreateActionMode(mode, menu));
  }

  public void testOnCreateReturnsTrueIfAllActionsReturnTrueToIndicateActionModeShouldBeCreated() {
    given(action1.onCreateActionMode(mode, menu)).willReturn(true);
    given(action2.onCreateActionMode(mode, menu)).willReturn(true);
    assertTrue(composite.onCreateActionMode(mode, menu));
  }

  public void testOnPrepareIsDelegated() {
    composite.onPrepareActionMode(mode, menu);
    verify(action1).onPrepareActionMode(mode, menu);
    verify(action2).onPrepareActionMode(mode, menu);
  }

  public void testOnPrepareReturnsTrueIfAllActionsReturnTrue() {
    given(action1.onPrepareActionMode(mode, menu)).willReturn(true);
    given(action2.onPrepareActionMode(mode, menu)).willReturn(true);
    assertTrue(composite.onPrepareActionMode(mode, menu));
  }

  public void testOnPrepareReturnsFalseIfAllActionsReturnFalse() {
    given(action1.onPrepareActionMode(mode, menu)).willReturn(false);
    given(action2.onPrepareActionMode(mode, menu)).willReturn(false);
    assertFalse(composite.onPrepareActionMode(mode, menu));
  }

  public void testOnPrepareReturnsTrueIfAnyActionReturnsTrueToIndicateMenuShouldBeUpdated() {
    given(action1.onPrepareActionMode(mode, menu)).willReturn(false);
    given(action2.onPrepareActionMode(mode, menu)).willReturn(true);
    assertTrue(composite.onPrepareActionMode(mode, menu));
    verify(action1).onPrepareActionMode(mode, menu);
    verify(action2).onPrepareActionMode(mode, menu);
  }

  public void testOnClickIsDelegated() {
    given(action1.onActionItemClicked(mode, item)).willReturn(false);
    given(action2.onActionItemClicked(mode, item)).willReturn(false);
    assertFalse(composite.onActionItemClicked(mode, item));
    verify(action1).onActionItemClicked(mode, item);
    verify(action2).onActionItemClicked(mode, item);
  }

  public void testOnClickStopsExecutingIfHandled() {
    given(action1.onActionItemClicked(mode, item)).willReturn(true);
    given(action2.onActionItemClicked(mode, item)).willReturn(false);
    assertTrue(composite.onActionItemClicked(mode, item));
    verify(action1).onActionItemClicked(mode, item);
    verifyZeroInteractions(action2);
  }

  public void testOnChangeIsDelegated() {
    composite.onItemCheckedStateChanged(mode, 0, 1, true);
    verify(action1).onItemCheckedStateChanged(mode, 0, 1, true);
    verify(action2).onItemCheckedStateChanged(mode, 0, 1, true);
  }

  public void testOnDestroyIsDelegated() {
    composite.onDestroyActionMode(mode);
    verify(action1).onDestroyActionMode(mode);
    verify(action2).onDestroyActionMode(mode);
  }
}
