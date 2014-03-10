package l.files.provider;

import l.files.common.testing.BaseTest;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StopSelfListenerTest extends BaseTest {

  private DirWatcher observer;
  private StopSelfListener listener;

  @Override protected void setUp() throws Exception {
    super.setUp();
    observer = mock(DirWatcher.class);
    listener = new StopSelfListener(observer, new EmptyCallback());
    observer.addListeners(singleton(listener));
  }

  public void testOnDeleteSelf() throws Exception {
    listener.onDeleteSelf(null);
    verify(observer).stopWatching();
  }

  // Disabled for now, see StopSelfListener.onMovedSelf
  public void _testOnMoveSelf() throws Exception {
    listener.onMoveSelf(null);
    verify(observer).stopWatching();
  }

  static class EmptyCallback implements StopSelfListener.Callback {
    @Override public void onObserverStopped(DirWatcher observer) {}
  }
}
