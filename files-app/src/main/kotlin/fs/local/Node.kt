package l.files.fs.local

import kotlin.platform.platformStatic

private data class Node(val device: Long, val inode: Long) {

    companion object {

        platformStatic fun from(status: LocalResourceStatus) =
                Node(status.device, status.inode)

    }

}
