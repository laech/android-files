package l.files.ui.mode;

import junit.framework.TestCase;

public final class MultiChoiceModeActionAdapterTest extends TestCase {

  public void testOnCreateActionMode_returnsTrueToIndicateActionModeShouldBeCreatedByDefault() {
    assertTrue(create().onCreateActionMode(null, null));
  }

  public void testOnActionItemClicked_returnsFalseToIndicateActionIsNotHandledByDefault() {
    assertFalse(create().onActionItemClicked(null, null));
  }

  public void testOnPrepareActionMode_returnsFalseToIndicateNoLayoutChangeByDefault() {
    assertFalse(create().onPrepareActionMode(null, null));
  }

  public void testGetItemId_returns0ToIndicateNoItemIsCreatedByDefault() {
    assertEquals(0, create().getItemId());
  }

  private MultiChoiceModeActionAdapter create() {
    return new MultiChoiceModeActionAdapter();
  }
}
