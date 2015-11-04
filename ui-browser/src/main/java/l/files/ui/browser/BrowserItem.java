package l.files.ui.browser;

import android.support.annotation.Nullable;
import android.support.v4.util.CircularArray;
import android.support.v4.util.CircularIntArray;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.ui.base.app.BaseApplication;
import l.files.ui.base.fs.FileIcons;

import static android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE;
import static android.text.format.Formatter.formatShortFileSize;
import static java.util.concurrent.TimeUnit.MINUTES;
import static l.files.fs.File.MEDIA_TYPE_OCTET_STREAM;
import static l.files.ui.base.fs.FileIcons.defaultDirectoryIconStringId;
import static l.files.ui.base.fs.FileIcons.defaultFileIconStringId;
import static l.files.ui.base.fs.FileIcons.fileIconStringId;

abstract class BrowserItem {

    BrowserItem() {
    }

    abstract boolean isFileItem();

    boolean isHeaderItem() {
        return !isFileItem();
    }

    @AutoValue
    static abstract class HeaderItem extends BrowserItem {

        HeaderItem() {
        }

        abstract String header();

        static HeaderItem of(String header) {
            return new AutoValue_BrowserItem_HeaderItem(header);
        }

        @Override
        boolean isFileItem() {
            return false;
        }

        @Override
        public String toString() {
            return header();
        }
    }

    @AutoValue
    static abstract class FileItem extends BrowserItem implements Comparable<FileItem> {

        private static final Object[] spansForIcon = {
                new MaxAlphaSpan(150),
                new AbsoluteSizeSpan(32, true),
                new TypefaceSpan(FileIcons.font(BaseApplication.get().getAssets())),
                new VerticalSpaceSpan(16, 6),
        };

        private static final Object[] spansForLink = {
                new MaxAlphaSpan(150),
                new AbsoluteSizeSpan(12, true),
                new VerticalSpaceSpan(3),
        };

        private static final Object[] spansForSummary = {
                new MaxAlphaSpan(150),
                new AbsoluteSizeSpan(12, true),
                new VerticalSpaceSpan(3),
        };

        private static final ThreadLocal<CircularIntArray> spanStarts =
                new ThreadLocal<CircularIntArray>() {
                    @Override
                    protected CircularIntArray initialValue() {
                        return new CircularIntArray(3);
                    }
                };

        private static final ThreadLocal<CircularIntArray> spanEnds =
                new ThreadLocal<CircularIntArray>() {
                    @Override
                    protected CircularIntArray initialValue() {
                        return new CircularIntArray(3);
                    }
                };

        private static final ThreadLocal<CircularArray<Object[]>> spanObjects =
                new ThreadLocal<CircularArray<Object[]>>() {
                    @Override
                    protected CircularArray<Object[]> initialValue() {
                        return new CircularArray<>(16);
                    }
                };

        private static final ThreadLocal<StringBuilder> spanBuilders =
                new ThreadLocal<StringBuilder>() {
                    @Override
                    protected StringBuilder initialValue() {
                        return new StringBuilder();
                    }
                };

        private static final ThreadLocal<DateFormatter> formatter =
                new ThreadLocal<DateFormatter>() {
                    @Override
                    protected DateFormatter initialValue() {
                        return new DateFormatter(BaseApplication.get());
                    }
                };

        private static SpannableString layout(FileItem item, boolean showIcon) {

            CircularIntArray localSpanStarts = spanStarts.get();
            CircularIntArray localSpanEnds = spanEnds.get();
            CircularArray<Object[]> localSpanObjects = spanObjects.get();
            StringBuilder localSpanBuilder = spanBuilders.get();

            localSpanStarts.clear();
            localSpanEnds.clear();
            localSpanObjects.clear();
            localSpanBuilder.setLength(0);

            Stat stat = item.selfStat();
            CharSequence name = item.selfFile().name();
            CharSequence summary = getSummary(item);
            CharSequence link = null;
            if (stat != null && stat.isSymbolicLink()) {
                File target = item.linkTargetFile();
                if (target != null) {
                    link = target.path();
                }
            }

            if (showIcon) {
                localSpanBuilder.append(BaseApplication.get().getString(iconTextId(item))).append('\n');
                localSpanStarts.addLast(0);
                localSpanEnds.addLast(localSpanBuilder.length());
                localSpanObjects.addLast(spansForIcon);
            }

            localSpanBuilder.append(name);

            if (link != null && link.length() > 0) {
                localSpanStarts.addLast(localSpanBuilder.length());
                localSpanBuilder.append('\n').append(BaseApplication.get().getString(R.string.link_x, link));
                localSpanEnds.addLast(localSpanBuilder.length());
                localSpanObjects.addLast(spansForLink);
            }

            if (summary != null && summary.length() > 0) {
                localSpanStarts.addLast(localSpanBuilder.length());
                localSpanBuilder.append('\n').append(summary);
                localSpanEnds.addLast(localSpanBuilder.length());
                localSpanObjects.addLast(spansForSummary);
            }

            SpannableString span = new SpannableString(localSpanBuilder.toString());
            while (!localSpanStarts.isEmpty()) {
                int start = localSpanStarts.popFirst();
                int end = localSpanEnds.popFirst();
                for (Object sp : localSpanObjects.popFirst()) {
                    if (sp instanceof VerticalSpaceSpan) {
                        span.setSpan(sp, start, start == 0 ? end : start + 1, SPAN_INCLUSIVE_EXCLUSIVE);
                    } else {
                        span.setSpan(sp, start, end, SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            return span;
        }


        private static int iconTextId(FileItem file) {
            Stat stat = file.linkTargetOrSelfStat();
            if (stat == null) {
                return defaultFileIconStringId();
            }

            if (stat.isDirectory()) {
                return defaultDirectoryIconStringId();
            } else {
                return fileIconStringId(file.basicMediaType());
            }
        }

        private static CharSequence getSummary(FileItem file) {
            Stat stat = file.selfStat();
            if (stat != null) {
                CharSequence date = formatter.get().apply(stat);
                CharSequence size = formatShortFileSize(BaseApplication.get(), stat.size());
                boolean hasDate = stat.lastModifiedTime().to(MINUTES) > 0;
                boolean isFile = stat.isRegularFile();
                if (hasDate && isFile) {
                    return BaseApplication.get().getString(R.string.x_dot_y, date, size);
                } else if (hasDate) {
                    return date;
                } else if (isFile) {
                    return size;
                }
            }
            return null;
        }


        private Collator collator;
        private CollationKey collationKey;
        private Boolean readable;
        private SpannableString layoutWithIcon;
        private SpannableString layoutWithoutIcon;

        FileItem() {
        }

        boolean isReadable() {
            if (readable == null) {
                try {
                    readable = selfFile().isReadable();
                } catch (IOException e) {
                    readable = false;
                }
            }
            return readable;
        }

        String basicMediaType() {
            try {
                return selfFile().detectBasicMediaType(linkTargetOrSelfStat());
            } catch (IOException e) {
                return MEDIA_TYPE_OCTET_STREAM;
            }
        }

        SpannableString layoutWithIcon() {
            if (layoutWithIcon == null) {
                layoutWithIcon = layout(this, true);
            }
            return layoutWithIcon;
        }

        SpannableString layoutWithoutIcon() {
            if (layoutWithoutIcon == null) {
                layoutWithoutIcon = layout(this, false);
            }
            return layoutWithoutIcon;
        }

        abstract File selfFile();

        @Nullable
        abstract Stat selfStat();

        @Nullable
        abstract File linkTargetFile();

        @Nullable
        abstract Stat linkTargetStat();

        @Nullable
        Stat linkTargetOrSelfStat() {
            return linkTargetStat() != null ? linkTargetStat() : selfStat();
        }

        private CollationKey collationKey() {
            if (collationKey == null) {
                collationKey = collator.getCollationKey(selfFile().name().toString());
            }
            return collationKey;
        }

        @Override
        boolean isFileItem() {
            return true;
        }

        @Override
        public int compareTo(FileItem another) {
            return collationKey().compareTo(another.collationKey());
        }

        static FileItem create(
                File file,
                @Nullable Stat stat,
                @Nullable File target,
                @Nullable Stat targetStat,
                Collator collator) {
            FileItem item = new AutoValue_BrowserItem_FileItem(file, stat, target, targetStat);
            item.collator = collator;
            return item;
        }
    }

}
