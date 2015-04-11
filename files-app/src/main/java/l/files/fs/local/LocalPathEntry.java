package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import auto.parcel.AutoParcel;
import l.files.fs.PathEntry;

@AutoParcel
abstract class LocalPathEntry implements PathEntry {

    LocalPathEntry() {
    }

    @Override
    public abstract LocalResource getResource();

    public abstract long getInode();

    public abstract boolean isDirectory();

    @Override
    public LocalPath getPath() {
        return getResource().getPath();
    }

    public static LocalPathEntry create(LocalResource resource, long inode, boolean directory) {
        return new AutoParcel_LocalPathEntry(resource, inode, directory);
    }

    public static LocalPathEntry stat(File file) throws IOException {
        return read(LocalResource.create(file));
    }

    public static LocalPathEntry read(LocalResource resource) throws IOException {
        LocalResourceStatus status = LocalResourceStatus.stat(resource.getFile(), false);
        return create(resource, status.getInode(), status.isDirectory());
    }

    // observerable

}
