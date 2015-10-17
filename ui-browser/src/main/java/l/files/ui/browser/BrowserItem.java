package l.files.ui.browser;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.Collator;

import java.io.IOException;

import l.files.collation.NaturalKey;
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

        private Boolean readable;

        FileItem() {
        }

        // TODO don't do the following in the main thread

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

        // TODO compute this lazyly
        abstract NaturalKey collationKey();

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
            String name = file.name().toString();
            NaturalKey key = NaturalKey.create(collator, name);
            return new AutoValue_BrowserItem_FileItem(file, stat, target, targetStat, key);
        }
    }

}
