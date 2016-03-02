package l.files.ui.info;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.FileSystem.SizeVisitor;
import l.files.fs.Files;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.info.CalculateSizeLoader.Size;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;

final class CalculateSizeLoader
        extends AsyncTaskLoader<Size>
        implements SizeVisitor {

    private boolean started;
    private volatile int currentCount;
    private volatile long currentSize;
    private volatile long currentSizeOnDisk;
    private volatile Size result;

    private final Path dir;
    private final Collection<Name> children;

    CalculateSizeLoader(Context context, Path dir, Collection<Name> children) {
        super(context);
        this.dir = requireNonNull(dir);
        this.children = requireNonNull(children);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (!started) {
            started = true;
            forceLoad();
        }
    }

    @Override
    public Size loadInBackground() {

        currentCount = 0;
        currentSize = 0;
        currentSizeOnDisk = 0;

        for (Name child : children) {
            try {
                Files.traverseSize(dir.resolve(child), FOLLOW, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isLoadInBackgroundCanceled()) {
            currentCount = 0;
            currentSize = 0;
            currentSizeOnDisk = 0;
        }

        result = Size.of(currentCount, currentSize, currentSizeOnDisk);
        return result;
    }

    @Override
    public boolean onSize(long size, long sizeOnDisk) throws RuntimeException {
        currentCount++;
        currentSize += size;
        currentSizeOnDisk += sizeOnDisk;
        return !isLoadInBackgroundCanceled();
    }

    int currentCount() {
        return currentCount;
    }

    long currentSize() {
        return currentSize;
    }

    long currentSizeOnDisk() {
        return currentSizeOnDisk;
    }

    boolean finished() {
        return result != null;
    }

    static final class Size {

        private final int count;
        private final long size;
        private final long sizeOnDisk;

        Size(int count, long size, long sizeOnDisk) {
            this.count = count;
            this.size = size;
            this.sizeOnDisk = sizeOnDisk;
        }

        int count() {
            return count;
        }

        long size() {
            return size;
        }

        long sizeOnDisk() {
            return sizeOnDisk;
        }

        static Size of(int count, long size, long sizeOnDisk) {
            return new Size(count, size, sizeOnDisk);
        }
    }
}
