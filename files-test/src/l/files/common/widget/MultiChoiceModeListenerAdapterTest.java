package l.files.common.widget;

import l.files.test.BaseTest;

public final class MultiChoiceModeListenerAdapterTest extends BaseTest {

  private MultiChoiceModeListenerAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();
    adapter = new MultiChoiceModeListenerAdapter();
  }

  public void testOnCreateReturnsTrueToIndicateActionModeShouldBeCreated() {
    assertTrue(adapter.onCreateActionMode(null, null));
  }

  public void testOnPrepareReturnsFalseToIndicateMenuDoesNotNeedToBeUpdated() {
    assertFalse(adapter.onPrepareActionMode(null, null));
  }

  public void testOnClickReturnsFalseToIndicateActionIsNotHandled() {
    assertFalse(adapter.onActionItemClicked(null, null));
  }
}
