package l.files.operations;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static l.files.io.file.operations.FileOperation.Failure;

@AutoValue
public abstract class DeleteProgress extends Progress {
  DeleteProgress() {}

  /**
   * The total number of items to be processed. This value may increase while
   * the task is in {@link State#PREPARING}.
   */
  public abstract int totalItemCount();

  /**
   * The total number of items deleted so far.
   */
  public abstract int deletedItemCount();

  public static DeleteProgress create(
      int taskId, long taskStartTime, State state, List<Failure> failures,
      int totalItemCount, int deletedItemCount) {
    return new AutoValue_DeleteProgress(taskId, taskStartTime, state,
        ImmutableList.copyOf(failures), totalItemCount, deletedItemCount);
  }
}
