package l.files.operations;

import android.os.Handler;

import java.nio.file.Path;
import java.util.Collection;

import static l.files.operations.TaskKind.DELETE;

final class DeleteTask extends Task {

    private final Size count;
    private final Delete delete;

    DeleteTask(
        int id,
        Clock clock,
        Callback callback,
        Handler handler,
        Collection<? extends Path> paths
    ) {
        super(
            TaskId.create(id, DELETE),
            Target.from(paths),
            clock,
            callback,
            handler
        );

        this.count = new Size(paths);
        this.delete = new Delete(paths);
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
