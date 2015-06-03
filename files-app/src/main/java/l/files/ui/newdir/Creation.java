package l.files.ui.newdir;

import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;

final class Creation
{
    private Creation()
    {
    }

    /**
     * Message to request a directory to be created.
     */
    @AutoParcel
    public static abstract class Request
    {
        Request()
        {
        }

        public abstract Resource dir();

        public static Request target(final Resource dir)
        {
            return new AutoParcel_Creation_Request(dir);
        }
    }

    /**
     * Message indicating failure to create a directory.
     */
    @AutoParcel
    public static abstract class Failure
    {
        Failure()
        {
        }

        public abstract IOException cause();

        public static Failure causedBy(final IOException e)
        {
            return new AutoParcel_Creation_Failure(e);
        }
    }
}
