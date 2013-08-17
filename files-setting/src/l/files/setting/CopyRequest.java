package l.files.setting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;
import l.files.common.base.Value;

public final class CopyRequest extends Value<Set<File>> {

  public CopyRequest(File... files) {
    this(asList(files));
  }

  public CopyRequest(Iterable<File> files) {
    super(ImmutableSet.copyOf(checkNotNull(files, "files")));
    checkArgument(!value().isEmpty());
  }
}
