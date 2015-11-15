package l.files.ui.browser;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Stat;

import static l.files.fs.File.MEDIA_TYPE_OCTET_STREAM;

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

        private Provider<Collator> collator;
        private CollationKey collationKey;
        private Boolean readable;

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
                collationKey = collator.get()
                        .getCollationKey(selfFile().name().toString());
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
                Provider<Collator> collator) {
            FileItem item = new AutoValue_BrowserItem_FileItem(file, stat, target, targetStat);
            item.collator = collator;
            return item;
        }
    }

}
