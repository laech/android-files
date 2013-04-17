package l.files.event;

import l.files.app.HomeActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;


public final class EventBusTest
    extends ActivityInstrumentationTestCase2<HomeActivity> {

  private EventBus bus;

  public EventBusTest() {
    super(HomeActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    try {
      runTestOnUiThread(new Runnable() {
        @Override public void run() {
          bus = new EventBus();
        }
      });
    } catch (Throwable e) {
      throw new AssertionError(e);
    }
  }

  public void testCanNotInstantiateOnNonMainThread() {
    try {
      new EventBus();
      fail();
    } catch (IllegalStateException e) {
      // Pass
    }
  }

  public void testCanNotPostOnNonMainThread() {
    try {
      bus.post("");
      fail();
    } catch (IllegalStateException e) {
      // Pass
    }
  }

  public void testCanNotRegisterOnNonMainThread() {
    try {
      bus.register(Object.class, new EventHandler<Object>() {
        @Override public void handle(Object event) {}
      });
      fail();
    } catch (IllegalStateException e) {
      // Pass
    }
  }

  public void testCanNotUnregisterOnNonMainThread() {
    try {
      bus.unregister(new EventHandler<Object>() {
        @Override public void handle(Object event) {}
      });
      fail();
    } catch (IllegalStateException e) {
      // Pass
    }
  }

  @UiThreadTest public void testPostsEventToRegisteredListener() {
    final boolean[] called = {false};
    bus.register(Object.class, new EventHandler<Object>() {
      @Override public void handle(Object event) {
        called[0] = true;
      }
    });
    bus.post(new Object());
    assertTrue(called[0]);
  }

  @UiThreadTest public void testPostsNothingToUnregisteredListener() {
    EventHandler<Object> handler = new EventHandler<Object>() {
      @Override public void handle(Object event) {
        fail();
      }
    };
    bus.register(Object.class, handler);
    bus.unregister(handler);
    bus.post(new Object());
    assertTrue(bus.handlersByType.get(Object.class).isEmpty());
  }
}
