package l.files.fs;

import java.util.Set;

public interface BatchObserver {

    void onBatchEvent(boolean selfChanged, Set<String> children);

    void onObserveFailed(String child);

    void onObserveRecovered(String child);

    void onCancel();

}
