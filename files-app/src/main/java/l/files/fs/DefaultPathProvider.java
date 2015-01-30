package l.files.fs;

import java.io.File;
import java.net.URI;

import l.files.fs.local.LocalPath;

public final class DefaultPathProvider implements PathProvider {

  @Override public Path get(URI uri) {
    return LocalPath.of(new File(uri));
  }

}
