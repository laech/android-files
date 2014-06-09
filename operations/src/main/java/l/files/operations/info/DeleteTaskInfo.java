package l.files.operations.info;

/**
 * Information of a running deletion task.
 */
public interface DeleteTaskInfo extends TaskInfo {

    /**
     * The currently known total number of items to be deleted.
     */
    int getTotalItemCount();

    /**
     * The number of items deleted so far.
     */
    int getDeletedItemCount();

    /**
     * The root directory where files are being deleted from.
     */
    String getSourceRootPath();
}
