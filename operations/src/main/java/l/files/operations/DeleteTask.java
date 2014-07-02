package l.files.operations;

import java.io.File;

import l.files.io.file.operations.Delete;
import l.files.io.file.operations.Size;
import l.files.operations.info.DeleteTaskInfo;

final class DeleteTask extends Task implements DeleteTaskInfo {

    private final Size count;
    private final Delete delete;

    DeleteTask(int id, Iterable<String> paths) {
        super(id);
        this.count = new Size(paths);
        this.delete = new Delete(paths);
    }

    @Override
    protected void doTask() throws InterruptedException {
        count.call();
        delete.call();
    }

    @Override
    public int getTotalItemCount() {
        return count.getCount();
    }

    @Override
    public long getTotalByteCount() {
        return count.getSize();
    }

    @Override
    public int getProcessedItemCount() {
        return delete.getDeletedItemCount();
    }

    @Override
    public long getProcessedByteCount() {
        return delete.getDeletedByteCount();
    }

    @Override
    public String getSourceDirName() {
        String path = (count.isDone() ? delete : count).getCurrentPath();
        return new File(path).getParentFile().getName();
    }
}
