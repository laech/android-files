package l.files.setting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;
import l.files.common.base.Value;

/**
 * An instance will be provided on initial registration to the event bus for
 * providing the current clipboard to the event handler, if there is file
 * content in the clipboard.
 */
public class Clipboard extends Value<Set<File>> {

  private static final class Cut extends Clipboard {
    private Cut(File... files) {
      super(files);
    }
  }

  private static final class Copy extends Clipboard {
    private Copy(File... files) {
      super(files);
    }
  }

  public static Clipboard cut(File... files) {
    return new Cut(files);
  }

  public static Clipboard copy(File... files) {
    return new Copy(files);
  }

  private Clipboard(File... files) {
    super(ImmutableSet.copyOf(checkNotNull(files, "files")));
    checkArgument(!value().isEmpty());
  }

  public boolean isCut() {
    return this instanceof Cut;
  }

  public boolean isCopy() {
    return this instanceof Copy;
  }
}
