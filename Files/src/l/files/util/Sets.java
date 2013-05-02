package l.files.util;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class Sets {

  private Sets() {
  }

  public static <T> Set<T> difference(
      Collection<? extends T> xs, Collection<? extends T> ys) {
    Set<T> set = newHashSet(xs);
    set.removeAll(ys);
    return set;
  }
}
