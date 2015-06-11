package l.files.ui.browser;

import com.google.common.net.MediaType;

import java.io.IOException;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.BasicDetector;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static com.google.common.net.MediaType.OCTET_STREAM;

public abstract class FileListItem
{

    FileListItem()
    {
    }

    public abstract boolean isFile();

    public boolean isHeader()
    {
        return !isFile();
    }

    @AutoParcel
    public static abstract class Header extends FileListItem
    {

        Header()
        {
        }

        public abstract String header();

        public static Header of(final String header)
        {
            return new AutoParcel_FileListItem_Header(header);
        }

        @Override
        public boolean isFile()
        {
            return false;
        }

        @Override
        public String toString()
        {
            return header();
        }
    }

    @AutoParcel
    public static abstract class File extends FileListItem
    {

        private Boolean readable;

        File()
        {
        }

        // TODO don't do the following in the main thread

        public boolean isReadable()
        {
            if (readable == null)
            {
                try
                {
                    readable = resource().readable();
                }
                catch (final IOException e)
                {
                    readable = false;
                }
            }
            return readable;
        }

        public MediaType basicMediaType()
        {
            try
            {
                return BasicDetector.INSTANCE.detect(resource());
            }
            catch (final IOException e)
            {
                return OCTET_STREAM;
            }
        }

        public abstract Resource resource();

        @Nullable
        public abstract Stat stat(); // TODO

        @Nullable
        abstract Stat _targetStat();

        public static File create(
                final Resource resource,
                @Nullable final Stat stat,
                @Nullable final Stat targetStat)
        {
            return new AutoParcel_FileListItem_File(resource, stat, targetStat);
        }

        @Override
        public boolean isFile()
        {
            return true;
        }

        /**
         * If the resource is a link, this returns the status of the target
         * file, if not available, returns the status of the link.
         */
        @Nullable
        public Stat targetStat()
        {
            return _targetStat() != null ? _targetStat() : stat();
        }
    }

}
