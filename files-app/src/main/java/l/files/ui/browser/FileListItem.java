package l.files.ui.browser;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;

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
        private Boolean writable;
        private Boolean executable;

        File() {
        }

        public boolean isReadable() {
            if (readable == null) {
                readable = getResource().isReadable();
            }
            return readable;
        }

        public boolean isWritable() {
            if (writable == null) {
                writable = getResource().isWritable();
            }
            return writable;
        }

        public boolean isExecutable() {
            if (executable == null) {
                executable = getResource().isExecutable();
            }
            return executable;
        }

        public abstract Resource getResource();

        @Nullable
        public abstract ResourceStatus getStat();

        @Nullable
        abstract ResourceStatus _targetStat();

        public static File create(Resource resource,
                                  @Nullable ResourceStatus stat,
                                  @Nullable ResourceStatus targetStat) {
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
        public ResourceStatus getTargetStat() {
            return _targetStat() != null ? _targetStat() : getStat();
        }
    }

}
