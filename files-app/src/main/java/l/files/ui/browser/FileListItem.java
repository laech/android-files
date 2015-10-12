package l.files.ui.browser;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.io.IOException;
import java.text.Collator;

import l.files.collation.NaturalKey;
import l.files.fs.Stat;

import static l.files.fs.File.MEDIA_TYPE_OCTET_STREAM;

public abstract class FileListItem {

    FileListItem() {
    }

    public abstract boolean isFile();

    public boolean isHeader() {
        return !isFile();
    }

    @AutoValue
    public static abstract class Header extends FileListItem {

        Header() {
        }

        public abstract String header();

        public static Header of(String header) {
            return new AutoValue_FileListItem_Header(header);
        }

        @Override
        public boolean isFile() {
            return false;
        }

        @Override
        public String toString() {
            return header();
        }
    }

    @AutoValue
    public static abstract class File extends FileListItem implements Comparable<File> {

        private Boolean readable;

        File() {
        }

        // TODO don't do the following in the main thread

        public boolean isReadable() {
            if (readable == null) {
                try {
                    readable = file().isReadable();
                } catch (IOException e) {
                    readable = false;
                }
            }
            return readable;
        }

        public String basicMediaType() {
            try {
                return file().detectBasicMediaType(targetStat());
            } catch (IOException e) {
                return MEDIA_TYPE_OCTET_STREAM;
            }
        }

        public abstract l.files.fs.File file();

        @Nullable
        public abstract Stat stat(); // TODO

        @Nullable
        abstract Stat _targetStat();

        abstract NaturalKey collationKey();

        public static File create(
                l.files.fs.File file,
                @Nullable Stat stat,
                @Nullable Stat targetStat,
                Collator collator) {
            String name = file.name().toString();
            NaturalKey key = NaturalKey.create(collator, name);
            return new AutoValue_FileListItem_File(file, stat, targetStat, key);
        }

        @Override
        public boolean isFile() {
            return true;
        }

        /**
         * If the file is a link, this returns the status of the target
         * file, if not available, returns the status of the link.
         */
        @Nullable
        public Stat targetStat() {
            return _targetStat() != null ? _targetStat() : stat();
        }

        @Override
        public int compareTo(File another) {
            return collationKey().compareTo(another.collationKey());
        }
    }

}
