package l.files.operations.info;

public interface ProgressInfo extends TaskInfo {

    /**
     * The currently known total number of items to be processed.
     */
    int getTotalItemCount();

    /**
     * The total number of bytes.
     */
    long getTotalByteCount();

    /**
     * The number of items processed so far.
     */
    int getProcessedItemCount();

    /**
     * The total number of bytes processed.
     */
    long getProcessedByteCount();

}
