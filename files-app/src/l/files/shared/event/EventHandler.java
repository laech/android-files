package l.files.shared.event;

public interface EventHandler<T> {

  void handle(T event);

}
