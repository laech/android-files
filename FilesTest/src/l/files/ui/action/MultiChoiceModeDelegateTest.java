package l.files.ui.action;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import junit.framework.TestCase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public final class MultiChoiceModeDelegateTest extends TestCase {

  private MultiChoiceModeDelegate delegate;

  private MultiChoiceModeAction action1;
  private MultiChoiceModeAction action2;

  @Override protected void setUp() throws Exception {
    super.setUp();
    action1 = mock(MultiChoiceModeAction.class);
    action2 = mock(MultiChoiceModeAction.class);
    delegate = new MultiChoiceModeDelegate(action1, action2);
  }

  public void testOnCreateActionModeIsDelegated() {
    ActionMode mode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);

    delegate.onCreateActionMode(mode, menu);

    verify(action1).onCreateActionMode(mode, menu);
    verify(action2).onCreateActionMode(mode, menu);
  }

  public void testOnCreateActionModeReturnsFalseIfAnyActionSaysItShouldNotBeCreated() {
    given(action1.onCreateActionMode(null, null)).willReturn(true);
    given(action2.onCreateActionMode(null, null)).willReturn(false);
    assertFalse(delegate.onCreateActionMode(null, null));
  }

  public void testOnCreateActionModeReturnsFalseIfAllActionsSayItShouldNotBeCreated() {
    given(action1.onCreateActionMode(null, null)).willReturn(false);
    given(action2.onCreateActionMode(null, null)).willReturn(false);
    assertFalse(delegate.onCreateActionMode(null, null));
  }

  public void testOnCreateActionModeReturnsTrueIfAllActionsSayItShouldBeCreated() {
    given(action1.onCreateActionMode(null, null)).willReturn(true);
    given(action2.onCreateActionMode(null, null)).willReturn(true);
    assertTrue(delegate.onCreateActionMode(null, null));
  }

  public void testOnPrepareActionModeIsDelegated() {
    ActionMode mode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);

    delegate.onPrepareActionMode(mode, menu);

    verify(action1).onPrepareActionMode(mode, menu);
    verify(action2).onPrepareActionMode(mode, menu);
  }

  public void testOnPrepareActionModeReturnsTrueIfAnyActionSaysUpdateIsNeeded() {
    given(action1.onPrepareActionMode(null, null)).willReturn(false);
    given(action2.onPrepareActionMode(null, null)).willReturn(true);
    assertTrue(delegate.onPrepareActionMode(null, null));
  }

  public void testOnPrepareActionModeReturnsFalseIfAllActionsSayUpdateIsNotNeeded() {
    given(action1.onPrepareActionMode(null, null)).willReturn(false);
    given(action2.onPrepareActionMode(null, null)).willReturn(false);
    assertFalse(delegate.onPrepareActionMode(null, null));
  }

  public void testOnPrepareActionModeReturnsTrueIfAllActionsSayUpdateIsNeeded() {
    given(action1.onPrepareActionMode(null, null)).willReturn(true);
    given(action2.onPrepareActionMode(null, null)).willReturn(true);
    assertTrue(delegate.onPrepareActionMode(null, null));
  }

  public void testOnActionItemClickedIsDelegatedToActionWithSameItemId() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(2);
    given(action1.getItemId()).willReturn(1);
    given(action2.getItemId()).willReturn(2);

    delegate.onActionItemClicked(null, item);

    verify(action1, never()).onActionItemClicked(null, item);
    verify(action2).onActionItemClicked(null, item);
  }

  public void testOnActionItemClickedReturnsTrueIfHasActionWithSameItemIdToHandleEvent() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(2);
    given(action1.getItemId()).willReturn(1);
    given(action2.getItemId()).willReturn(2);
    assertTrue(delegate.onActionItemClicked(null, item));
  }

  public void testOnActionItemClickedReturnsFalseIfHasNoActionWithSameItemIdToHandleEvent() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(3);
    given(action1.getItemId()).willReturn(1);
    given(action2.getItemId()).willReturn(2);
    assertFalse(delegate.onActionItemClicked(null, item));
  }

  public void testOnActionItemClickedWillNotBeDelegatedToActionsWithNoIdEvenIfMenuItemHasNoId() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(0);
    given(action1.getItemId()).willReturn(0);
    given(action2.getItemId()).willReturn(0);

    delegate.onActionItemClicked(null, item);

    verify(action1, never()).onActionItemClicked(null, item);
    verify(action2, never()).onActionItemClicked(null, item);
  }

  public void testOnDestroyActionModeIsDelegated() {
    ActionMode mode = mock(ActionMode.class);
    delegate.onDestroyActionMode(mode);
    verify(action1).onDestroyActionMode(mode);
    verify(action2).onDestroyActionMode(mode);
  }

  public void testOnItemCheckedStateChangedIsDelegated() {
    ActionMode mode = mock(ActionMode.class);
    delegate.onItemCheckedStateChanged(mode, 1, 2, true);
    verify(action1).onItemCheckedStateChanged(mode, 1, 2, true);
    verify(action2).onItemCheckedStateChanged(mode, 1, 2, true);
  }
}
