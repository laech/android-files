package l.files.operations;

import android.os.Handler;

import java.util.Collection;

import l.files.fs.File;

import static l.files.operations.TaskKind.DELETE;

final class DeleteTask extends Task {

    private final Size count;
    private final Delete delete;

    DeleteTask(
            int id,
            Clock clock,
            Callback callback,
            Handler handler,
            Collection<? extends File> files) {

        super(
                TaskId.create(id, DELETE),
                Target.from(files),
                clock,
                callback,
                handler
        );

        this.count = new Size(files);
        this.delete = new Delete(files);
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
