package l.files.operations

import java.io.IOException

import l.files.fs.Path
import l.files.logging.Logger

private class Size(paths: Iterable<Path>) : Count(paths) {

    volatile var size: Long = 0
        private set

    override fun onCount(path: Path) {
        super.onCount(path)
        try {
            size += path.resource.readStatus(false).size
        } catch (e: IOException) {
            logger.debug(e)
        }
    }

    class object {

        private val logger = Logger.get(javaClass<Size>())

    }

}
