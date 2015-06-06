package l.files.operations;


import auto.parcel.AutoParcel;
import l.files.fs.Resource;
import l.files.fs.Resource.Name;

/**
 * Source and destination of a file task.
 */
@AutoParcel
public abstract class Target
{

    public static final Target NONE = create("", "");

    Target()
    {
    }

    /**
     * Name of the source file/directory the task is operating from.
     */
    public abstract Name source();

    /**
     * Name of the destination file/directory the task is operating to.
     */
    public abstract Name destination();

    public static Target create(final String source, final String destination)
    {
        return create(Name.of(source), Name.of(destination));
    }

    public static Target create(final Name source, final Name destination)
    {
        return new AutoParcel_Target(source, destination);
    }

    public static Target from(
            final Iterable<? extends Resource> sources,
            final Resource destination)
    {
        final Resource src = sources.iterator().next().parent();
        assert src != null;
        return create(src.name(), destination.name());
    }

    public static Target from(final Iterable<? extends Resource> resources)
    {
        final Resource parent = resources.iterator().next().parent();
        assert parent != null;
        return create(parent.name(), parent.name());
    }

}
