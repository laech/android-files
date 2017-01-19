package l.files.fs.event;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.Name;

public interface Observer {

    /**
     * @param child if null the event is for the observed file itself, if not
     *              null the event is for the child of the observed file with
     *              that this name
     */
    void onEvent(Event event, @Nullable Name child);

    /**
     * Called when we can no longer fully observe on all files.
     * For example, internal system limit has been reached,
     * or some files are inaccessible.
     * This maybe called multiple times.
     */
    void onIncompleteObservation(IOException cause);

}
