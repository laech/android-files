package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import junit.framework.TestCase;

import static org.fest.assertions.api.Assertions.assertThat;
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

  public void testOnCreateActionMode_isDelegated() {
    ActionMode mode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);
    given(action1.onCreateActionMode(mode, menu)).willReturn(true);
    given(action2.onCreateActionMode(mode, menu)).willReturn(true);

    delegate.onCreateActionMode(mode, menu);

    verify(action1).onCreateActionMode(mode, menu);
    verify(action2).onCreateActionMode(mode, menu);
  }

  public void testOnCreateActionMode_returnsFalseIfAnyActionSaysItShouldNotBeCreated() {
    given(action1.onCreateActionMode(null, null)).willReturn(true);
    given(action2.onCreateActionMode(null, null)).willReturn(false);
    assertThat(delegate.onCreateActionMode(null, null)).isFalse();
  }

  public void testOnCreateActionMode_stopsExecutingSubsequentActionsWhenAnAnyActionSayItShouldNotBeCreated() {
    given(action1.onCreateActionMode(null, null)).willReturn(false);
    delegate.onCreateActionMode(null, null);
    verifyZeroInteractions(action2);
  }

  public void testOnCreateActionMode_returnsFalseIfAllActionsSayItShouldNotBeCreated() {
    given(action1.onCreateActionMode(null, null)).willReturn(false);
    given(action2.onCreateActionMode(null, null)).willReturn(false);
    assertThat(delegate.onCreateActionMode(null, null)).isFalse();
  }

  public void testOnCreateActionMode_returnsTrueIfAllActionsSayItShouldBeCreated() {
    given(action1.onCreateActionMode(null, null)).willReturn(true);
    given(action2.onCreateActionMode(null, null)).willReturn(true);
    assertThat(delegate.onCreateActionMode(null, null)).isTrue();
  }

  public void testOnPrepareActionMode_isDelegated() {
    ActionMode mode = mock(ActionMode.class);
    Menu menu = mock(Menu.class);

    delegate.onPrepareActionMode(mode, menu);

    verify(action1).onPrepareActionMode(mode, menu);
    verify(action2).onPrepareActionMode(mode, menu);
  }

  public void testOnPrepareActionMode_returnsTrueIfAnyActionSaysUpdateIsNeeded() {
    given(action1.onPrepareActionMode(null, null)).willReturn(false);
    given(action2.onPrepareActionMode(null, null)).willReturn(true);
    assertThat(delegate.onPrepareActionMode(null, null)).isTrue();
  }

  public void testOnPrepareActionMode_returnsFalseIfAllActionsSayUpdateIsNotNeeded() {
    given(action1.onPrepareActionMode(null, null)).willReturn(false);
    given(action2.onPrepareActionMode(null, null)).willReturn(false);
    assertThat(delegate.onPrepareActionMode(null, null)).isFalse();
  }

  public void testOnPrepareActionMode_returnsTrueIfAllActionsSayUpdateIsNeeded() {
    given(action1.onPrepareActionMode(null, null)).willReturn(true);
    given(action2.onPrepareActionMode(null, null)).willReturn(true);
    assertThat(delegate.onPrepareActionMode(null, null)).isTrue();
  }

  public void testOnActionItemClicked_isDelegatedToActionWithSameItemId() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(2);
    given(action1.getItemId()).willReturn(1);
    given(action2.getItemId()).willReturn(2);

    delegate.onActionItemClicked(null, item);

    verify(action1, never()).onActionItemClicked(null, item);
    verify(action2).onActionItemClicked(null, item);
  }

  public void testOnActionItemClicked_returnsTrueIfHasActionWithSameItemIdToHandleEvent() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(2);
    given(action1.getItemId()).willReturn(1);
    given(action2.getItemId()).willReturn(2);
    assertTrue(delegate.onActionItemClicked(null, item));
  }

  public void testOnActionItemClicked_returnsFalseIfHasNoActionWithSameItemIdToHandleEvent() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(3);
    given(action1.getItemId()).willReturn(1);
    given(action2.getItemId()).willReturn(2);
    assertThat(delegate.onActionItemClicked(null, item)).isFalse();
  }

  public void testOnActionItemClicked_willNotBeDelegatedToActionsWithNoIdEvenIfMenuItemHasNoId() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(0);
    given(action1.getItemId()).willReturn(0);
    given(action2.getItemId()).willReturn(0);

    delegate.onActionItemClicked(null, item);

    verify(action1, never()).onActionItemClicked(null, item);
    verify(action2, never()).onActionItemClicked(null, item);
  }

  public void testOnDestroyActionMode_isDelegated() {
    ActionMode mode = mock(ActionMode.class);
    delegate.onDestroyActionMode(mode);
    verify(action1).onDestroyActionMode(mode);
    verify(action2).onDestroyActionMode(mode);
  }

  public void testOnItemCheckedStateChanged_isDelegated() {
    ActionMode mode = mock(ActionMode.class);
    delegate.onItemCheckedStateChanged(mode, 1, 2, true);
    verify(action1).onItemCheckedStateChanged(mode, 1, 2, true);
    verify(action2).onItemCheckedStateChanged(mode, 1, 2, true);
  }
}
