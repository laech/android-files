package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileItem;

import static java.lang.System.currentTimeMillis;

enum FileSort {

    NAME(R.string.name) {
        @Override
        Comparator<FileItem> comparator() {
            return new Comparator<FileItem>() {
                @Override
                public int compare(FileItem a, FileItem b) {
                    return a.compareTo(b);
                }
            };
        }

        @Override
        Categorizer categorizer() {
            return Categorizer.NULL;
        }

        @Override
        List<Object> sort(List<FileItem> items, Resources res) {
            List<FileItem> result = new ArrayList<>(items);
            Collections.sort(result);
            return Collections.<Object>unmodifiableList(result);
        }
    },

    MODIFIED(R.string.date_modified) {
        @Override
        Comparator<FileItem> comparator() {
            return new StatComparator() {
                @Override
                protected int compareNotNull(
                        FileItem a, Stat aStat,
                        FileItem b, Stat bStat) {
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
        Categorizer categorizer() {
            return new DateCategorizer(currentTimeMillis());
        }
    },

    SIZE(R.string.size) {
        @Override
        Comparator<FileItem> comparator() {
            return new StatComparator() {
                @Override
                protected int compareNotNull(
                        FileItem a, Stat aStat,
                        FileItem b, Stat bStat) {
                    if (aStat.isDirectory() && bStat.isDirectory()) {
                        return a.compareTo(b);
                    }

                    if (aStat.isDirectory()) return 1;
                    if (bStat.isDirectory()) return -1;

                    int result = compare(bStat.size(), aStat.size());
                    if (result == 0) {
                        return a.compareTo(b);
                    }
                    return result;
                }

                private int compare(long a, long b) {
                    return a < b ? -1 : (a == b ? 0 : 1);
                }

            };
        }

        @Override
        Categorizer categorizer() {
            return new SizeCategorizer();
        }
    };

    private int labelId;

    FileSort(int labelId) {
        this.labelId = labelId;
    }

    String getLabel(Resources res) {
        return res.getString(labelId);
    }

    abstract Comparator<FileItem> comparator();

    abstract Categorizer categorizer();

    List<Object> sort(List<FileItem> items, Resources res) {
        List<FileItem> sorted = new ArrayList<>(items);
        Collections.sort(sorted, comparator());
        return categorizer().categorize(res, sorted);
    }

    private static abstract class StatComparator implements Comparator<FileItem> {

        @Override
        public int compare(FileItem a, FileItem b) {
            if (a.selfStat() == null && b.selfStat() == null) return a.compareTo(b);
            if (a.selfStat() == null) return 1;
            if (b.selfStat() == null) return -1;

            return compareNotNull(
                    a, a.selfStat(),
                    b, b.selfStat());
        }

        protected abstract int compareNotNull(
                FileItem a, Stat aStat,
                FileItem b, Stat bStat
        );

    }

}
