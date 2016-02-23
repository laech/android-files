package l.files.operations;

import android.os.Handler;

import java.util.Collection;

import l.files.fs.Name;
import l.files.fs.Path;

import static l.files.operations.TaskKind.DELETE;

final class DeleteTask extends Task {

    private final Size count;
    private final Delete delete;

    DeleteTask(
            int id,
            Clock clock,
            Callback callback,
            Handler handler,
            Path sourceDirectory,
            Collection<? extends Name> sourceFiles) {

        super(
                TaskId.create(id, DELETE),
                Target.from(sourceDirectory, sourceFiles),
                clock,
                callback,
                handler
        );

        this.count = new Size(sourceDirectory, sourceFiles);
        this.delete = new Delete(sourceDirectory, sourceFiles);
    }

    @Override
    void doTask() throws FileException, InterruptedException {
        count.execute();
        delete.execute();
    }

    @Override
    TaskState.Running running(TaskState.Running state) {
        return state.running(
                Progress.normalize(count.getCount(), delete.getDeletedItemCount()),
                Progress.normalize(count.getSize(), delete.getDeletedByteCount())
        );
    }

}
