package l.files.operations;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l.files.fs.File;

import static l.files.operations.TaskKind.MOVE;

final class MoveTask extends Task {

    private final Move move;
    private final Size count;
    private final Copy copy;

    MoveTask(
            int id,
            Clock clock,
            Callback callback,
            Handler handler,
            Collection<? extends File> sources,
            File destination) {

        super(
                TaskId.create(id, MOVE),
                Target.from(sources, destination),
                clock,
                callback,
                handler
        );

        this.move = new Move(sources, destination);
        this.count = new Size(sources);
        this.copy = new Copy(sources, destination);
    }

    @Override
    void doTask() throws FileException, InterruptedException {
        try {
            move.execute();
        } catch (FileException e) {
            copyThenDelete(e.failures());
        }
    }

    private void copyThenDelete(List<Failure> failures)
            throws FileException, InterruptedException {

        List<File> files = new ArrayList<>();
        for (Failure failure : failures) {
            files.add(failure.resource());
        }
        count.execute();
        copy.execute();
        new Delete(files).execute();
    }

    @Override
    TaskState.Running running(TaskState.Running state) {
        return state.running(
                Progress.normalize(count.getCount(), copy.getCopiedItemCount()),
                Progress.normalize(count.getSize(), copy.getCopiedByteCount())
        );
    }
}
