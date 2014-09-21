package l.files.app.category;

import android.content.res.Resources;
import android.database.Cursor;

import l.files.R;
import l.files.provider.FilesContract;

import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.isDirectory;

/**
 * Categorizes by file size (descending order).
 *
 * @see FilesContract.Files#SORT_BY_SIZE
 */
final class FileSizeCategorizer implements Categorizer {

  private static final long ZERO = 0;
  private static final long KB_1 = 1024;
  private static final long MB_1 = KB_1 * 1024;
  private static final long MB_100 = MB_1 * 100;
  private static final long GB_1 = MB_1 * 1024;
  private static final long GB_10 = GB_1 * 10;

  private static final Group[] GROUPS = {
      new Group(GB_10, R.string._10gb_or_more),
      new Group(GB_1, R.string._1gb_to_10gb),
      new Group(MB_100, R.string._100mb_to_1gb),
      new Group(MB_1, R.string._1mb_to_100mb),
      new Group(KB_1, R.string._1kb_to_1mb),
      new Group(ZERO, R.string.less_than_1kb),
  };

  @Override public String getCategory(Resources res, Cursor cursor) {
    if (isDirectory(cursor)) {
      return res.getString(R.string.__);
    }
    long size = getSize(cursor);
    for (Group group : GROUPS) {
      if (size >= group.minSize) {
        return res.getString(group.label);
      }
    }
    return null;
  }

  private static class Group {
    final long minSize;
    final int label;

    Group(long minSize, int label) {
      this.minSize = minSize;
      this.label = label;
    }
  }
}
