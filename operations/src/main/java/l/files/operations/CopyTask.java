package l.files.operations;

import java.io.File;

import l.files.io.file.operations.Copy;
import l.files.io.file.operations.Size;
import l.files.operations.info.CopyTaskInfo;

final class CopyTask extends Task implements CopyTaskInfo {

  private final String dstName;
  private final Size size;
  private final Copy copy;

  CopyTask(int id, Iterable<String> sources, String dstPath) {
    super(id);
    this.size = new Size(sources);
    this.copy = new Copy(sources, dstPath);
    this.dstName = new File(dstPath).getName();
  }

  @Override protected void doTask() throws InterruptedException {
    size.call();
    copy.call();
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
