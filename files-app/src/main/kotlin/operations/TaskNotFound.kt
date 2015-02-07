package l.files.operations

/**
 * This event is posted when an attempt to cancel a task is received, but that
 * task does not exist. This could occur due to an error causing the app to
 * crash, then the user attempts to cancel the now invalid notification.
 */
data class TaskNotFound(val taskId: Int)
