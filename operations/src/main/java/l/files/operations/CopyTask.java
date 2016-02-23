package l.files.operations;

import android.os.Handler;

import java.util.Collection;

import l.files.fs.Name;
import l.files.fs.Path;

import static l.files.operations.TaskKind.COPY;

final class CopyTask extends Task {

    private final Size size;
    private final Copy copy;

    CopyTask(
            int id,
            Clock clock,
            Callback callback,
            Handler handler,
            Path sourceDirectory,
            Collection<? extends Name> sourceFiles,
            Path destinationDirectory) {

        super(
                TaskId.create(id, COPY),
                Target.from(sourceDirectory, sourceFiles, destinationDirectory),
                clock,
                callback,
                handler);

        this.size = new Size(sourceDirectory, sourceFiles);
        this.copy = new Copy(sourceDirectory, sourceFiles, destinationDirectory);
    }

    @Override
    void doTask() throws FileException, InterruptedException {
        size.execute();
        copy.execute();
    }

    @Override
    TaskState.Running running(TaskState.Running state) {
        return state.running(
                Progress.normalize(size.getCount(), copy.getCopiedItemCount()),
                Progress.normalize(size.getSize(), copy.getCopiedByteCount())
        );
    }
}
