package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Stat;
import l.files.ui.R;
import l.files.ui.browser.BrowserItem.FileItem;

import static java.lang.System.currentTimeMillis;

public enum FileSort {

    NAME(R.string.name) {
        @Override
        public Comparator<FileItem> comparator() {
            return new Comparator<FileItem>() {
                @Override
                public int compare(FileItem a, FileItem b) {
                    return a.compareTo(b);
                }
            };
        }

        @Override
        public Categorizer categorizer() {
            return Categorizer.NULL;
        }

        @Override
        public List<BrowserItem> sort(List<FileItem> items, Resources res) {
            List<FileItem> result = new ArrayList<>(items);
            Collections.sort(result);
            return Collections.<BrowserItem>unmodifiableList(result);
        }
    },

    MODIFIED(R.string.date_modified) {
        @Override
        public Comparator<FileItem> comparator() {
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
        public Categorizer categorizer() {
            return new DateCategorizer(currentTimeMillis());
        }
    },

    SIZE(R.string.size) {
        @Override
        public Comparator<FileItem> comparator() {
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

    public abstract Comparator<FileItem> comparator();

    public abstract Categorizer categorizer();

    public List<BrowserItem> sort(
            List<FileItem> items, Resources res) {
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
                FileItem b, Stat bStat);
    }
}
