package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.Path;
import l.files.fs.PathEntry;
import l.files.fs.Resource;

@AutoParcel
abstract class LocalPathEntry implements PathEntry {

    LocalPathEntry() {
    }

    @Override
    public abstract LocalPath getPath();

    public abstract long getInode();

    public abstract boolean isDirectory();

    @Override
    public LocalResource getResource() {
        return getPath().getResource();
    }

    public static LocalPathEntry create(LocalPath path, long inode, boolean directory) {
        return new AutoParcel_LocalPathEntry(path, inode, directory);
    }

    public static LocalPathEntry stat(File file) throws IOException {
        return read(LocalPath.of(file));
    }

    public static LocalPathEntry read(LocalPath path) throws IOException {
        LocalResourceStatus status = LocalResourceStatus.stat(path, false);
        return create(path, status.getInode(), status.isDirectory());
    }

}
