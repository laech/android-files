package l.files.ui.browser;

import com.google.common.net.MediaType;

import java.io.IOException;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.BasicDetector;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static com.google.common.net.MediaType.OCTET_STREAM;

public abstract class FileListItem {

    FileListItem() {
    }

    public abstract boolean isFile();

    public boolean isHeader() {
        return !isFile();
    }

    @AutoParcel
    public static abstract class Header extends FileListItem {

        Header() {
        }

        public abstract String getHeader();

        public static Header create(String header) {
            return new AutoParcel_FileListItem_Header(header);
        }

        @Override
        public boolean isFile() {
            return false;
        }

        @Override
        public String toString() {
            return getHeader();
        }
    }

    @AutoParcel
    public static abstract class File extends FileListItem {

        private Boolean readable;

        File() {
        }

        // TODO don't do the following in the main thread

        public boolean isReadable() {
            if (readable == null) {
                try {
                    readable = getResource().readable();
                } catch (IOException e) {
                    readable = false;
                }
            }
            return readable;
        }

        public MediaType getBasicMediaType() {
            try {
                return BasicDetector.INSTANCE.detect(getResource());
            } catch (IOException e) {
                return OCTET_STREAM;
            }
        }

        public abstract Resource getResource();

        @Nullable
        public abstract Stat getStat();

        @Nullable
        abstract Stat _targetStat();

        public static File create(Resource resource,
                                  @Nullable Stat stat,
                                  @Nullable Stat targetStat) {
            return new AutoParcel_FileListItem_File(resource, stat, targetStat);
        }

        @Override
        public boolean isFile() {
            return true;
        }

        /**
         * If the resource is a link, this returns the status of the target
         * file, if not available, returns the status of the link.
         */
        @Nullable
        public Stat getTargetStat() {
            return _targetStat() != null ? _targetStat() : getStat();
        }
    }

}
