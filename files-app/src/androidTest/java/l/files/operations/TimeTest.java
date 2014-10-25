package l.files.operations;

import l.files.common.testing.BaseTest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class TimeTest extends BaseTest {

  public void testCreate() throws Exception {
    Time time = Time.create(1, 2);
    assertEquals(1, time.time());
    assertEquals(2, time.tick());
  }

  public void testCreateFromClock() throws Exception {
    Clock clock = mock(Clock.class);
    given(clock.time()).willReturn(1L);
    given(clock.tick()).willReturn(2L);
    Time time = Time.create(clock);
    assertEquals(1, time.time());
    assertEquals(2, time.tick());
  }

}