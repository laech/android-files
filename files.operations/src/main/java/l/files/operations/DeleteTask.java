package l.files.operations;

import com.google.common.collect.ImmutableList;

import l.files.io.file.operations.Count;
import l.files.io.file.operations.Delete;
import l.files.operations.info.DeleteTaskInfo;

import static com.google.common.base.Preconditions.checkNotNull;

final class DeleteTask extends Task
        implements DeleteTaskInfo, Count.Listener, Delete.Listener {

    private final String sourceRootPath;
    private final Iterable<String> paths;

    private volatile int totalItemCount;
    private volatile int deletedItemCount;

    DeleteTask(int id, String sourceRootPath, Iterable<String> paths) {
        super(id);
        this.paths = ImmutableList.copyOf(paths);
        this.sourceRootPath = checkNotNull(sourceRootPath);
    }

    @Override
    protected void doTask() throws InterruptedException {
        new Count(this, paths).call();
        new Delete(this, paths).call();
    }

    @Override
    public void onCount(String path) {
        totalItemCount++;
        if (setAndGetUpdateProgress()) {
            notifyProgress();
        }
    }

    @Override
    public void onDelete(String path) {
        deletedItemCount++;
        if (setAndGetUpdateProgress()) {
            notifyProgress();
        }
    }

    @Override
    public int getTotalItemCount() {
        return totalItemCount;
    }

    @Override
    public int getDeletedItemCount() {
        return deletedItemCount;
    }

    @Override
    public String getSourceRootPath() {
        return sourceRootPath;
    }
}
