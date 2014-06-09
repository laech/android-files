package l.files.operations;

import com.google.common.collect.ImmutableList;

import java.io.File;

import l.files.io.file.operations.Count;
import l.files.io.file.operations.Delete;
import l.files.operations.info.DeleteTaskInfo;

import static java.util.Collections.singleton;

final class DeleteTask extends Task
        implements DeleteTaskInfo, Count.Listener, Delete.Listener {

    private final Iterable<String> paths;
    private volatile String sourceRootPath;
    private volatile int totalItemCount;
    private volatile int deletedItemCount;

    DeleteTask(int id, Iterable<String> paths) {
        super(id);
        this.paths = ImmutableList.copyOf(paths);
    }

    @Override
    protected void doTask() throws InterruptedException {
        for (String path : paths) {
            sourceRootPath = new File(path).getParent();
            new Count(this, singleton(path)).call();
        }
        for (String path : paths) {
            sourceRootPath = new File(path).getParent();
            new Delete(this, singleton(path)).call();
        }
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
