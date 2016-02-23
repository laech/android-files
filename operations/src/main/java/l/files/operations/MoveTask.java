package l.files.operations;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l.files.fs.Name;
import l.files.fs.Path;

import static l.files.operations.TaskKind.MOVE;

final class MoveTask extends Task {

    private final Path sourceDirectory;
    private final Move move;
    private final Size count;
    private final Copy copy;

    MoveTask(
            int id,
            Clock clock,
            Callback callback,
            Handler handler,
            Path sourceDirectory,
            Collection<? extends Name> sourceFiles,
            Path destinationDirectory) {

        super(
                TaskId.create(id, MOVE),
                Target.from(sourceDirectory, sourceFiles, destinationDirectory),
                clock,
                callback,
                handler
        );

        this.sourceDirectory = sourceDirectory;
        this.move = new Move(sourceDirectory, sourceFiles, destinationDirectory);
        this.count = new Size(sourceDirectory, sourceFiles);
        this.copy = new Copy(sourceDirectory, sourceFiles, destinationDirectory);
    }

    @Override
    void doTask() throws FileException, InterruptedException {
        try {
            move.execute();
        } catch (FileException e) {
            // TODO failure is limited and not all source files are here
            copyThenDelete(e.failures());
        }
    }

    private void copyThenDelete(List<Failure> failures)
            throws FileException, InterruptedException {

        List<Name> sourceFiles = new ArrayList<>();
        for (Failure failure : failures) {
            sourceFiles.add(failure.file());
        }
        count.execute();
        copy.execute();
        new Delete(sourceDirectory, sourceFiles).execute();
    }

    @Override
    TaskState.Running running(TaskState.Running state) {
        return state.running(
                Progress.normalize(count.getCount(), copy.getCopiedItemCount()),
                Progress.normalize(count.getSize(), copy.getCopiedByteCount())
        );
    }
}
