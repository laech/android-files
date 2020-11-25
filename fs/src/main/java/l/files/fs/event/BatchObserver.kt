package l.files.fs.event;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;

public interface BatchObserver {

    void onLatestEvents(
        boolean selfChanged,
        Map<Path, WatchEvent.Kind<?>> childFileNames
    );

    void onIncompleteObservation(IOException cause);

}
