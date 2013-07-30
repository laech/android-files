package l.files.event;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * This event will be fired when the bookmarks are being added/removed, and it
 * will also be fired on initial registration to the event bus for providing the
 * current bookmarks to the event handler.
 */
public final class BookmarksEvent {

  private final Set<File> bookmarks;

  public BookmarksEvent(File... bookmarks) {
    this(asList(checkNotNull(bookmarks, "bookmarks")));
  }

  public BookmarksEvent(Iterable<File> bookmarks) {
    this.bookmarks = ImmutableSet.copyOf(checkNotNull(bookmarks, "bookmarks"));
  }

  public Set<File> bookmarks() {
    return bookmarks;
  }

  @Override public int hashCode() {
    return bookmarks().hashCode();
  }

  @Override public boolean equals(Object o) {
    return o instanceof BookmarksEvent
        && ((BookmarksEvent) o).bookmarks().equals(bookmarks());
  }

  @Override public String toString() {
    return Objects.toStringHelper(this).addValue(bookmarks()).toString();
  }

}
