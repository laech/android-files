package l.files.ui.newdir;

import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;

final class Suggestion
{
    private Suggestion()
    {
    }

    /**
     * Message to request a new name suggest for a directory.
     */
    @AutoParcel
    public static abstract class Request
    {
        Request()
        {
        }

        public abstract Resource base();

        public static Request basedOn(final Resource base)
        {
            return new AutoParcel_Suggestion_Request(base);
        }
    }

    /**
     * Message indicating a name suggestion has completed.
     */
    @AutoParcel
    public static abstract class Completion
    {
        Completion()
        {
        }

        public abstract Resource suggestion();

        public static Completion suggest(final Resource suggestion)
        {
            return new AutoParcel_Suggestion_Completion(suggestion);
        }
    }

    /**
     * Message indicating a name suggestion has failed.
     */
    @AutoParcel
    public static abstract class Failure
    {
        Failure()
        {
        }

        public abstract IOException cause();

        public static Failure causedBy(final IOException cause)
        {
            return new AutoParcel_Suggestion_Failure(cause);
        }
    }
}
