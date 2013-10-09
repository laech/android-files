package l.files.event.internal;

import java.io.File;
import java.io.IOException;

public abstract class FileOperation {

    private volatile boolean mCancelled;

    public static FileOperation newCopy(File source, File destination) {
        return new Copy(source, destination);
    }

    public void cancel() {
        mCancelled = true;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public abstract void execute() throws IOException;
}
