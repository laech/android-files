package l.files.operations;

import com.google.auto.value.AutoValue;

/**
 * This event is posted when an attempt to cancel a task is received, but that
 * task does not exist. This could occur due to an error causing the app to
 * crash, then the user attempts to cancel the now invalid notification.
 */
@AutoValue
public abstract class TaskNotFound {

    TaskNotFound() {
    }

    public abstract int id();

    public static TaskNotFound create(int id) {
        return new AutoValue_TaskNotFound(id);
    }

}
