package l.files.fs;

import java.net.URI;

public interface PathProvider {

  Path get(URI uri);

}
