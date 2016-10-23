package l.files.fs;

import java.io.IOException;
import java.util.Map;

public interface BatchObserver {

    void onLatestEvents(boolean selfChanged, Map<Path, Event> children);

    void onIncompleteObservation(IOException cause);

}
