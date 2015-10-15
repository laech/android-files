package l.files.ui.operations;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import l.files.fs.File;
import l.files.operations.OperationService;
import l.files.operations.Target;
import l.files.operations.TaskNotFound;
import l.files.operations.TaskState;

import static l.files.ui.base.fs.FileIntents.broadcastFilesChanged;

final class FilesChangedBroadcaster implements OperationService.TaskListener {

    private final Context context;

    FilesChangedBroadcaster(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onUpdate(TaskState state) {
        if (state.isFinished()) {
            Target target = state.getTarget();
            Set<File> files = new HashSet<>(target.srcFiles());
            files.addAll(target.dstFiles());
            broadcastFilesChanged(files, context);
        }
    }

    @Override
    public void onNotFound(TaskNotFound notFound) {
    }

}
