package l.files.setting;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import l.files.common.base.Value;

/**
 * This event will be fired when the bookmarks are being added/removed, and it
 * will also be fired on initial registration to the event bus for providing the
 * current bookmarks to the event handler.
 */
public final class BookmarksSetting extends Value<Set<File>> {

  public BookmarksSetting(File... bookmarks) {
    this(asList(bookmarks));
  }

  public BookmarksSetting(Collection<File> bookmarks) {
    super(ImmutableSet.copyOf(checkNotNull(bookmarks, "bookmarks")));
  }
}
