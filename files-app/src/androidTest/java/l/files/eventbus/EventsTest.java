package l.files.eventbus;

import org.mockito.ArgumentCaptor;

import java.util.concurrent.Executor;

import de.greenrobot.event.EventBus;
import l.files.common.testing.BaseTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class EventsTest extends BaseTest {

  public void testFailFastOnError() throws Exception {
    testFailFast(new Error(), false);
  }

  public void testFailFastOnRuntimeException() throws Exception {
    testFailFast(new RuntimeException(), false);
  }

  public void testFailFastOnCheckedException() throws Exception {
    testFailFast(new Exception(), true);
  }

  public void testFailFast(Throwable expected, boolean checkCause) {
    Executor executor = mock(Executor.class);
    ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
    EventBus bus = Events.failFast(new EventBus(), executor);
    bus.register(new Object() {
      @Subscribe public void onEvent(Throwable e) throws Throwable {
        throw e;
      }
    });
    bus.post(expected);
    verify(executor).execute(captor.capture());
    try {
      captor.getValue().run();
      fail("Exception expected");
    } catch (Throwable actual) {
      if (checkCause) {
        assertEquals(expected, actual.getCause());
      } else {
        assertEquals(expected, actual);
      }
    }
  }
}
