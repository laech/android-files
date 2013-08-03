package l.files.setting;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Set;

/**
 * This event will be fired when the bookmarks are being added/removed, and it
 * will also be fired on initial registration to the event bus for providing the
 * current bookmarks to the event handler.
 */
public final class BookmarksSetting extends Value<Set<File>> {

  public BookmarksSetting(File... bookmarks) {
    super(ImmutableSet.copyOf(bookmarks));
  }

  public BookmarksSetting(Iterable<File> bookmarks) {
    super(ImmutableSet.copyOf(bookmarks));
  }
}
