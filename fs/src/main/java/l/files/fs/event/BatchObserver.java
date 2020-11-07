package l.files.fs.event;

import l.files.fs.Path;

import java.io.IOException;
import java.util.Map;

public interface BatchObserver {

    void onLatestEvents(boolean selfChanged, Map<Path, Event> childFileNames);

    void onIncompleteObservation(IOException cause);

}
