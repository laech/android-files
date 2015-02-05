package l.files.ui;

import android.content.res.Resources;

import com.google.common.primitives.Longs;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import l.files.R;
import l.files.fs.FileStatus;

import static java.lang.System.currentTimeMillis;

public enum FileSort {

  NAME(R.string.name) {
    @Override public Comparator<FileListItem.File> newComparator(Locale locale) {
      final Collator collator = Collator.getInstance(locale);
      return new Comparator<FileListItem.File>() {
        @Override public int compare(FileListItem.File a, FileListItem.File b) {
          return collator.compare(a.getPath().getName(), b.getPath().getName());
        }
      };
    }

    @Override public Categorizer newCategorizer() {
      return Categorizer.NULL;
    }
  },

  MODIFIED(R.string.date_modified) {
    @Override public Comparator<FileListItem.File> newComparator(Locale locale) {
      return new NullableFileStatComparator(locale) {
        @Override public int compareNotNull(FileStatus a, FileStatus b) {
          int result = Longs.compare(b.lastModifiedTime(), a.lastModifiedTime());
          if (result == 0) {
            result = nameComparator.compare(a.name(), b.name());
          }
          return result;
        }
      };
    }

    @Override public Categorizer newCategorizer() {
      return new DateCategorizer(currentTimeMillis());
    }
  },

  SIZE(R.string.size) {
    @Override public Comparator<FileListItem.File> newComparator(final Locale locale) {
      return new NullableFileStatComparator(locale) {
        @Override public int compareNotNull(FileStatus a, FileStatus b) {
          if (a.isDirectory() && b.isDirectory()) {
            return nameComparator.compare(a.name(), b.name());
          }
          if (a.isDirectory()) {
            return 1;
          }
          if (b.isDirectory()) {
            return -1;
          }
          int compare = Longs.compare(b.size(), a.size());
          if (compare == 0) {
            return nameComparator.compare(a.name(), b.name());
          }
          return compare;
        }
      };
    }

    @Override public Categorizer newCategorizer() {
      return new SizeCategorizer();
    }
  };

  private final int labelId;

  FileSort(int labelId) {
    this.labelId = labelId;
  }

  public String getLabel(Resources res) {
    return res.getString(labelId);
  }

  public abstract Comparator<FileListItem.File> newComparator(Locale locale);

  public abstract Categorizer newCategorizer();

  private static abstract class NullableFileStatComparator implements Comparator<FileListItem.File> {

    protected final Collator nameComparator;

    NullableFileStatComparator(Locale locale) {
      nameComparator = Collator.getInstance(locale);
    }

    @Override public int compare(FileListItem.File a, FileListItem.File b) {
      if (a.getStat() == null && b.getStat() == null) {
        return nameComparator.compare(a.getPath().getName(), b.getPath().getName());
      }
      if (a.getStat() == null) {
        return 1;
      }
      if (b.getStat() == null) {
        return -1;
      }
      return compareNotNull(a.getStat(), b.getStat());
    }

    protected abstract int compareNotNull(FileStatus a, FileStatus b);
  }
}
