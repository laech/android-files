package l.files.ui.browser.sort;

import android.content.res.Resources;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.browser.R;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            return new AttrsComparator() {
                @Override
                protected int compareNotNull(
                    FileInfo a, BasicFileAttributes aAttrs,
                    FileInfo b, BasicFileAttributes bAttrs
                ) {
                    int result = bAttrs.lastModifiedTime()
                        .compareTo(aAttrs.lastModifiedTime());
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
            return new AttrsComparator() {
                @Override
                protected int compareNotNull(
                    FileInfo a, BasicFileAttributes aAttrs,
                    FileInfo b, BasicFileAttributes bAttrs
                ) {
                    if (aAttrs.isDirectory() && bAttrs.isDirectory()) {
                        return a.compareTo(b);
                    }

                    if (aAttrs.isDirectory()) return 1;
                    if (bAttrs.isDirectory()) return -1;

                    int result = Long.compare(bAttrs.size(), aAttrs.size());
                    if (result == 0) {
                        return a.compareTo(b);
                    }
                    return result;
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
        items.sort(comparator());
        return categorizer().categorize(res, items);
    }

    private static abstract class AttrsComparator
        implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo a, FileInfo b) {
            BasicFileAttributes aAttrs = a.selfAttrs();
            BasicFileAttributes bAttrs = b.selfAttrs();
            if (aAttrs == null && bAttrs == null) return a.compareTo(b);
            if (aAttrs == null) return 1;
            if (bAttrs == null) return -1;

            return compareNotNull(
                a, aAttrs,
                b, bAttrs
            );
        }

        protected abstract int compareNotNull(
            FileInfo a, BasicFileAttributes aAttrs,
            FileInfo b, BasicFileAttributes bAttrs
        );

    }

}
