package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.File;

class Count extends AbstractOperation {

  private final AtomicInteger count = new AtomicInteger();

  Count(Collection<? extends File> resources) {
    super(resources);
  }

  public int getCount() {
    return count.get();
  }

  @Override void process(File file) {
    traverse(file, new OperationVisitor() {

      @Override public Result onPreVisit(File res) throws IOException {
        count.incrementAndGet();
        onCount(res);
        return super.onPreVisit(res);
      }

    });
  }

  void onCount(File file) {
  }

}
