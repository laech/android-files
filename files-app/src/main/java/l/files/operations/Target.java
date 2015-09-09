package l.files.operations;

import com.google.auto.value.AutoValue;

import l.files.fs.File;
import l.files.fs.FileName;

/**
 * Source and destination of a file task.
 */
@AutoValue
public abstract class Target {

    public static final Target NONE = create("", "");

    Target() {
    }

    /**
     * Name of the source file/directory the task is operating from.
     */
    public abstract FileName source();

    /**
     * Name of the destination file/directory the task is operating to.
     */
    public abstract FileName destination();

    public static Target create(String source, String destination) {
        return create(FileName.of(source), FileName.of(destination));
    }

    public static Target create(FileName source, FileName destination) {
        return new AutoValue_Target(source, destination);
    }

    public static Target from(Iterable<? extends File> sources, File destination) {
        File src = sources.iterator().next().parent();
        assert src != null;
        return create(src.name(), destination.name());
    }

    public static Target from(Iterable<? extends File> files) {
        File parent = files.iterator().next().parent();
        assert parent != null;
        return create(parent.name(), parent.name());
    }

}
