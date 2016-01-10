package l.files.operations;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static l.files.base.Objects.requireNonNull;

/**
 * Represents the state of a task. Instances of this will be posted to the event
 * bus during task execution.
 */
public abstract class TaskState {

    private final TaskId task;
    private final Target target;
    private final Time time;

    TaskState(TaskId task, Target target, Time time) {
        this.task = requireNonNull(task);
        this.target = requireNonNull(target);
        this.time = requireNonNull(time);
    }

    /**
     * The source task this state is for.
     */
    public TaskId task() {
        return task;
    }

    /**
     * Gets the source/destination of the source task.
     */
    public Target target() {
        return target;
    }

    /**
     * Gets the time when the state transitioned to this one. This time will
     * stay the same for subsequent state updates of the same kind. i.e. this
     * value will only change if the next state is of a different kind.
     */
    public Time time() {
        return time;
    }

    /**
     * Returns true if the task is finished (success or failure).
     */
    public boolean isFinished() {
        return this instanceof Success || this instanceof Failed;
    }

    public static final class Pending extends TaskState {

        Pending(TaskId task, Target target, Time time) {
            super(task, target, time);
        }

        public Running running(Time time) {
            return running(time, Progress.NONE, Progress.NONE);
        }

        public Running running(Time time, Progress items, Progress bytes) {
            return new Running(task(), target(), time, items, bytes);
        }

    }

    public static final class Running extends TaskState {

        private final Progress items;
        private final Progress bytes;

        Running(TaskId task, Target target, Time time, Progress items, Progress bytes) {
            super(task, target, time);
            this.items = requireNonNull(items);
            this.bytes = requireNonNull(bytes);
        }

        /**
         * Number of items to process.
         */
        public Progress items() {
            return items;
        }

        /**
         * Number of bytes to process.
         */
        public Progress bytes() {
            return bytes;
        }

        public Running running(Progress items, Progress bytes) {
            // Do not update the time as specified by the contract on time()
            return new Running(task(), target(), time(), items, bytes);
        }

        public Success success(Time time) {
            return new Success(task(), target(), time);
        }

        public Failed failed(Time time, List<Failure> failures) {
            return new Failed(task(), target(), time,
                    unmodifiableList(new ArrayList<>(failures)));
        }
    }

    public static final class Success extends TaskState {
        Success(TaskId task, Target target, Time time) {
            super(task, target, time);
        }
    }

    public static final class Failed extends TaskState {

        private final List<Failure> failures;

        Failed(TaskId task, Target target, Time time, List<Failure> failures) {
            super(task, target, time);
            this.failures = requireNonNull(failures);
        }

        /**
         * The file failures of the task, may be empty if the task if caused by
         * other errors.
         */
        public List<Failure> failures() {
            return failures;
        }

    }

    public static Pending pending(TaskId task, Target target, Time time) {
        return new Pending(task, target, time);
    }

}
