package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import auto.parcel.AutoParcel;

@AutoParcel
abstract class LocalPathEntry {

    LocalPathEntry() {
    }

    public abstract LocalResource getResource();

    public abstract long getInode();

    public abstract boolean isDirectory();

    public static LocalPathEntry create(LocalResource resource, long inode, boolean directory) {
        return new AutoParcel_LocalPathEntry(resource, inode, directory);
    }

    public static LocalPathEntry stat(File file) throws IOException {
        return read(LocalResource.create(file));
    }

    public static LocalPathEntry read(LocalResource resource) throws IOException {
        LocalResourceStatus status = LocalResourceStatus.stat(resource, false);
        return create(resource, status.getInode(), status.isDirectory());
    }

    // TODO observerable

}
