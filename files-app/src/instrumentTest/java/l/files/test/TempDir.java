package l.files.test;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.createTempDir;
import static com.google.common.io.Files.touch;
import static java.lang.System.nanoTime;
import static junit.framework.Assert.assertTrue;

public final class TempDir {

    public static TempDir create() {
        return new TempDir(createTempDir());
    }

    private final File mDir;

    private TempDir(File dir) {
        this.mDir = dir;
    }

    public void delete() {
        delete(mDir);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void delete(File file) {
        if (file.isDirectory()) {
            file.setExecutable(true, true);
        }
        file.setReadable(true, true);
        final File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                delete(child);
            }
        }
        file.delete();
    }

    public File get() {
        return mDir;
    }

    public File newFile() {
        return newFile(String.valueOf(nanoTime()));
    }

    public File newFile(String name) {
        final File file = new File(mDir, name);
        final File parent = file.getParentFile();
        assertTrue(parent.exists() || parent.mkdirs());
        try {
            touch(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(file.isFile());
        return file;
    }

    public File newDir() {
        return newDir(String.valueOf(nanoTime()));
    }

    public File newDir(String name) {
        final File file = new File(mDir, name);
        assertTrue(file.mkdirs() || file.isDirectory());
        return file;
    }
}
