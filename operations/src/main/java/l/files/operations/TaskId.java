package l.files.operations;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TaskId {

    TaskId() {
    }

    public abstract int id();

    public abstract TaskKind kind();

    public static TaskId create(int id, TaskKind kind) {
        return new AutoValue_TaskId(id, kind);
    }

}
