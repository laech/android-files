package l.files.event;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;
import l.files.common.base.ValueObject;

public abstract class PasteRequest extends ValueObject {

  public static final class Cut extends PasteRequest {
    public Cut(Set<File> files, File destination) {
      super(files, destination);
    }
  }

  public static final class Copy extends PasteRequest {
    public Copy(Set<File> files, File destination) {
      super(files, destination);
    }
  }

  private final Set<File> files;
  private final File destination;

  private PasteRequest(Set<File> files, File destination) {
    this.files = ImmutableSet.copyOf(checkNotNull(files, "files"));
    this.destination = checkNotNull(destination, "destination");
  }

  public Set<File> files() {
    return files;
  }

  public File destination() {
    return destination;
  }
}
