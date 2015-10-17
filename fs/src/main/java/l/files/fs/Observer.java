package l.files.fs;

public interface Observer {

    /**
     * @param child if null the event is for the observed file itself, if not
     *              null the event is for the child of the observed file with
     *              that this name
     */
    void onEvent(Event event, String child);

    /**
     * Notifies the given child is not observable.
     * e.g. no read permission.
     */
    void onObserveFailed(String child);

    /**
     * Notifies the given child is observable,
     * after a previous call to {@link #onObserveFailed(String)}.
     */
    void onObserveRecovered(String child);

    /**
     * Called when internal system limit has been reached and we can no longer
     * observe on anymore files/directories. Existing watches for this observer
     * registration will be cancelled.
     */
    void onCancel();

}
