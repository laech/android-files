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
    @Override public Comparator<FileStatus> newComparator(Locale locale) {
      final Collator collator = Collator.getInstance(locale);
      return new Comparator<FileStatus>() {
        @Override public int compare(FileStatus a, FileStatus b) {
          return collator.compare(a.name(), b.name());
        }
      };
    }

    @Override public Categorizer newCategorizer() {
      return Categorizer.NULL;
    }
  },

  MODIFIED(R.string.date_modified) {
    @Override public Comparator<FileStatus> newComparator(Locale locale) {
      final Comparator<FileStatus> nameComparator = NAME.newComparator(locale);
      return new Comparator<FileStatus>() {
        @Override public int compare(FileStatus a, FileStatus b) {
          int result = Longs.compare(b.lastModifiedTime(), a.lastModifiedTime());
          if (result == 0) {
            result = nameComparator.compare(a, b);
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
    @Override public Comparator<FileStatus> newComparator(final Locale locale) {
      final Comparator<FileStatus> nameComparator = NAME.newComparator(locale);
      return new Comparator<FileStatus>() {
        @Override public int compare(FileStatus a, FileStatus b) {
          if (a.isDirectory() && a.isDirectory() == b.isDirectory()) {
            return nameComparator.compare(a, b);
          }
          if (a.isDirectory()) {
            return 1;
          }
          if (b.isDirectory()) {
            return -1;
          }
          int compare = Longs.compare(b.size(), a.size());
          if (compare == 0) {
            return nameComparator.compare(a, b);
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

  public abstract Comparator<FileStatus> newComparator(Locale locale);

  public abstract Categorizer newCategorizer();
}
