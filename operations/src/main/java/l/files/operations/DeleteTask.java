package l.files.operations;

import l.files.io.file.operations.Count;
import l.files.io.file.operations.Delete;
import l.files.operations.info.DeleteTaskInfo;

final class DeleteTask extends Task implements DeleteTaskInfo {

    private final Count count;
    private final Delete delete;

    DeleteTask(int id, Iterable<String> paths) {
        super(id);
        this.count = new Count(paths);
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
    public int getDeletedItemCount() {
        return delete.getDeletedItemCount();
    }

    @Override
    public String getSourceRootPath() {
        return (count.isDone() ? delete : count).getCurrentPath();
    }
}
