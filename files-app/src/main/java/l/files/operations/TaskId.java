package l.files.operations;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class TaskId {

    TaskId() {
    }

    public abstract int getId();

    public abstract TaskKind getKind();

    public static TaskId create(int id, TaskKind kind) {
        return new AutoParcel_TaskId(id, kind);
    }

}
