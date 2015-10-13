package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.ui.R;
import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.ui.browser.FileListItem.File;

import static java.lang.System.currentTimeMillis;

public enum FileSort {

    NAME(R.string.name) {
        @Override
        public Comparator<File> comparator() {
            return new Comparator<File>() {
                @Override
                public int compare(File a, File b) {
                    return a.compareTo(b);
                }
            };
        }

        @Override
        public Categorizer categorizer() {
            return Categorizer.NULL;
        }

        @Override
        public List<FileListItem> sort(
                List<File> items, Resources res) {
            List<File> result = new ArrayList<>(items);
            Collections.sort(result);
            return Collections.<FileListItem>unmodifiableList(result);
        }
    },

    MODIFIED(R.string.date_modified) {
        @Override
        public Comparator<File> comparator() {
            return new StatComparator() {
                @Override
                protected int compareNotNull(
                        File a, Stat aStat,
                        File b, Stat bStat) {
                    Instant aTime = aStat.lastModifiedTime();
                    Instant bTime = bStat.lastModifiedTime();
                    int result = bTime.compareTo(aTime);
                    if (result == 0) {
                        return a.compareTo(b);
                    }
                    return result;
                }
            };
        }

        @Override
        public Categorizer categorizer() {
            return new DateCategorizer(currentTimeMillis());
        }
    },

    SIZE(R.string.size) {
        @Override
        public Comparator<File> comparator() {
            return new StatComparator() {
                @Override
                protected int compareNotNull(
                        File a, Stat aStat,
                        File b, Stat bStat) {
                    if (aStat.isDirectory() && bStat.isDirectory()) {
                        return a.compareTo(b);
                    }

                    if (aStat.isDirectory()) return 1;
                    if (bStat.isDirectory()) return -1;

                    int result = Long.compare(bStat.size(), aStat.size());
                    if (result == 0) {
                        return a.compareTo(b);
                    }
                    return result;
                }
            };
        }

        @Override
        public Categorizer categorizer() {
            return new SizeCategorizer();
        }
    };

    private int labelId;

    FileSort(int labelId) {
        this.labelId = labelId;
    }

    public String getLabel(Resources res) {
        return res.getString(labelId);
    }

    public abstract Comparator<File> comparator();

    public abstract Categorizer categorizer();

    public List<FileListItem> sort(
            List<File> items, Resources res) {
        List<File> sorted = new ArrayList<>(items);
        Collections.sort(sorted, comparator());
        return categorizer().categorize(res, sorted);
    }

    private static abstract class StatComparator implements Comparator<File> {
        @Override
        public int compare(File a, File b) {
            if (a.stat() == null && b.stat() == null) return a.compareTo(b);
            if (a.stat() == null) return 1;
            if (b.stat() == null) return -1;

            return compareNotNull(
                    a, a.stat(),
                    b, b.stat());
        }

        protected abstract int compareNotNull(
                File a, Stat aStat,
                File b, Stat bStat);
    }
}
