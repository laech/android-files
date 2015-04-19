package l.files.fs;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class WatchEvent {

    WatchEvent() {
    }

    public abstract Kind getKind();

    public abstract Resource getResource();

    public static WatchEvent create(Kind kind, Resource resource) {
        return new AutoParcel_WatchEvent(kind, resource);
    }

    public enum Kind {
        CREATE,
        DELETE,
        MODIFY
    }

    /**
     * Listener to be notified when files are being added/changed/removed.
     * <p/>
     * Note that when a listener method is called, the target file may have
     * already be changed again.
     * <p/>
     * Methods defined in this listener will be called from a background thread,
     * and expensive operations should be moved out of the thread to avoid
     * blocking of events to other listeners.
     */
    public interface Listener {
        void onEvent(WatchEvent event);
    }

}
