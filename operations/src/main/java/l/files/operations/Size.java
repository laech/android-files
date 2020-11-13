package l.files.operations;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.concurrent.atomic.LongAdder;

final class Size extends Count {

    private final LongAdder size = new LongAdder();

    Size(Collection<? extends Path> sourcePaths) {
        super(sourcePaths);
    }

    public long getSize() {
        return size.longValue();
    }

    @Override
    void onCount(BasicFileAttributes attrs) {
        super.onCount(attrs);
        size.add(attrs.size());
    }

}
