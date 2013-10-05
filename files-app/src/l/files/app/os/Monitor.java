package l.files.app.os;

import com.google.common.base.Optional;

import java.io.File;
import java.util.List;

/**
 * Monitors directories for content changes.
 * All operations are intended to be called on the main thread only.
 */
public interface Monitor {

    /**
     * Registers a callback to be notified of content change on the given directory.
     * Has no affect if callback is already registered.
     */
    void register(Callback callback, File dir);

    /**
     * Unregisters a callback from being notified of content change on the given directory.
     * Has no affect if callback is already unregistered.
     */
    void unregister(Callback callback, File dir);

    public static interface Callback {
        void onRefreshed(Optional<? extends List<?>> content);
    }
}
