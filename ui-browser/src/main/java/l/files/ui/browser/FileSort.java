package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.base.Integers;
import l.files.base.Longs;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;

import static java.lang.System.currentTimeMillis;

enum FileSort {

    NAME(R.string.name) {
        @Override
        Comparator<FileInfo> comparator() {
            return new Comparator<FileInfo>() {
                @Override
                public int compare(FileInfo a, FileInfo b) {
                    return a.compareTo(b);
                }
            };
        }

        @Override
        Categorizer categorizer() {
            return Categorizer.NULL;
        }

        @Override
        List<Object> sort(List<FileInfo> items, Resources res) {
            List<FileInfo> result = new ArrayList<>(items);
            Collections.sort(result);
            return Collections.<Object>unmodifiableList(result);
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

    abstract Comparator<FileInfo> comparator();

    abstract Categorizer categorizer();

    List<Object> sort(List<FileInfo> items, Resources res) {
        List<FileInfo> sorted = new ArrayList<>(items);
        Collections.sort(sorted, comparator());
        return categorizer().categorize(res, sorted);
    }

    private static abstract class StatComparator implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo a, FileInfo b) {
            if (a.selfStat() == null && b.selfStat() == null) return a.compareTo(b);
            if (a.selfStat() == null) return 1;
            if (b.selfStat() == null) return -1;

            return compareNotNull(
                    a, a.selfStat(),
                    b, b.selfStat());
        }

        protected abstract int compareNotNull(
                FileInfo a, Stat aStat,
                FileInfo b, Stat bStat
        );

    }

}
