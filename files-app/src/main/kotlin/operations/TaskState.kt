package l.files.operations

/**
 * Represents the state of a task.
 * Instances of this will be posted to the event bus during task execution.
 */
trait TaskState {

    /**
     * The source task this state is for.
     */
    val task: TaskId

    /**
     * Gets the source/destination of the source task.
     */
    val target: Target

    /**
     * Gets the time when the state transitioned to this one.
     * This time will stay the same for subsequent state updates of the same kind.
     * i.e. this value will only change if the next state is of a different kind.
     */
    val time: Time

    /**
     * Returns true if the task is finished (success or failure).
     */
    val isFinished: Boolean get() = this is Success || this is Failed

    data class Pending(override val task: TaskId,
                       override val target: Target,
                       override val time: Time) : TaskState {

        fun running(time: Time) =
                running(time, Progress.NONE, Progress.NONE)

        fun running(time: Time, items: Progress, bytes: Progress) =
                Running(task, target, time, items, bytes)

    }

    data class Running(override val task: TaskId,
                       override val target: Target,
                       override val time: Time,

                       /**
                        * Number of items to process.
                        */
                       val items: Progress,

                       /**
                        * Number of bytes to process.
                        */
                       val bytes: Progress) : TaskState {

        fun running(items: Progress, bytes: Progress) =
                // Do not update the time as specified by the contract on time()
                Running(task, target, time, items, bytes)


        fun success(time: Time) = Success(task, target, time)

        fun failed(time: Time, failures: List<Failure>) =
                Failed(task, target, time, failures)

    }

    data class Success(override val task: TaskId,
                       override val target: Target,
                       override val time: Time) : TaskState

    data class Failed(override val task: TaskId,
                      override val target: Target,
                      override val time: Time,

                      /**
                       * The file failures of the task, may be empty if the task if caused by
                       * other errors.
                       */
                      val failures: List<Failure>) : TaskState


    class object {

        fun pending(task: TaskId, target: Target, time: Time) =
                Pending(task, target, time)

    }

}
