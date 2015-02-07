package l.files.fs

import l.files.fs.WatchEvent.Kind

data class WatchEvent(val kind: Kind, val path: Path) {

    enum class Kind {
        CREATE
        DELETE
        MODIFY
    }

    /**
     * Listener to be notified when files are being added/changed/removed.
     *
     * Note that when a listener method is called, the target file may have
     * already be changed again.
     *
     * Methods defined in this listener will be called from a background thread,
     * and expensive operations should be moved out of the thread to avoid
     * blocking of events to other listeners.
     */
    trait Listener {
        fun onEvent(event: WatchEvent)
    }

}
