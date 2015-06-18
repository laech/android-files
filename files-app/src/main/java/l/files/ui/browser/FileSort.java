package l.files.ui.browser;

import android.content.res.Resources;

import com.google.common.primitives.Longs;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import auto.parcel.AutoParcel;
import l.files.R;
import l.files.fs.Instant;
import l.files.fs.Resource.Name;
import l.files.fs.Stat;
import l.files.ui.browser.FileListItem.File;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableList;

public enum FileSort
{

    NAME(R.string.name)
            {
                @Override
                public Comparator<File> newComparator(final Locale locale)
                {
                    final Comparator<Name> comparator = Name.comparator(locale);
                    return new Comparator<File>()
                    {
                        @Override
                        public int compare(final File a, final File b)
                        {
                            return comparator.compare(
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

                @Override
                public List<FileListItem> sort(
                        final List<File> items,
                        final Resources res,
                        final Locale locale)
                {
                    final Collator collator = Collator.getInstance(locale);
                    final List<FileKey> keys = sort(items, collator);
                    return files(keys);
                }

                private List<FileKey> sort(
                        final List<File> items,
                        final Collator collator)
                {
                    final List<FileKey> keys = new ArrayList<>(items.size());
                    for (final File item : items)
                    {
                        final String name = item.resource().name().toString();
                        final CollationKey key = collator.getCollationKey(name);
                        keys.add(new FileKey(key, item));
                    }
                    Collections.sort(keys);
                    return keys;
                }

                private List<FileListItem> files(final List<FileKey> keys)
                {
                    final List<FileListItem> result = new ArrayList<>(keys.size());
                    for (final FileKey key : keys)
                    {
                        result.add(key.file);
                    }
                    return unmodifiableList(result);
                }

                @AutoParcel
                final class FileKey implements Comparable<FileKey>
                {
                    final CollationKey key;
                    final File file;

                    FileKey(final CollationKey key, final File file)
                    {
                        this.key = key;
                        this.file = file;
                    }

                    @Override
                    public int compareTo(final FileKey that)
                    {
                        return key.compareTo(that.key);
                    }
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
                                final File a, final Stat aStat,
                                final File b, final Stat bStat)
                        {
                            final Instant aTime = aStat.modified();
                            final Instant bTime = bStat.modified();
                            final int result = bTime.compareTo(aTime);
                            if (result == 0)
                            {
                                return nameComparator.compare(a, b);
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
                                final File a, final Stat aStat,
                                final File b, final Stat bStat)
                        {
                            if (aStat.isDirectory() && bStat.isDirectory())
                            {
                                return nameComparator.compare(a, b);
                            }

                            if (aStat.isDirectory()) return 1;
                            if (bStat.isDirectory()) return -1;

                            final int result = Longs.compare(bStat.size(), aStat.size());
                            if (result == 0)
                            {
                                return nameComparator.compare(a, b);
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

    public List<FileListItem> sort(
            final List<File> unsorted,
            final Resources res,
            final Locale locale)
    {
        final List<File> sorted = new ArrayList<>(unsorted);
        Collections.sort(sorted, newComparator(locale));
        return newCategorizer().categorize(res, sorted);
    }

    private static abstract class NullableFileStatComparator
            implements Comparator<File>
    {

        protected final Comparator<File> nameComparator;

        NullableFileStatComparator(final Locale locale)
        {
            nameComparator = NAME.newComparator(locale);
        }

        @Override
        public int compare(final File a, final File b)
        {
            if (a.stat() == null && b.stat() == null)
            {
                return nameComparator.compare(a, b);
            }

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
