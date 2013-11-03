package l.files.app.os;

import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;

import java.io.File;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.BuildConfig.DEBUG;

final class DirObserver extends FileObserver {

    private static final String TAG = DirObserver.class.getSimpleName();

    /**
     * Wait for this amount of milliseconds when an event is received before
     * notifying the listener, batching subsequent events then update in one go,
     * this is more efficient and avoids interrupting any animations for the
     * previous update.
     */
    static final long BATCH_UPDATE_DELAY = 75;

    /**
     * Mask for all change events within a directory.
     */
    static final int DIR_CHANGED_MASK = CREATE | DELETE | MOVED_FROM | MOVED_TO | MODIFY;

    /**
     * Mask for all change events within a directory, except modifications, this is
     * useful when you only want to be notified when contents are being added/removed,
     * but not when they are modified.
     */
    static final int DIR_CHANGED_MASK_NO_MODIFY = CREATE | DELETE | MOVED_FROM | MOVED_TO;

    private final File mDirectory;
    private final Handler mHandler;
    private final Runnable mListener;

    DirObserver(File dir, Handler handler, Runnable listener) {
        this(dir, handler, listener, DIR_CHANGED_MASK);
    }

    DirObserver(File dir, Handler handler, Runnable listener, int mask) {
        super(checkNotNull(dir, "dir").getAbsolutePath(), mask);
        this.mDirectory = dir;
        this.mHandler = checkNotNull(handler, "handler");
        this.mListener = checkNotNull(listener, "listener");
    }

    @Override
    public String toString() {
        return toStringHelper(this).addValue(mDirectory).toString();
    }

    @Override
    public void onEvent(int event, final String path) {
        if (DEBUG) log(event, path);

        if ((event & DIR_CHANGED_MASK) != 0) {
            mHandler.removeCallbacks(mListener);
            mHandler.postDelayed(mListener, BATCH_UPDATE_DELAY);
        }
    }

    private void log(int event, String path) {
        if ((event & OPEN) != 0) debug("OPEN", path);
        else if ((event & ACCESS) != 0) debug("ACCESS", path);
        else if ((event & ATTRIB) != 0) debug("ATTRIB", path);
        else if ((event & CREATE) != 0) debug("CREATE", path);
        else if ((event & DELETE) != 0) debug("DELETE", path);
        else if ((event & MODIFY) != 0) debug("MODIFY", path);
        else if ((event & MOVED_TO) != 0) debug("MOVED_TO", path);
        else if ((event & MOVE_SELF) != 0) debug("MOVE_SELF", path);
        else if ((event & MOVED_FROM) != 0) debug("MOVED_FROM", path);
        else if ((event & CLOSE_WRITE) != 0) debug("CLOSE_WRITE", path);
        else if ((event & DELETE_SELF) != 0) debug("DELETE_SELF", path);
        else if ((event & CLOSE_NOWRITE) != 0) debug("CLOSE_NOWRITE", path);
        else debug("UNKNOWN", path);
    }

    private void debug(String event, String path) {
        Log.d(TAG, event +
                ", dir=" + mDirectory +
                ", lastModified=" + mDirectory.lastModified() +
                ", path=" + path);
    }

}