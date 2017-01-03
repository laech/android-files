package l.files.fs.event;

import java.io.IOException;
import java.util.Map;

import l.files.fs.Name;

public interface BatchObserver {

    void onLatestEvents(boolean selfChanged, Map<Name, Event> children);

    void onIncompleteObservation(IOException cause);

}
