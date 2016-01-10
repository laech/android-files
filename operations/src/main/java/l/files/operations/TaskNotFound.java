package l.files.operations;

/**
 * This event is posted when an attempt to cancel a task is received, but that
 * task does not exist. This could occur due to an error causing the app to
 * crash, then the user attempts to cancel the now invalid notification.
 */
public final class TaskNotFound {

    private final int id;

    private TaskNotFound(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static TaskNotFound create(int id) {
        return new TaskNotFound(id);
    }

    @Override
    public String toString() {
        return "TaskNotFound{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskNotFound that = (TaskNotFound) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
