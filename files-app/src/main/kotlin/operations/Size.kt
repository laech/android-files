package l.files.operations

import java.io.IOException

import l.files.fs.Path
import l.files.fs.Resource

private class Size(paths: Iterable<Path>) : Count(paths) {

    volatile var size = 0L
        private set

    override fun onCount(resource: Resource) {
        super.onCount(resource)
        try {
            size += resource.readStatus(false).size
        } catch (e: IOException) {
            // Ignore count
        }
    }

}
