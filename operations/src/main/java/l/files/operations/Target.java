package l.files.operations;

import java.util.ArrayList;
import java.util.Collection;

import l.files.base.Objects;
import l.files.fs.Path;

import static java.util.Collections.unmodifiableList;
import static l.files.base.Objects.requireNonNull;

/**
 * Source and destination of a file task.
 */
public final class Target {

    private final Collection<Path> srcFiles;
    private final Path dstDir;

    private Target(Collection<Path> srcFiles, Path dstDir) {
        this.srcFiles = requireNonNull(srcFiles);
        this.dstDir = requireNonNull(dstDir);
    }

    public Collection<Path> srcFiles() {
        return srcFiles;
    }

    /**
     * Name of the destination directory the task is operating to.
     */
    public Path dstDir() {
        return dstDir;
    }

    public static Target from(Collection<? extends Path> srcFiles, Path dstDir) {
        return new Target(unmodifiableList(new ArrayList<>(srcFiles)), dstDir);
    }

    public static Target from(Collection<? extends Path> files) {
        return from(files, files.iterator().next().parent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equal(srcFiles, target.srcFiles) &&
                Objects.equal(dstDir, target.dstDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcFiles, dstDir);
    }

    @Override
    public String toString() {
        return "Target{" +
                "srcFiles=" + srcFiles +
                ", dstDir=" + dstDir +
                '}';
    }

}
