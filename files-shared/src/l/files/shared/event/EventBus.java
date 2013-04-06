package l.files.shared.event;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Map;
import java.util.Set;

public class EventBus {

  final Map<Class<?>, Set<EventHandler<Object>>> handlersByType;

  public EventBus() {
    ensureMainThread();
    handlersByType = newHashMap();
  }

  private void ensureMainThread() {
    if (myLooper() != getMainLooper()) throw new IllegalStateException();
  }

  public void post(Object event) {
    ensureMainThread();
    Set<EventHandler<Object>> handlers = handlersByType.get(event.getClass());
    if (handlers != null)
      for (EventHandler<Object> handler : handlers)
        handler.handle(event);
  }

  @SuppressWarnings("unchecked")
  public <T> void register(Class<T> eventType, EventHandler<T> handler) {
    ensureMainThread();
    Set<EventHandler<Object>> handlers = handlersByType.get(eventType);
    if (handlers == null) {
      handlers = newHashSet();
      handlersByType.put(eventType, handlers);
    }
    handlers.add((EventHandler<Object>) handler);
  }

  public void unregister(EventHandler<?> handler) {
    ensureMainThread();
    for (Set<EventHandler<Object>> handlers : handlersByType.values())
      handlers.remove(handler);
  }
}
