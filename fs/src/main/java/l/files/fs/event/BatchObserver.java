package l.files.fs.event;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface BatchObserver {

    void onLatestEvents(boolean selfChanged, Map<Path, Event> childFileNames);

    void onIncompleteObservation(IOException cause);

}
