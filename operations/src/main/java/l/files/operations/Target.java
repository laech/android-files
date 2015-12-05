package l.files.operations;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Collection;

import l.files.fs.Path;

import static java.util.Collections.unmodifiableList;

/**
 * Source and destination of a file task.
 */
@AutoValue
public abstract class Target {

    Target() {
    }

    public abstract Collection<Path> srcFiles();

    /**
     * Name of the destination directory the task is operating to.
     */
    public abstract Path dstDir();

    public static Target from(Collection<? extends Path> srcFiles, Path dstDir) {
        return new AutoValue_Target(
                unmodifiableList(new ArrayList<>(srcFiles)), dstDir);
    }

    public static Target from(Collection<? extends Path> files) {
        return from(files, files.iterator().next().parent());
    }

}
