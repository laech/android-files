package l.files.operations;

import l.files.common.testing.BaseTest;

import static com.google.common.truth.Truth.ASSERT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class TimeTest extends BaseTest {

  public void testCreate() throws Exception {
    Time time = Time.create(1, 2);
    ASSERT.that(time.time()).is(1);
    ASSERT.that(time.tick()).is(2);
  }

  public void testCreateFromClock() throws Exception {
    Clock clock = mock(Clock.class);
    given(clock.time()).willReturn(1L);
    given(clock.tick()).willReturn(2L);
    Time time = Time.create(clock);
    ASSERT.that(time.time()).is(1);
    ASSERT.that(time.tick()).is(2);
  }

}
