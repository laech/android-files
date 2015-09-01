package l.files.operations;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TaskId {

  TaskId() {
  }

  public abstract int getId();

  public abstract TaskKind getKind();

  public static TaskId create(int id, TaskKind kind) {
    return new AutoValue_TaskId(id, kind);
  }

}
