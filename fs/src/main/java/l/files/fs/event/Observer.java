package l.files.fs.event;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public interface Observer {

    /**
     * @param childFileName if null the event is for the observed file
     *                      itself, if not null the event is for the child of
     *                      the observed file with that this name
     */
    void onEvent(Event event, @Nullable Path childFileName);

    /**
     * Called when we can no longer fully observe on all files.
     * For example, internal system limit has been reached,
     * or some files are inaccessible.
     * This maybe called multiple times.
     */
    void onIncompleteObservation(IOException cause);

}
