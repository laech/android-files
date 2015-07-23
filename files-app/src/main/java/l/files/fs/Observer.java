package l.files.fs;

import android.support.annotation.Nullable;

public interface Observer {

  /**
   * @param child if null the event is for the observed resource itself, if not
   *              null the event is for the child of the observed resource with
   *              that this name
   */
  void onEvent(Event event, @Nullable String child);

}
