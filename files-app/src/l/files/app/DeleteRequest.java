package l.files.app;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;
import l.files.common.base.Value;

public final class DeleteRequest extends Value<Set<File>> {

  public DeleteRequest(File... files) {
    super(ImmutableSet.copyOf(checkNotNull(files, "files")));
  }
}
