package l.files.fs;

import java.util.Set;

public interface BatchObserver {

    void onBatchEvent(boolean selfChanged, Set<Name> children);

    void onIncompleteObservation();

}
