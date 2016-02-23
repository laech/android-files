package l.files.operations;

import java.util.ArrayList;
import java.util.Collection;

import l.files.base.Objects;
import l.files.fs.Name;
import l.files.fs.Path;

import static java.util.Collections.unmodifiableList;
import static l.files.base.Objects.requireNonNull;

/**
 * Source and destination of a file task.
 */
public final class Target {

    private final Collection<Name> sourceFiles;
    private final Path sourceDirectory;
    private final Path destinationDirectory;

    private Target(
            Path sourceDirectory,
            Collection<Name> sourceFiles,
            Path destinationDirectory) {
        
        this.sourceDirectory = requireNonNull(sourceDirectory);
        this.sourceFiles = requireNonNull(sourceFiles);
        this.destinationDirectory = requireNonNull(destinationDirectory);
    }

    public Collection<Name> srcFiles() {
        return sourceFiles;
    }

    public Path sourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Name of the destination directory the task is operating to.
     */
    public Path destinationDirectory() {
        return destinationDirectory;
    }

    public static Target from(
            Path sourceDirectory,
            Collection<? extends Name> sourceFiles,
            Path destinationDirectory) {

        return new Target(
                sourceDirectory,
                unmodifiableList(new ArrayList<>(sourceFiles)),
                destinationDirectory);
    }

    public static Target from(Path sourceDirectory, Collection<? extends Name> sourceFiles) {
        return from(sourceDirectory, sourceFiles, sourceDirectory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equal(sourceDirectory, target.sourceDirectory) &&
                Objects.equal(sourceFiles, target.sourceFiles) &&
                Objects.equal(destinationDirectory, target.destinationDirectory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceDirectory, sourceFiles, destinationDirectory);
    }

    @Override
    public String toString() {
        return "Target{" +
                "sourceDirectory=" + sourceDirectory +
                ", sourceFiles=" + sourceFiles +
                ", destinationDirectory=" + destinationDirectory +
                '}';
    }

}
