package l.files.ui.browser.sort;

import android.content.res.Resources;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.base.Integers;
import l.files.base.Longs;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.browser.R;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableList;

public enum FileSort {

    NAME(R.string.name) {
        @Override
        Comparator<FileInfo> comparator() {
            return FileInfo::compareTo;
        }

        @Override
        Categorizer categorizer() {
            return NullCategorizer.INSTANCE;
        }

        @Override
        public List<Object> sort(List<FileInfo> items, Resources res) {
            Collections.sort(items);
            return unmodifiableList(items);
        }
    },

    MODIFIED(R.string.date_modified) {
        @Override
        Comparator<FileInfo> comparator() {
            return new StatComparator() {
                @Override
                protected int compareNotNull(
                        FileInfo a, Stat aStat,
                        FileInfo b, Stat bStat) {

                    int result = Longs.compare(
                            bStat.lastModifiedEpochSecond(),
                            aStat.lastModifiedEpochSecond());

                    if (result == 0) {
                        result = Integers.compare(
                                bStat.lastModifiedNanoOfSecond(),
                                aStat.lastModifiedNanoOfSecond());
                    }

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
        Comparator<FileInfo> comparator() {
            return new StatComparator() {
                @Override
                protected int compareNotNull(
                        FileInfo a, Stat aStat,
                        FileInfo b, Stat bStat) {
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
            return SizeCategorizer.INSTANCE;
        }
    };

    private final int labelId;

    FileSort(int labelId) {
        this.labelId = labelId;
    }

    public String getLabel(Resources res) {
        return res.getString(labelId);
    }

    abstract Comparator<FileInfo> comparator();

    abstract Categorizer categorizer();

    public List<Object> sort(List<FileInfo> items, Resources res) {
        Collections.sort(items, comparator());
        return categorizer().categorize(res, items);
    }

    private static abstract class StatComparator implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo a, FileInfo b) {
            Stat aStat = a.selfStat();
            Stat bStat = b.selfStat();
            if (aStat == null && bStat == null) return a.compareTo(b);
            if (aStat == null) return 1;
            if (bStat == null) return -1;

            return compareNotNull(
                    a, aStat,
                    b, bStat);
        }

        protected abstract int compareNotNull(
                FileInfo a, Stat aStat,
                FileInfo b, Stat bStat
        );

    }

}
