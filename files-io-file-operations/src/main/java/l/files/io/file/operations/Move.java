package l.files.io.file.operations;

import java.io.IOException;
import java.util.Collection;

import static l.files.io.file.Files.rename;

public final class Move extends Paste {

  public Move(Iterable<String> sources, String dstDir) {
    super(sources, dstDir);
  }

  @Override
  protected void paste(String from, String to, Collection<Failure> failures) {
    try {
      rename(from, to);
    } catch (IOException e) {
      failures.add(Failure.create(from, e));
    }
  }
}
