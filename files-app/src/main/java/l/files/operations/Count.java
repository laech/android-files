package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Resource;

class Count extends AbstractOperation {

  private final AtomicInteger count = new AtomicInteger();

  Count(Collection<? extends Resource> resources) {
    super(resources);
  }

  public int getCount() {
    return count.get();
  }

  @Override void process(Resource resource) {
    traverse(resource, new OperationVisitor() {

      @Override public Result onPreVisit(Resource res) throws IOException {
        count.incrementAndGet();
        onCount(res);
        return super.onPreVisit(res);
      }

    });
  }

  void onCount(Resource resource) {
  }

}
