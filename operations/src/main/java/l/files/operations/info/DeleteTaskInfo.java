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
     * The total number of bytes.
     */
    long getTotalByteCount();

    /**
     * The number of items deleted so far.
     */
    int getDeletedItemCount();

    /**
     * The total number of bytes deleted.
     */
    long getDeletedByteCount();

    /**
     * The name of the directory where files are being deleted from.
     */
    String getDirName();
}
