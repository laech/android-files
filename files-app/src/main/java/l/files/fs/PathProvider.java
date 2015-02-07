package l.files.fs;

import java.net.URI;

public interface PathProvider {

  /**
   * @throws IllegalArgumentException if the URI scheme cannot be handled
   */
  Path get(URI uri);

}
