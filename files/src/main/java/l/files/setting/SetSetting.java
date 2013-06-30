package l.files.setting;

import java.util.Set;

public interface SetSetting<E> extends Setting<Set<E>> {

  void add(E item);

  void remove(E item);

  boolean contains(E item);
  
}
