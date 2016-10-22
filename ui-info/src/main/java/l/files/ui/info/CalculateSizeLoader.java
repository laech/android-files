package l.files.ui.info;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;

import l.files.fs.Files;
import l.files.fs.FileName;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.TraversalCallback;
import l.files.ui.info.CalculateSizeLoader.Size;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;

final class CalculateSizeLoader
        extends AsyncTaskLoader<Size>
        implements TraversalCallback<Path> {

    private boolean started;
    private volatile int currentCount;
    private volatile long currentSize;
    private volatile long currentSizeOnDisk;

    @Nullable
    private volatile Size result;

    private final Path dir;
    private final Stat statBuffer;
    private final Collection<FileName> children;

    CalculateSizeLoader(Context context, Path dir, Collection<FileName> children) {
        super(context);
        this.dir = requireNonNull(dir);
        this.statBuffer = Files.newEmptyStat(dir);
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

        for (FileName child : children) {
            Path path = dir.resolve(child);
            try {
                Files.traverse(path, NOFOLLOW, this);
            } catch (IOException e) {
                Log.w(getClass().getSimpleName(),
                        "Failed to traverse " + path, e);
            }
        }

        if (isLoadInBackgroundCanceled()) {
            currentCount = 0;
            currentSize = 0;
            currentSizeOnDisk = 0;
        }

        Size size = Size.of(currentCount, currentSize, currentSizeOnDisk);
        result = size;
        return size;
    }

    @Override
    public Result onPreVisit(Path path) throws IOException {
        if (isLoadInBackgroundCanceled()) {
            return Result.TERMINATE;
        }

        Files.stat(path, NOFOLLOW, statBuffer);

        currentCount++;
        currentSize += statBuffer.size();
        currentSizeOnDisk += statBuffer.sizeOnDisk();

        return Result.CONTINUE;
    }

    @Override
    public Result onPostVisit(Path path) throws IOException {
        return Result.CONTINUE;
    }

    @Override
    public void onException(Path path, IOException e) throws IOException {
        Log.w(getClass().getSimpleName(),
                "Failed to visit " + path, e);
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
