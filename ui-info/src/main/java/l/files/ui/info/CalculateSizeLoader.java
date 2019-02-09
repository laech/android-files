package l.files.ui.info;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.Name;
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

    private final Path parentDirectory;
    private final Collection<Name> children;

    CalculateSizeLoader(
            Context context,
            Path parentDirectory,
            Collection<Name> children
    ) {
        super(context);
        this.parentDirectory = requireNonNull(parentDirectory);
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
            Path path = parentDirectory.concat(child);
            try {
                path.traverse(NOFOLLOW, this);
            } catch (IOException e) {
                // TODO report to UI
            }
        }

        if (isLoadInBackgroundCanceled()) {
            currentCount = 0;
            currentSize = 0;
            currentSizeOnDisk = 0;
        }

        return (result = progress());
    }

    Size progress() {
        return new Size(
                currentCount,
                currentSize,
                currentSizeOnDisk
        );
    }

    @Override
    public Result onPreVisit(Path path) throws IOException {
        if (isLoadInBackgroundCanceled()) {
            return Result.TERMINATE;
        }

        Stat stat = path.stat(NOFOLLOW);

        currentCount++;
        currentSize += stat.size();
        currentSizeOnDisk += stat.sizeOnDisk();

        return Result.CONTINUE;
    }

    @Override
    public Result onPostVisit(Path path) throws IOException {
        return Result.CONTINUE;
    }

    @Override
    public void onException(Path path, IOException e) throws IOException {
        // TODO report to UI
    }

    boolean isRunning() {
        return isStarted() && result == null;
    }

    static final class Size {
        final int count;
        final long size;
        final long sizeOnDisk;

        Size(int count, long size, long sizeOnDisk) {
            this.count = count;
            this.size = size;
            this.sizeOnDisk = sizeOnDisk;
        }
    }
}
