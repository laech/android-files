package l.files.operations;


import auto.parcel.AutoParcel;
import l.files.fs.Resource;

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

    public static Target create(String source, String destination) {
        return new AutoParcel_Target(source, destination);
    }

    public static Target from(Iterable<? extends Resource> sources, Resource destination) {
        Resource src = sources.iterator().next().getParent();
        assert src != null;
        return create(src.getName(), destination.getName());
    }

    public static Target from(Iterable<? extends Resource> resources) {
        Resource parent = resources.iterator().next().getParent();
        assert parent != null;
        return create(parent.getName(), parent.getName());
    }

}
