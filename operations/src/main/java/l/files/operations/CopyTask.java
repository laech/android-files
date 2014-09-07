package l.files.operations;

import android.os.Handler;

import java.io.File;

import de.greenrobot.event.EventBus;

final class CopyTask extends Task implements CopyTaskInfo {

  private final String dstName;
  private final Size size;
  private final Copy copy;

  CopyTask(int id, EventBus bus, Handler handler,
           Iterable<String> sources, String dstPath) {
    super(id, bus, handler);
    this.size = new Size(sources);
    this.copy = new Copy(sources, dstPath);
    this.dstName = new File(dstPath).getName();
  }

  @Override protected void doTask() throws FileException, InterruptedException {
    size.execute();
    copy.execute();
  }

  @Override public String getDestinationName() {
    return dstName;
  }

  @Override public long getTotalByteCount() {
    return size.getSize();
  }

  @Override public long getProcessedByteCount() {
    return copy.getCopiedByteCount();
  }

  @Override public int getTotalItemCount() {
    return size.getCount();
  }

  @Override public int getProcessedItemCount() {
    return copy.getCopiedItemCount();
  }

  @Override public boolean isCleanup() {
    return false;
  }
}
