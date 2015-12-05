package l.files.ui.browser;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

import java.io.IOException;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.Files.MEDIA_TYPE_OCTET_STREAM;

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
        private String basicMediaType;

        FileItem() {
        }

        boolean isReadable() {
            if (readable == null) {
                try {
                    readable = Files.isReadable(selfPath());
                } catch (IOException e) {
                    readable = false;
                }
            }
            return readable;
        }

        String basicMediaType() {
            if (basicMediaType == null) {
                try {
                    basicMediaType = Files.detectBasicMediaType(
                            selfPath(), linkTargetOrSelfStat());
                } catch (IOException e) {
                    basicMediaType = MEDIA_TYPE_OCTET_STREAM;
                }
            }
            return basicMediaType;
        }

        abstract Path selfPath();

        @Nullable
        abstract Stat selfStat();

        @Nullable
        abstract Path linkTargetPath();

        @Nullable
        abstract Stat linkTargetStat();

        @Nullable
        Stat linkTargetOrSelfStat() {
            return linkTargetStat() != null ? linkTargetStat() : selfStat();
        }

        Path linkTargetOrSelfPath() {
            return linkTargetPath() != null ? linkTargetPath() : selfPath();
        }

        private CollationKey collationKey() {
            if (collationKey == null) {
                collationKey = collator.get()
                        .getCollationKey(selfPath().name().toString());
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
                Path path,
                @Nullable Stat stat,
                @Nullable Path target,
                @Nullable Stat targetStat,
                Provider<Collator> collator) {
            FileItem item = new AutoValue_BrowserItem_FileItem(path, stat, target, targetStat);
            item.collator = collator;
            return item;
        }
    }

}
