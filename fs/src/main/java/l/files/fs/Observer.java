package l.files.fs;

import java.io.IOException;

public interface Observer {

    /**
     * @param child if null the event is for the observed file itself, if not
     *              null the event is for the child of the observed file with
     *              that this name
     */
    void onEvent(Event event, Name child);

    /**
     * Called when we can no longer fully observe on all files.
     * For example, internal system limit has been reached,
     * or some files are inaccessible.
     * This maybe called multiple times.
     */
    void onIncompleteObservation(IOException cause);

}
