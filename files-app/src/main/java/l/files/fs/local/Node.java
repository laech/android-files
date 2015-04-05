package l.files.fs.local;

import auto.parcel.AutoParcel;

@AutoParcel
abstract class Node {

    Node() {
    }

    public abstract long getDevice();

    public abstract long getInode();

    public static Node create(long device, long inode) {
        return new AutoParcel_Node(device, inode);
    }

    public static Node from(LocalResourceStatus status) {
        return create(status.getDevice(), status.getInode());
    }

}
