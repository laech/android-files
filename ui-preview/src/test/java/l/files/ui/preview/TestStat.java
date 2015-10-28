package l.files.ui.preview;

import java.io.File;
import java.util.Set;

import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Stat;

class TestStat implements Stat {

    private final File file;

    TestStat(File file) {
        this.file = file;
    }

    @Override
    public long size() {
        return file.length();
    }

    @Override
    public boolean isRegularFile() {
        return file.isFile();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isFifo() {
        return false;
    }

    @Override
    public boolean isSocket() {
        return false;
    }

    @Override
    public boolean isBlockDevice() {
        return false;
    }

    @Override
    public boolean isCharacterDevice() {
        return false;
    }

    @Override
    public Instant lastModifiedTime() {
        return Instant.ofMillis(file.lastModified());
    }

    @Override
    public Set<Permission> permissions() {
        throw new UnsupportedOperationException();
    }

}
