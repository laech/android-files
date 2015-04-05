package l.files.operations;

import auto.parcel.AutoParcel;

/**
 * This event is posted when an attempt to cancel a task is received, but that
 * task does not exist. This could occur due to an error causing the app to
 * crash, then the user attempts to cancel the now invalid notification.
 */
@AutoParcel
public abstract class TaskNotFound {

    TaskNotFound() {
    }

    public abstract int getTaskId();

    public static TaskNotFound create(int id) {
        return new AutoParcel_TaskNotFound(id);
    }

}
