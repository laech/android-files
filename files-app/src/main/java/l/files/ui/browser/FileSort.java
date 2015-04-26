package l.files.ui.browser;

import android.content.res.Resources;

import com.google.common.primitives.Longs;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import l.files.R;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;

import static java.lang.System.currentTimeMillis;

public enum FileSort {

    NAME(R.string.name) {
        @Override
        public Comparator<FileListItem.File> newComparator(Locale locale) {
            final Collator collator = Collator.getInstance(locale);
            return new Comparator<FileListItem.File>() {
                @Override
                public int compare(FileListItem.File a, FileListItem.File b) {
                    return collator.compare(a.getResource().getName(), b.getResource().getName());
                }
            };
        }

        @Override
        public Categorizer newCategorizer() {
            return Categorizer.NULL;
        }
    },

    MODIFIED(R.string.date_modified) {
        @Override
        public Comparator<FileListItem.File> newComparator(Locale locale) {
            return new NullableFileStatComparator(locale) {
                @Override
                protected int compareNotNull(
                        Resource a, ResourceStatus aStatus,
                        Resource b, ResourceStatus bStatus
                ) {
                    int result = bStatus.getModificationTime().compareTo(
                            aStatus.getModificationTime());
                    if (result == 0) {
                        result = nameComparator.compare(a.getName(), b.getName());
                    }
                    return result;
                }
            };
        }

        @Override
        public Categorizer newCategorizer() {
            return new DateCategorizer(currentTimeMillis());
        }
    },

    SIZE(R.string.size) {
        @Override
        public Comparator<FileListItem.File> newComparator(final Locale locale) {
            return new NullableFileStatComparator(locale) {
                @Override
                protected int compareNotNull(
                        Resource a, ResourceStatus aStatus,
                        Resource b, ResourceStatus bStatus
                ) {
                    if (aStatus.isDirectory() && bStatus.isDirectory()) {
                        return nameComparator.compare(a.getName(), b.getName());
                    }
                    if (aStatus.isDirectory()) {
                        return 1;
                    }
                    if (bStatus.isDirectory()) {
                        return -1;
                    }
                    int compare = Longs.compare(bStatus.getSize(), aStatus.getSize());
                    if (compare == 0) {
                        return nameComparator.compare(a.getName(), b.getName());
                    }
                    return compare;
                }
            };
        }

        @Override
        public Categorizer newCategorizer() {
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

        @Override
        public int compare(FileListItem.File a, FileListItem.File b) {
            if (a.getStat() == null && b.getStat() == null) {
                return nameComparator.compare(a.getResource().getName(), b.getResource().getName());
            }
            if (a.getStat() == null) {
                return 1;
            }
            if (b.getStat() == null) {
                return -1;
            }
            return compareNotNull(
                    a.getResource(), a.getStat(),
                    b.getResource(), b.getStat()
            );
        }

        protected abstract int compareNotNull(
                Resource a, ResourceStatus aStatus,
                Resource b, ResourceStatus bStatus);
    }
}
