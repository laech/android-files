package l.files.operations;

import androidx.annotation.Nullable;

import static l.files.base.Objects.requireNonNull;

public final class TaskId {

    private final int id;
    private final TaskKind kind;

    private TaskId(int id, TaskKind kind) {
        this.id = id;
        this.kind = requireNonNull(kind);
    }

    public int id() {
        return id;
    }

    public TaskKind kind() {
        return kind;
    }

    public static TaskId create(int id, TaskKind kind) {
        return new TaskId(id, kind);
    }

    @Override
    public String toString() {
        return "TaskId{" +
                "id=" + id +
                ", kind=" + kind +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskId taskId = (TaskId) o;

        return id == taskId.id && kind == taskId.kind;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + kind.hashCode();
        return result;
    }
}
