package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Delete extends AbstractOperation {

  private final AtomicInteger deletedItemCount = new AtomicInteger();
  private final AtomicLong deletedByteCount = new AtomicLong();

  Delete(Collection<? extends Resource> resources) {
    super(resources);
  }

  public int getDeletedItemCount() {
    return deletedItemCount.get();
  }

  public long getDeletedByteCount() {
    return deletedByteCount.get();
  }

  @Override void process(Resource resource) {
    traverse(resource, new OperationVisitor() {

      @Override public Result onPostVisit(Resource res) throws IOException {
        delete(res);
        return super.onPostVisit(res);
      }

    });
  }

  private void delete(Resource resource) throws IOException {
    long size = resource.stat(NOFOLLOW).size();
    resource.delete();
    deletedByteCount.addAndGet(size);
    deletedItemCount.incrementAndGet();
  }

}
