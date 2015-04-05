package l.files.operations;


import auto.parcel.AutoParcel;
import l.files.fs.Path;

/**
 * Source and destination of a file task.
 */
@AutoParcel
public abstract class Target {

    public static final Target NONE = create("", "");

    Target() {
    }

    /**
     * Name of the source file/directory the task is operating from.
     */
    public abstract String getSource();

    /**
     * Name of the destination file/directory the task is operating to.
     */
    public abstract String getDestination();

    private static Target create(String source, String destination) {
        return new AutoParcel_Target(source, destination);
    }

    public static Target fromPaths(Iterable<Path> sources, Path dst) {
        Path src = sources.iterator().next().getParent();
        assert src != null;
        return create(src.getName(), dst.getName());
    }

    public static Target fromPaths(Iterable<Path> paths) {
        Path parent = paths.iterator().next().getParent();
        assert parent != null;
        return create(parent.getName(), parent.getName());
    }

}