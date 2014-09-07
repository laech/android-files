package l.files.operations;

/**
 * Information of a running deletion task.
 */
public interface DeleteTaskInfo extends ProgressInfo {

    /**
     * The name of the directory where files are being deleted from.
     */
    String getSourceDirName();
}
