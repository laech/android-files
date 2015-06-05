package l.files.ui.browser;

import android.content.res.Resources;

import com.google.common.primitives.Longs;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import l.files.R;
import l.files.fs.Instant;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.ui.browser.FileListItem.File;

import static java.lang.System.currentTimeMillis;

public enum FileSort
{

    NAME(R.string.name)
    {
        @Override
        public Comparator<File> newComparator(final Locale locale)
        {
            final Collator collator = Collator.getInstance(locale);
            return new Comparator<File>()
            {
                @Override
                public int compare(final File a, final File b)
                {
                    return collator.compare(
                            a.resource().name(),
                            b.resource().name());
                }
            };
        }

        @Override
        public Categorizer newCategorizer()
        {
            return Categorizer.NULL;
        }
    },

    MODIFIED(R.string.date_modified)
    {
        @Override
        public Comparator<File> newComparator(final Locale locale)
        {
            return new NullableFileStatComparator(locale)
            {
                @Override
                protected int compareNotNull(
                        final Resource a, final Stat aStat,
                        final Resource b, final Stat bStat)
                {
                    final Instant aTime = aStat.modificationTime();
                    final Instant bTime = bStat.modificationTime();
                    final int result = bTime.compareTo(aTime);
                    if (result == 0)
                    {
                        return nameComparator.compare(a.name(), b.name());
                    }
                    return result;
                }
            };
        }

        @Override
        public Categorizer newCategorizer()
        {
            return new DateCategorizer(currentTimeMillis());
        }
    },

    SIZE(R.string.size)
    {
        @Override
        public Comparator<File> newComparator(final Locale locale)
        {
            return new NullableFileStatComparator(locale)
            {
                @Override
                protected int compareNotNull(
                        final Resource a, final Stat aStat,
                        final Resource b, final Stat bStat)
                {
                    if (aStat.isDirectory() && bStat.isDirectory())
                    {
                        return nameComparator.compare(a.name(), b.name());
                    }

                    if (aStat.isDirectory()) return 1;
                    if (bStat.isDirectory()) return -1;

                    final int result = Longs.compare(bStat.size(), aStat.size());
                    if (result == 0)
                    {
                        return nameComparator.compare(a.name(), b.name());
                    }
                    return result;
                }
            };
        }

        @Override
        public Categorizer newCategorizer()
        {
            return new SizeCategorizer();
        }
    };

    private final int labelId;

    FileSort(final int labelId)
    {
        this.labelId = labelId;
    }

    public String getLabel(final Resources res)
    {
        return res.getString(labelId);
    }

    public abstract Comparator<File> newComparator(Locale locale);

    public abstract Categorizer newCategorizer();

    private static abstract class NullableFileStatComparator
            implements Comparator<File>
    {

        protected final Collator nameComparator;

        NullableFileStatComparator(final Locale locale)
        {
            nameComparator = Collator.getInstance(locale);
        }

        @Override
        public int compare(final File a, final File b)
        {
            final Resource aRes = a.resource();
            final Resource bRes = b.resource();
            if (a.stat() == null && b.stat() == null)
            {
                return nameComparator.compare(aRes.name(), bRes.name());
            }

            if (a.stat() == null) return 1;
            if (b.stat() == null) return -1;

            return compareNotNull(
                    aRes, a.stat(),
                    bRes, b.stat());
        }

        protected abstract int compareNotNull(
                Resource a, Stat aStat,
                Resource b, Stat bStat);
    }
}
