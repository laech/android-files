package l.files.fs;

import java.util.Map;

public interface BatchObserver {

    void onLatestEvents(boolean selfChanged, Map<Name, Event> children);

    void onIncompleteObservation();

}
