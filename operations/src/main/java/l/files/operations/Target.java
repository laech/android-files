package l.files.operations;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l.files.fs.File;

import static java.util.Collections.unmodifiableList;

/**
 * Source and destination of a file task.
 */
@AutoValue
public abstract class Target {

    Target() {
    }

    public abstract Collection<File> srcFiles();

    /**
     * Name of the destination directory the task is operating to.
     */
    public abstract File dstDir();

    public List<File> dstFiles() {
        List<File> dstFiles = new ArrayList<>(srcFiles().size());
        for (File src : srcFiles()) {
            dstFiles.add(dstDir().resolve(src.name()));
        }
        return dstFiles;
    }

    public static Target from(Collection<? extends File> srcFiles, File dstDir) {
        return new AutoValue_Target(
                unmodifiableList(new ArrayList<>(srcFiles)), dstDir);
    }

    public static Target from(Collection<? extends File> files) {
        return from(files, files.iterator().next().parent());
    }

}
