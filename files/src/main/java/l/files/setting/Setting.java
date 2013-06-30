package l.files.setting;

public interface Setting<T> {

  T get();

  void set(T value);

  String key();

}
