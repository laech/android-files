package l.files.event;

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
public abstract class Clipboard extends Value<Set<File>> {

  public static final class Cut extends Clipboard {
    public Cut(File... files) {
      super(files);
    }

    @Override public PasteRequest.Cut paste(File dir) {
      return new PasteRequest.Cut(value(), dir);
    }
  }

  public static final class Copy extends Clipboard {
    public Copy(File... files) {
      super(files);
    }

    @Override public PasteRequest.Copy paste(File dir) {
      return new PasteRequest.Copy(value(), dir);
    }
  }

  private Clipboard(File... files) {
    super(ImmutableSet.copyOf(checkNotNull(files, "files")));
    checkArgument(!value().isEmpty());
  }

  public abstract PasteRequest paste(File dir);
}
