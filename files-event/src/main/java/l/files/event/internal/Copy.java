package l.files.event.internal;

import android.util.Log;

import java.io.*;
import java.nio.channels.FileChannel;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.event.BuildConfig.DEBUG;
import static l.files.common.io.Files.canonicalStartsWith;
import static org.apache.commons.io.IOUtils.closeQuietly;

final class Copy extends FileOperation {

    private static final String TAG = Copy.class.getSimpleName();

    private static final long ONE_KB = 1024;
    private static final long ONE_MB = ONE_KB * ONE_KB;
    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    private final File source;
    private final File destination;

    Copy(File source, File destination) {
        this.source = checkNotNull(source, "source");
        this.destination = checkNotNull(destination, "destination");
    }

    @Override
    public void execute() throws IOException {
        if (isCancelled()) {
            return;
        }
        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    private void copyFile(File srcFile, File destFile) throws IOException {
        if (isCancelled()) {
            return;
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        final File parentFile = destFile.getParentFile();
        if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
            throw new IOException("Destination '" + parentFile + "' directory cannot be created");
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile);
    }

    private void doCopyFile(File srcFile, File destFile) throws IOException {
        if (isCancelled()) {
            return;
        }
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            final long size = input.size();
            long pos = 0;
            long count;
            while (pos < size) {
                if (isCancelled()) {
                    break;
                }
                count = (size - pos) > FILE_COPY_BUFFER_SIZE
                        ? FILE_COPY_BUFFER_SIZE
                        : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            closeQuietly(output);
            closeQuietly(fos);
            closeQuietly(input);
            closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            if (isCancelled()) {
                if (!destFile.delete() && DEBUG) {
                    Log.d(TAG, "Failed to delete file on cancel: " + destFile);
                }
                return;
            } else {
                throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
            }
        }
        if (!destFile.setLastModified(srcFile.lastModified())) {
            if (DEBUG) {
                Log.d(TAG, "Failed to set last modified date on " + destFile);
            }
        }
    }

    private void copyDirectory(File srcDir, File destDir) throws IOException {
        if (isCancelled()) {
            return;
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (canonicalStartsWith(destDir, srcDir)) {
            throw new IOException("Source '" + srcDir + "' is the canonical parent of destination '" + destDir + "'");
        }
        doCopyDirectory(srcDir, destDir);
    }

    private void doCopyDirectory(File srcDir, File destDir) throws IOException {
        if (isCancelled()) {
            return;
        }
        final File[] srcFiles = srcDir.listFiles();
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (File srcFile : srcFiles) {
            if (isCancelled()) {
                return;
            }
            final File dstFile = new File(destDir, srcFile.getName());
            if (srcFile.isDirectory()) {
                doCopyDirectory(srcFile, dstFile);
            } else {
                doCopyFile(srcFile, dstFile);
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (!destDir.setLastModified(srcDir.lastModified())) {
            if (DEBUG) {
                Log.d(TAG, "Failed to set last modified date on " + destDir);
            }
        }
    }
}
