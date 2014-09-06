package l.files.operations;

import android.os.Handler;
import android.os.Message;

import org.mockito.ArgumentCaptor;

import de.greenrobot.event.SubscriberExceptionEvent;
import l.files.common.testing.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class EventsTest extends BaseTest {

  public void testCrashesAppOnErrorEvent() {
    Handler handler = mock(Handler.class);
    Throwable error = mock(Throwable.class);

    new Events.Crasher(handler).onEvent(
        new SubscriberExceptionEvent(null, error, null, null));

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(handler).sendMessageDelayed(captor.capture(), anyLong());
    try {
      captor.getValue().getCallback().run();
      fail();
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isSameAs(error);
    }
  }
}
