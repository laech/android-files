package l.files.fs.event;

import java.io.IOException;
import java.util.Map;

import l.files.fs.Path;

public interface BatchObserver {

    void onLatestEvents(boolean selfChanged, Map<Path, Event> children);

    void onIncompleteObservation(IOException cause);

}
