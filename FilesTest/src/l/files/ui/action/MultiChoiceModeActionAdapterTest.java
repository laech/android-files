package l.files.ui.action;

import junit.framework.TestCase;

public final class MultiChoiceModeActionAdapterTest extends TestCase {

  public void testOnCreateActionModeReturnsTrueToIndicateActionModeShouldBeCreatedByDefault() {
    assertTrue(create().onCreateActionMode(null, null));
  }

  public void testOnActionItemClickedReturnsFalseToIndicateActionIsNotHandledByDefault() {
    assertFalse(create().onActionItemClicked(null, null));
  }

  public void testOnPrepareActionModeReturnsFalseToIndicateNoLayoutChangeByDefault() {
    assertFalse(create().onPrepareActionMode(null, null));
  }

  public void testGetItemIdReturns0ToIndicateNoItemIsCreatedByDefault() {
    assertEquals(0, create().getItemId());
  }

  private MultiChoiceModeActionAdapter create() {
    return new MultiChoiceModeActionAdapter();
  }
}
